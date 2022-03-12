package CodigoFonte;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Servidor {

    private ServerSocket serverSocket;
    private static final int BUFFER_SIZE = 4096;
    private byte[] buf = new byte[BUFFER_SIZE];

    //Cria um servidor para comunicação
    protected void GerarServerSocket(int porta) throws IOException {

        serverSocket = new ServerSocket(porta);
    }

    //Ficar aguardando uma requisição
    protected Socket AguardaConexao() throws IOException {

        //Objeto que encapsula a conexão
        Socket socket = serverSocket.accept();
        return socket;
    }

    //Elimina socket
    protected void EncerrarSocket(Socket socket) throws IOException {
        socket.close();
    }

    //Recebe um socket e faz o envio do arquivo
    protected void RespostaComArquivo(Socket socket) throws IOException {
        OutputStream  saida = null;
        InputStream  entrada = null;
        int numberRead = 0;


        try {
            saida = socket.getOutputStream();
            entrada = socket.getInputStream();

            numberRead = entrada.read(buf, 0, BUFFER_SIZE);

            if(numberRead <0 )
                return;

            //Pega dados de entrada na conexão
            byte[] BufferLido = new byte[numberRead];
            System.arraycopy(buf, 0, BufferLido, 0, numberRead);
            String cabecalho = new String(BufferLido);
            String nomeArquivo = cabecalho.split("\r\n")[0];
            nomeArquivo = nomeArquivo.split(" ")[1];
            nomeArquivo = nomeArquivo.substring(1);

            //Valida e Localiza arquivo
            File arquivo = new File("Arquivos\\" + nomeArquivo);
            if(!arquivo.exists())
                return;

            //Montando resposta a requisição com o arquivo solicitado
            saida.write("HTTP/1.1 200 OK\r\n".getBytes());
            saida.write("Accept-Ranges: bytes\r\n".getBytes());
            saida.write(("Content-Length: " + arquivo.length() + "\r\n").getBytes());
            saida.write("Content-Type: application/octet-stream\r\n".getBytes());
            saida.write(("Content-Disposition: attachment; filename=\"" + nomeArquivo + "\"\r\n").getBytes());
            saida.write("\r\n".getBytes());
            Files.copy(Paths.get("Arquivos\\" + nomeArquivo) , saida);

        } catch (Exception e) {
            if(!e.getMessage().contains("Read timed out"))
                System.out.println("Erro na conexão com o cliente: " + socket.getInetAddress() + " Mensagem: " + e.getLocalizedMessage());
        } finally {
            //Encerra socket e objetos de input e output de stream
            if (entrada != null) entrada.close();
            if (saida != null) saida.close();
            EncerrarSocket(socket);
        }
    }
}
