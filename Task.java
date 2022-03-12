package CodigoFonte;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Task implements  Runnable
{
    Socket socket;
    Servidor servidor;

    public Task(Servidor serv, Socket skt)
    {
        servidor = serv;
        socket = skt;
    }

    //Aqui é onde cada conexão se torna uma thread e inicia
    // o download dos arquivos de maneira assíncrona para múltiplos usuários
    public void run() {
        try
        {
            socket.setSoTimeout(1000);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        System.out.println("Cliente conectado: " + socket.getInetAddress());

        try
        {
            servidor.RespostaComArquivo(socket);
        }
        catch
        (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Fim da conexão cliente");
    }
}
