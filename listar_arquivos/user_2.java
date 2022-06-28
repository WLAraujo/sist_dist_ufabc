import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class user_2 {

    public static void main(String[] args) {

        try{

            // Abrindo DatagramSocket de escuta do servidor na porta 9876
            DatagramSocket serverSocket = new DatagramSocket(9876);

            // Laço para prestar serviço contínuo
            while (true){

                // Declaração de buffer de recebimento
                byte[] bufferReceber = new byte[1024];

                // Criação do pacote datagrama a ser recebido
                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber,bufferReceber.length);

                // Método bloqueante que espera pacote
                serverSocket.receive(pacoteRecebido);

                // Salvando os dados recebidos em uma string
                String informacao = new String(pacoteRecebido.getData(), pacoteRecebido.getOffset(), pacoteRecebido.getLength());

                // Realizando parser da informação recebida
                String[] lista_arquivos = informacao.split("//");

                System.out.println("Arquivo recebido");

                // Imprimindo a informação
                for (String arquivo: lista_arquivos){
                    System.out.println(arquivo);
                }

            }

        }
        catch (Exception e){

        }

    }
    
}
