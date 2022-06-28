package tcp;

// Importações necessárias 
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;

// Classe de cliente TCP

public class ClienteTCP {
    

    // Método main para deixar o cliente rodando
    public static void main (String args []) throws Exception {

        //Instanciação de um socket usando a porta 9000 local
        // Lembrando que o socket deve ser instanciado com uma porta entre 1024 e 65535 
        Socket s = new Socket("127.0.0.1", 9000);

        // Criação de stream de saída (escrita) de informações pelo socket
        OutputStream os =  s.getOutputStream();
        DataOutputStream writer = new DataOutputStream(os);

        // Criação de stream de entrada (leitura) de informações pelo socket
        InputStreamReader is =  new InputStreamReader(s.getInputStream());
        BufferedReader reader = new BufferedReader(is);

        // Criando buffer para leitura de dados do teclado
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        // Leitura dos dados do teclado
        // Método bloaqueante
        String texto = inFromUser.readLine();

        // Passando o texto da string para o socker e já enviando ao host remoto
        writer.writeBytes(texto + "\n");

        // Recebendo dados do servidor, os salvando em uma string e imprimindo na tela
        // Essa parte do código é bloqueante, logo não avança até que sejam recebidas informações do servidor
        String response = reader.readLine();
        System.out.println("Do servidor: " + response);

        // Fechamento do canal de comunicação
        s.close(); 

    }

}
