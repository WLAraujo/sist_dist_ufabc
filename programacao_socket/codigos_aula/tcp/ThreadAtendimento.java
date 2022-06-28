package tcp;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;

// Classe de threads
public class ThreadAtendimento extends Thread{

    // Variável que declara nome da thread
    private Socket porta = null;

    // Método construtor
    public ThreadAtendimento(Socket cliente){
        porta = cliente;
    }

    // Classe que dá start em uma thread, chamado pelo método start()
    public void run() {

        try{
            // Criação de stream de entrada (leitura) de informações pelo socket
            // Lembre-se no nosso processo o cliente envia informações primeiro
            InputStreamReader is =  new InputStreamReader(porta.getInputStream());
            BufferedReader reader = new BufferedReader(is);

            // Criação de stream de saída (escrita) de informações pelo socket
            OutputStream os =  porta.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);

            // Lendo mensagem enviada
            String request = reader.readLine();

            // Mensagem devolvida para o cliente
            writer.writeBytes(request.toUpperCase() + "\n");

            
        }
        catch (Exception e){}
    }

}