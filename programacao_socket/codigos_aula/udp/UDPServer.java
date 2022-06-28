package udp;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class UDPServer {

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

                // Criar buffer e pacote enviado para cliente
                byte[] bufferEnvio = new byte[1024];
                bufferEnvio = "Sou o servidor".getBytes();

                // Como saber qual o IP e porta do cliente para devolver a mensagem?
                // Essas informações já estão contidas no pacote recebido do cliente
                InetAddress endIP = pacoteRecebido.getAddress();
                int porta = pacoteRecebido.getPort();

                // Datagram de envio
                DatagramPacket sendPacket = new DatagramPacket(bufferEnvio, bufferEnvio.length, endIP, porta);

                // Enviando a mensagem
                serverSocket.send(sendPacket);

            }

        }
        catch (Exception e){

        }

    }
    
}
