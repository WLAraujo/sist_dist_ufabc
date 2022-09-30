package servidor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
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
        idsMap.put(6, "DOWNLOAD");
        idsMap.put(7, "DOWNLOAD_NEGADO");
        ids = Collections.unmodifiableMap(idsMap);
    }
	
	public int idMensagem;
	public String tipoMensagem;
	public String conteudo;
	public ArrayList<String> conteudoLista;
	
	public Mensagem(int idMensagem, String conteudo) {
		this.idMensagem = idMensagem;
		this.tipoMensagem = ids.get(idMensagem);
		this.conteudo = conteudo;
	}
	
	public Mensagem(int idMensagem, ArrayList<String> conteudoLista) {
		this.idMensagem = idMensagem;
		this.tipoMensagem = ids.get(idMensagem);
		this.conteudoLista = conteudoLista;
	}
	
	public JSONObject criarJSON() throws JSONException {
		
		JSONObject mensagemJSON = new JSONObject();
		
		mensagemJSON.put("id", this.idMensagem);
		mensagemJSON.put("tipoMensagem", this.tipoMensagem);
		mensagemJSON.put("conteudo", this.conteudo);
		
		return mensagemJSON;
	}
	
	public JSONObject criarJSONLista() throws JSONException {
		
		JSONArray IPs = new JSONArray(this.conteudoLista);
		
		JSONObject mensagemJSON = new JSONObject();
		
		mensagemJSON.put("id", this.idMensagem);
		mensagemJSON.put("tipoMensagem", this.tipoMensagem);
		mensagemJSON.put("conteudoLista", IPs);
		
		return mensagemJSON;
	}
	
	public Mensagem(JSONObject msgJSON) {
		this.idMensagem = msgJSON.getInt("id");
		this.tipoMensagem = msgJSON.getString("tipoMensagem");
		if (msgJSON.has("conteudo")) {
			this.conteudo = msgJSON.getString("conteudo");
		}
		if(msgJSON.has("conteudoLista")) {
			this.conteudoLista = new ArrayList<String>();
			JSONArray arrayConteudo = msgJSON.getJSONArray("conteudoLista");
			if (arrayConteudo != null) { 
				for (int i = 0; i < arrayConteudo.length(); i++){ 
					this.conteudoLista.add(arrayConteudo.getString(i));
				} 
			}
			else {
				this.conteudoLista = new ArrayList<String>();
			}
		}	
	}
	
}
