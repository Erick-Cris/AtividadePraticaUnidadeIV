package CodigoFonte;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
* Atividade Prática: Unidade IV
* Aluno: Erick Cristian de Oliveira Pereira
* Matrícula: 11621BSI265
* */

public class Main {

    public static void main(String[] args) {

        try
        {
            System.out.println("Iniciando Servidor");
            Servidor servidor = new Servidor();
            servidor.GerarServerSocket(4001);
            System.out.println("Inicializacao do servidor concluida");

            int nucleos = Runtime.getRuntime().availableProcessors();
            ExecutorService pool = Executors.newFixedThreadPool(nucleos);

            while (true)
            {
                System.out.println("Aguardando conexão cliente");
                //Espera Conexão e retorna um Socket com a conexão quando uma requisição chegar
                Socket socket = servidor.AguardaConexao();

                pool.execute(new Task(servidor, socket));

            }

        }
        catch (Exception e)
        {
            System.out.println("Erro fatal: " + e.getMessage());
        }
    }
}
