import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.File;

public class user_1 {

    public static void main(String[] args) {

        // Pegando todos os arquivos de uma determinada pasta

        String[] nomes_arquivos;

        File f = new File("./pasta_arquivos");

        nomes_arquivos = f.list();

        for (String arquivo: nomes_arquivos){
            System.out.println(arquivo);
        }
        
        // Unificando todos os elementos do array em uma única string usando buffer
        // O nome de cada arquivo é separado por //

        StringBuffer bufferArquivos = new StringBuffer();

        for(String arquivo : nomes_arquivos) {
            bufferArquivos.append(arquivo);
            bufferArquivos.append("//");
        }

        String string_arquivos = bufferArquivos.toString();

        System.out.println(string_arquivos);

        // Enviando string com arquivos para
        
        try{
            
            // Endereço de IP
            InetAddress IPAdress = InetAddress.getByName("127.0.0.1");

            // Canal de comunicação não orientado à conexão
            DatagramSocket clientSocket = new DatagramSocket();

            // Buffer com mensagem que será enviada
            byte[] dadosEnviados = new byte[4096];
            dadosEnviados = string_arquivos.getBytes();
            
            // Criação de datagrama para envio de informações
            DatagramPacket enviarPacote = new DatagramPacket(dadosEnviados, dadosEnviados.length, IPAdress, 9876);

            // A linha abaixo envia para o servidor os dadosEnviados na porta 9876
            clientSocket.send(enviarPacote);

            // Fechando a conexão
            clientSocket.close();

        }
        catch (Exception e){
            
        }

    }
    
}
