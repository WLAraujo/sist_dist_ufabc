import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class envio_udp {

    public static void main(String[] args) {

        try{
            
            // Endereço de IP do servidor
            InetAddress IPAdress = InetAddress.getByName("172.29.207.255");

            // Canal de comunicação não orientado à conexão
            DatagramSocket clientSocket = new DatagramSocket();

            // Criando mensagem
            Mensagem msg = Mensagem(1, "Mensagem de teste");
            JSONObject jsonmsg = msg.criarJSON();

            // Buffer com mensagem que será enviada
            byte[] dadosEnviados = new byte[1024];
            dadosEnviados = jsonmsg.toString().getBytes();
            
            // Criação de datagrama para envio de informações
            DatagramPacket enviarPacote = new DatagramPacket(dadosEnviados, dadosEnviados.length, IPAdress, 10098);

            // A linha abaixo envia para o servidor os dadosEnviados na porta 9876
            clientSocket.send(enviarPacote);

            // Fechando a conexão
            clientSocket.close();

        }
        catch (Exception e){
            
        }

    }

    public static class Mensagem {

        private static final Map<Integer, String> ids;
        
        static {
            Map<Integer, String> idsMap = new HashMap<Integer,String>();
            idsMap.put(1, "JOIN");
            idsMap.put(2, "LEAVE");
            idsMap.put(3, "SEARCH");
            idsMap.put(4, "UPDATE");
            idsMap.put(5, "ALIVE");
            ids = Collections.unmodifiableMap(idsMap);
        }
        
        public int idMensagem;
        public String tipoMensagem;
        public String conteudo;
        
        public Mensagem(int idMensagem, String conteudo) {
            this.idMensagem = idMensagem;
            this.tipoMensagem = ids.get(idMensagem);
            this.conteudo = conteudo;
        }
        
        public JSONObject criarJSON() throws JSONException {
            
            JSONObject mensagemJSON = new JSONObject();
            
            mensagemJSON.put("id", this.idMensagem);
            mensagemJSON.put("tipoMsg", this.tipoMensagem);
            mensagemJSON.put("conteudo", this.conteudo);
            
            return mensagemJSON;
        }
        
        public Mensagem(JSONObject msgJSON) {
            this.idMensagem = msgJSON.getInt("id");
            this.tipoMensagem = msgJSON.getString("tipoMensagem");
            this.conteudo = msgJSON.getString("conteudo");
        }
        
    }
    
}
