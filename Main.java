import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Main
{
    int QTD_THREADS = 5;

    public ThreadPool threadPool = new ThreadPool (QTD_THREADS);

    public static void main (String [] argv)
            throws Exception
    {
        try
        {
            new Main().LigarServidor(argv);
        }
        catch (Exception e)
        {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    public void LigarServidor(String[] argv) throws Exception
    {
        int porta = 8080;

        //Instanciando de servidor/listenner.
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();

        //Gerenciador de acesso aos recursos de socket de maneira sincrona.
        Selector selector = Selector.open();
        serverSocket.bind(new InetSocketAddress(porta));

        //Configuracao para o socket trabalhar de maneira nao bloqueante
        serverChannel.configureBlocking(false);

        //Iformando que o socket do servidor vai trabalhar aceitando conexoes.
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true)
        {
            int n = selector.select();//Pega canais de sockets registrados.

            //Se nao houver nenhum canal registrado, nada acontece.
            if (n == 0) continue;

            //Chaves de identificacao de canais gerenciadas pelo seletor.
            Iterator chaves = selector.selectedKeys().iterator();

            // Percorre todas as chaves
            while (chaves.hasNext())
            {
                SelectionKey chave = (SelectionKey) chaves.next();

                // Essa condicao recebe as requisicoes feitas ao servidor e as aceita.
                if (chave.isAcceptable())
                {
                    ServerSocketChannel servidor = (ServerSocketChannel) chave.channel();
                    SocketChannel canal = servidor.accept();
                    addCanal(selector, canal, SelectionKey.OP_READ);
                }

                // Executa o trabalho de envio do arquivo para requisicoe ja aceitas pelo servidor.
                if (chave.isReadable())
                    executarDownload(chave);
                chaves.remove();
            }
        }
    }

    //Adiciona os canais de comunicacao de um socket ao gerenciador nao bloqueante.
    protected void addCanal(Selector gerenciadorDeChaves, SelectableChannel canal, int operacoes) throws Exception
    {
        if (canal == null) return;

        canal.configureBlocking(false);//Configurando canal do socket como nao bloqueante.
        canal.register(gerenciadorDeChaves, operacoes);//Adicionando canal no gerenciador para controle nao bloqueante.
    }

    protected void executarDownload(SelectionKey chave) throws Exception
    {
        WorkerThread worker = threadPool.getThreadWorker();//Pega uma thread para atende ao socket de comunicacao com o cliente.

        if (worker == null) return;

        worker.enviaArquivoCanalComunicacao (chave);//Aqui os canais serao verificados e se estiverem disponiveis serao usados para o envio do arquivo.
    }

    //Instancia e gerencia a execucao das threads. Uma thread por requisicao.
    public class ThreadPool
    {
        List threadsAtivas = new LinkedList();//Threas ativas, quantidade fixa, vao atender todas as requisicoes.

        ThreadPool (int qtd_threads)
        {
            for (int i = 0; i < qtd_threads; i++)
            {
                WorkerThread thread = new WorkerThread (this);
                thread.start();

                threadsAtivas.add(thread);
            }
        }

        WorkerThread getThreadWorker(  )
        {
            WorkerThread thread = null;

            synchronized (threadsAtivas)
            {
                if (threadsAtivas.size(  ) > 0)
                    thread = (WorkerThread) threadsAtivas.remove (0);
            }

            return (thread);
        }

        void returnWorkerThread (WorkerThread worker)
        {
            synchronized (threadsAtivas) {
                threadsAtivas.add (worker);
            }
        }
    }

    //Trabalho de envio de arquivo executado na thread
    public class WorkerThread extends Thread
    {
        //Reservei 9mb para os bytes referentes ao retorno da requisicao
        //Na resposta vai o cabecalho com protocolo http mais os bytes do arquivo no content da resposta.
        ByteBuffer buffer = ByteBuffer.allocate (9*1024*1024);

        ThreadPool threadPool;
        SelectionKey chaveDoCanal;

        WorkerThread (ThreadPool pool)
        {
            this.threadPool = pool;
        }

        public synchronized void run()
        {
            while (true)
            {
                try
                {
                    this.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    this.interrupted();
                }

                if (chaveDoCanal == null) continue;

                try {
                    escreverResposta(chaveDoCanal);
                } catch (Exception e)
                {
                    System.out.println ("Erro na thread: " + e.getMessage());

                    try
                    {
                        chaveDoCanal.channel().close();
                    } catch (IOException ex)
                    {
                        System.out.println("Erro ao abortar thread e fechar canal: " + ex.getMessage());
                    }
                    chaveDoCanal.selector().wakeup(  );
                }

                chaveDoCanal = null;
                this.threadPool.returnWorkerThread (this);
            }
        }

        synchronized void enviaArquivoCanalComunicacao (SelectionKey chave)
        {
            this.chaveDoCanal = chave;
            chave.interestOps (chave.interestOps() & (~SelectionKey.OP_READ));//Declara interesses ao usar o canal.
            this.notify();//Libera thread pra proseguir com seu trabalho.
        }

        //Construcao e envio da resposta http com arquivo.
        void escreverResposta (SelectionKey chave)
                throws Exception
        {
            try {
                SocketChannel canal = (SocketChannel) chave.channel();
                int statusBytesRestantes;//-1 significa que todos os bytes do buffer foram enviados.

                buffer.clear();
                String CRLF = "\r\n";//quebra de linha
                String linhaStatus = "";
                String linhaContentType = "";
                String linhaNomeArquivo = "";

                linhaStatus = "HTTP/1.0 200 OK" + CRLF;
                linhaContentType = "Content-type: application/octet-stream" + CRLF;
                linhaNomeArquivo = "Content-Disposition: attachment; filename=\"arquivoParaDownload.txt\"" + CRLF;
                File fis = new File("arquivoParaDownload.txt");

                buffer.put(linhaStatus.getBytes());
                buffer.put(linhaContentType.getBytes());
                buffer.put(linhaNomeArquivo.getBytes());
                buffer.put(CRLF.getBytes());
                buffer.put(Files.readAllBytes(fis.toPath()));

                //Escrevendo bytes da informacao no canal de escrita.
                int ret = 0;
                do{
                    buffer.flip();
                    ret = canal.write(buffer);
                    buffer.compact();
                }
                while ( ret > 0 );
                buffer.clear();
                statusBytesRestantes = -1;

                if (statusBytesRestantes < 0)
                {
                    //Fim do envio. Ecerrando canal e comunicacao com cliente.
                    canal.close();
                    return;
                }

                //Informa que o canal esta livre.
                chave.interestOps(chave.interestOps() | SelectionKey.OP_READ);

                //Atualiza o gerenciador de chaves das conexoes.
                chave.selector().wakeup();
            }
            catch (Exception e)
            {
                System.out.println("Erro no envio de dados na resposta. Erro: " + e.getMessage());
            }
        }
    }
}