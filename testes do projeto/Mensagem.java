import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Mensagem {

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
	public String remetente;
	public String destinatario;
	
	public Mensagem(int idMensagem, String conteudo, String remetente, String destinatario) {
		this.idMensagem = idMensagem;
		this.tipoMensagem = ids.get(idMensagem);
		this.conteudo = conteudo;
		this.remetente = remetente;
		this.destinatario = destinatario;
	}
	
	public JSONObject criarJSON() throws JSONException {
		
		JSONObject mensagemJSON = new JSONObject();
		
		mensagemJSON.put("id", this.idMensagem);
		mensagemJSON.put("tipoMsg", this.tipoMensagem);
		mensagemJSON.put("conteudo", this.conteudo);
		mensagemJSON.put("remetente", this.remetente);
		mensagemJSON.put("destinatario", this.destinatario);
		
		return mensagemJSON;
	}
	
	public Mensagem(JSONObject msgJSON) {
		this.idMensagem = msgJSON.getInt("id");
		this.tipoMensagem = msgJSON.getString("tipoMensagem");
		this.conteudo = msgJSON.getString("conteudo");
		this.remetente = msgJSON.getString("remetente");
		this.destinatario = msgJSON.getString("destinatario");
	}
	
}
