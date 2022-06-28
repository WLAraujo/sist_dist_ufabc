package udp;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPClient {

    public static void main(String[] args) {

        try{
            
            // Endereço de IP do servidor
            InetAddress IPAdress = InetAddress.getByName("127.0.0.1");

            // Canal de comunicação não orientado à conexão
            DatagramSocket clientSocket = new DatagramSocket();

            // Buffer com mensagem que será enviada
            byte[] dadosEnviados = new byte[1024];
            dadosEnviados = "Mensagem Enviada".getBytes();
            
            // Criação de datagrama para envio de informações
            DatagramPacket enviarPacote = new DatagramPacket(dadosEnviados, dadosEnviados.length, IPAdress, 9876);

            // A linha abaixo envia para o servidor os dadosEnviados na porta 9876
            clientSocket.send(enviarPacote);

            // Preparando um buffer para recebimento de informações
            byte[] bufferReceber = new byte[1024];
            DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);

            // Método bloqueante para aguardo de recebimento de mensagem através de UDP
            clientSocket.receive(pacoteRecebido);

            // Salvando os dados recebidos em uma string
            String informacao = new String(pacoteRecebido.getData(), pacoteRecebido.getOffset(), pacoteRecebido.getLength());

            // Imprimindo a informação
            System.out.println(informacao);

            // Fechando a conexão
            clientSocket.close();

        }
        catch (Exception e){
            
        }
        

    }
    
}
