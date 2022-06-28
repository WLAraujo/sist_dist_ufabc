package tcp;

import java.net.ServerSocket;
import java.net.Socket;

public class ServidorTCP {

    // Método main que deixa o servidor rodando durante a aplicação
    public static void main (String args []) throws Exception {
    
        // Criação de mecanismo para escutar e atender pedidos de conexão por parte dos clientes usando porta 9000
        ServerSocket serverSocket = new ServerSocket(9000);
        
        // Laço while para deixar servidor rodando
        while(true){
            
            // Método bloqueante que cria um novo socket num nó designado pelo SO entre 1024 e 65535
            // O método aguarda um pedido de conexão
            Socket clienteSocket = serverSocket.accept();

            // Vamos criar uma nova thread para atender esse novo processo de conexão
            ThreadAtendimento thread = new ThreadAtendimento(clienteSocket);
            thread.start();


            // O processo de atendimento ao cliente envolve ler as informações do cliente e enviar informações de volta

        }
    }
}
