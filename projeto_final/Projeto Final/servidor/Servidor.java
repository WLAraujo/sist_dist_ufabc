package servidor;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.Thread;
import java.util.Scanner;
import javax.sound.sampled.Port;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import org.json.JSONArray;
import java.util.ArrayList;

public class Servidor {

	public static void main(String[] args) {
		
		// Porta de escuta do servidor
		final int porta = 10098;	
		
		// Primeira coisa que o servidor faz � criar/conectar o banco de dados SQLite
		BancoServidor conexaoBancoServidor = new BancoServidor();
		conexaoBancoServidor.conectarBanco();
		
		// Execu��o da query que cria a tabela usada para armazenar os dados caso ela n�o exista
		conexaoBancoServidor.criaTabela();
		
		// Lan�ando thread respons�vel pelo envio de requisi��es ALIVE para o cliente a cada 30 segundos
        ThreadAlive threadAlive = new ThreadAlive(conexaoBancoServidor);
        threadAlive.start();
		
		// Ap�s realizar a conex�o com o banco de dados, o servidor realiza continuamente sua fun��o de escuta de mensagens
		// O processamento dessas mensagens � tercerizado via threads de atendimento
		try {
			
			// Definindo Datagram Socket na porta de escuta
			DatagramSocket serverSocket = new DatagramSocket(porta);
			
			// La�o while respons�vel pela continuidade da escuta
			while(true){
				
                // Declara��o de buffer de recebimento
                byte[] bufferReceber = new byte[2048];

                // Cria��o do pacote datagrama a ser recebido
                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber,bufferReceber.length);

                // M�todo bloqueante que espera pacote
                serverSocket.receive(pacoteRecebido);
                
                // Criando thread de atendimento para processamento da mensagem recebida
                ThreadAtendimento threadAtendimento = new ThreadAtendimento(pacoteRecebido, conexaoBancoServidor);
                threadAtendimento.start();
                
            }
        } 
		catch (Exception e){
			System.err.println(e.getMessage());
		}
		
		// O servidor tamb�m realiza a desconex�o do banco de dados quando o servi�o � encerrado
		conexaoBancoServidor.desconectarBanco();
		
	}
	
	// Classe respons�vel pelas opera��es sobre o banco de dados
	// Poss�vel conectar, desconectar e realizar queries
	public static class BancoServidor{
		
		// Vari�vel de conex�o
		private Connection conexao;
		
		// M�todo respons�vel pela conex�o com o banco de dados		
		public boolean conectarBanco () {
			try {
				String url = "jdbc:sqlite:bancoDados/bancoDados.db";
				this.conexao = DriverManager.getConnection(url);
			} 
			catch (SQLException e) {
				return false;
			}
			return true;			
		}
		
		// M�todo respons�vel por desconectar o banco de dados
		public boolean desconectarBanco () {
			try {
				if (this.conexao.isClosed() == false) {
					this.conexao.close();
				}
				return true;
			}
			catch (SQLException e) {
				return false;
			}
		}
		
		// M�todo respons�vel por criar a tabela com os dados de IP e arquivos, o comando s� � executado se a tabela n�o existir
		public void criaTabela() {
			try {
				Statement statement = this.conexao.createStatement();
				statement.executeUpdate("DROP TABLE IF EXISTS usuarios_arquivos;");
				statement.executeUpdate("CREATE TABLE usuarios_arquivos" +
								  "(" +
								  "ip TEXT NOT NULL," +
								  "porta TEXT NOT NULL," +
								  "arquivo TEXT NOT NULL," +
								  "flag_alive INTEGER NOT NULL" +
								  ");");
			}
			catch (SQLException e) {
			}
		}
		
		// M�todo respons�vel por realizar opera��o JOIN no banco de dados com um peer de entrada
		public void peerJoin(String ip, String porta, String arquivo) {
			try {
				// Adi��o de arquivos na tabela usuarios_arquivos
				Statement statement = this.conexao.createStatement();
				statement.execute("INSERT INTO usuarios_arquivos (" +
								  "ip," +
								  "porta," +
								  "arquivo," +
								  "flag_alive" +
								  ")" +
								  "VALUES( '" + ip + "' , '" + porta + "', '" + arquivo + "', 1);");
			}
			catch (SQLException e) {
			}
		}
		
		// M�todo respons�vel por realizar opera��o LEAVE no banco de dados com um peer de entrada
		public void peerLeave(String ip, String porta) {
			try {
				Statement statement = this.conexao.createStatement();
				// Remo��o da tabela de arquivos
				statement.execute("DELETE FROM usuarios_arquivos " +
						 		  "WHERE ip = '" + ip + "' AND porta = '" + porta + "';");
			}
			catch (SQLException e) {
			}
		}
		
		// M�todo respons�vel por realizar opera��o SEARCH no banco de dados com um arquivo de entrada, devolve uma lista de endere�os IP
		public ArrayList<String> peerSearch(String arquivo) {
			try {
				Statement statement = this.conexao.createStatement();
				ResultSet resultado;
				resultado = statement.executeQuery("SELECT DISTINCT ip, porta " +
												   "FROM usuarios_arquivos " +
												   "WHERE arquivo = '" + arquivo + "';");
				ArrayList<String> listaIPs = new ArrayList<String>();
				while (resultado.next()) {
					listaIPs.add(resultado.getString("ip") + ":" + resultado.getString("porta"));
				}
				return listaIPs;
			}
			catch (SQLException e) {
			}
			ArrayList<String> listaIPs = new ArrayList<String>();
			return listaIPs;
		}
		
		// M�todo respons�vel por realizar opera��o UPDATE quando um peer baixa um arquivo de outro peer
		public void peerUpdate(String ip, String porta, String arquivo) {
			try {
				Statement statement = this.conexao.createStatement();
				statement.execute("INSERT INTO usuarios_arquivos (" +
								  "ip," +
								  "porta," +
								  "arquivo," +
								  "flag_alive" +
								  ")" +
								  "VALUES( '" + ip + "' , '" + porta + "', '" + arquivo + "', 1);");
			}
			catch (SQLException e) {
			}
		}
		
		// M�todo disparado no in�cio de cada ciclo de alive para zerar o flag_alive, assim s� os peers que atualizarem sua condi��o se mantera�o ap�s limpeza
		public void zerarAlive() {
			try {
				Statement statement = this.conexao.createStatement();
				statement.executeUpdate("UPDATE usuarios_arquivos " +
								   		"SET flag_alive = 0 ;");
			}
			catch (SQLException e) {
			}
		}		
		
		// M�todo respons�vel por atualizar as flags de ALIVE quand recebe ALIVE_OK de cliente
		public void atualizarAlive(String IP, int porta) {
			try {
				Statement statement = this.conexao.createStatement();
				statement.execute("UPDATE usuarios_arquivos " +
						   			"SET flag_alive = 1 WHERE ip = '" + IP + "' AND porta = '" + porta + "';");
			}
			catch (SQLException e) {
			}
		}
		
		// M�todo respons�vel por fazer a limpeza dos peers que n�o atualizaram o flag_alive, tanto da tabela de alive quanto da de arquivos
		public ResultSet limparAlive() {
			try {
				Statement statement = this.conexao.createStatement();
				ResultSet resultado;
				resultado = statement.executeQuery("SELECT DISTINCT ip, porta, arquivo " +
						   							"FROM usuarios_arquivos " +
						   							"WHERE flag_alive = 0" +
						   ";");
				statement = this.conexao.createStatement();
				statement.execute("DELETE FROM usuarios_arquivos " +
		 		  		  		"WHERE flag_alive = 0;");
				return resultado;
			}
			catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return null;
		}
		
		// M�todo usado pela opera��o ALIVE para extrair lista de peers atuais
		public ResultSet aliveList() {
			try {
				Statement statement = this.conexao.createStatement();
				ResultSet resultado;
				resultado = statement.executeQuery("SELECT DISTINCT ip, porta, flag_alive " +
												   "FROM usuarios_arquivos " +
												   ";");
				return resultado;
			}
			catch (SQLException e) {
				System.err.println(e.getMessage());
			}
			return null;
		}
		
		// M�todo que devolve consulta com todos os dados atuais, usado em debug
		public void selectTudo() {
			try {
				Statement statement = this.conexao.createStatement();
				ResultSet resultado;
				resultado = statement.executeQuery("SELECT * FROM usuarios_arquivos;");
				while (resultado.next()) {
					System.out.println(resultado.getString("ip"));
					System.out.println(resultado.getString("porta"));
					System.out.println(resultado.getString("arquivo"));
				}
			}
			catch (SQLException e) {
				System.err.println(e.getMessage());
			}
		}
		
	}
	
	// Classe respons�vel por fazer o processamento/atendimento das mensagens recebidas pela thread de escuta
	public static class ThreadAtendimento extends Thread{
		
		// Criar vari�vel da classe que cont�m datagrama usado no processamento
		private DatagramPacket pacote;
		private BancoServidor conexaoBanco;
		
		// M�todo construtor da classe que simplesmente instancia o pacote tratado na thread atual e uma conex�o com o BD
		public ThreadAtendimento(DatagramPacket pacote, BancoServidor conexaoBanco) {
			this.pacote = pacote;
			this.conexaoBanco = conexaoBanco;
		}
		
		// M�todo run da classe
		public void run() {
			
			// Primeira coisa feita � criar uma string com base no cont�udo do pacote recebido
			String msgString = new String(this.pacote.getData(), this.pacote.getOffset(), this.pacote.getLength());
			
			// Depois convertemos essa string mensagem em um objeto JSON
		    JSONObject msgJSON = new JSONObject(msgString);
		    
		    // Por fim, convertemos o objeto JSON criado em um objeto da classe mensagem
		    Mensagem msg = new Mensagem(msgJSON);
		    
		    //Pegando o IP do usu�rio e porta de envio como String
	    	InetAddress IPend = pacote.getAddress();
	    	String IPstring = IPend.toString().replace("/", "");  
            String porta = Integer.toString(pacote.getPort());
		    
		    // Requisi��o JOIN - ID 1
		    if (msg.idMensagem == 1) {
                
        		// Para cada arquivo adicionamos uma linha na tabela (IP | PORTA | ARQUIVO | ALIVE)
        		String[] lista_arquivos = msg.conteudo.split("//");
                for (String arquivo: lista_arquivos){
                    this.conexaoBanco.peerJoin(IPstring, porta, arquivo);
                }
                
                // Enviando mensagem JOIN_OK para o cliente, necess�rio criar buffer, objeto mensagem, JSON da mensagem e serializar em string
                byte[] bufferEnvio = new byte[2048];
                Mensagem resposta = new Mensagem(1, "JOIN_OK");
                JSONObject JSONresposta = resposta.criarJSON();
                bufferEnvio = JSONresposta.toString().getBytes();
                
                // Criando packet que cont�m a resposta e socket exclusivo para envio da resposta, assim n�o oneramos o socket usado na escuta
                try {
                	DatagramPacket sendResposta = new DatagramPacket(bufferEnvio, bufferEnvio.length, IPend, pacote.getPort());
        			DatagramSocket respostaSocket = new DatagramSocket();
					respostaSocket.send(sendResposta);
					respostaSocket.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
                
                // Tratamento da lista de arquivos
                String stringArquivosPrint = msg.conteudo.replace("//", ",");
            	stringArquivosPrint = stringArquivosPrint.substring(0, stringArquivosPrint.length()-1);
                
                // Mensagem de confirma��o
                System.out.println("Peer " + IPstring + ":" + porta + " adicionado com os arquivos " + msg.conteudo);

		    }
		    
		    // Requisi��o LEAVE - ID 2
		    if (msg.idMensagem == 2) {
				
				// Parseando o conte�do da mensagem
		    	String[] mensagemSearch = msg.conteudo.split("//");
		    	String msgLEAVE = mensagemSearch[0];
		    	String portaPeer = mensagemSearch[1];

                // Chamando m�todo que remove registros desse IP da tabela do banco de dados
		    	this.conexaoBanco.peerLeave(IPstring, String.valueOf(portaPeer));   
                
                // Enviando mensagem ALIVE_OK para o cliente, necess�rio criar buffer, objeto mensagem, JSON da mensagem e serializar em string
                byte[] bufferEnvio = new byte[2048];
                Mensagem resposta = new Mensagem(2, "LEAVE_OK");
                JSONObject JSONresposta = resposta.criarJSON();
                bufferEnvio = JSONresposta.toString().getBytes();
                
                // Criando packet que cont�m a resposta e socket exclusivo para envio da resposta, assim n�o oneramos o socket usado na escuta
                try {
                	DatagramPacket sendResposta = new DatagramPacket(bufferEnvio, bufferEnvio.length, IPend, pacote.getPort());
        			DatagramSocket respostaSocket = new DatagramSocket();
					respostaSocket.send(sendResposta);
					respostaSocket.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
                
		    }
		    
		    // Requisi��o SEARCH - ID 3
		    if (msg.idMensagem == 3) {
		    	
		    	// Parseando o conte�do da mensagem
		    	String[] mensagemSearch = msg.conteudo.split("//");
		    	String arquivoBuscado = mensagemSearch[0];
		    	String portaPeer = mensagemSearch[1];
		    	
		    	
                // Executando m�todo que devolve como array list peers com o arquivo desejado
		    	ArrayList<String> listaIPs = this.conexaoBanco.peerSearch(arquivoBuscado);
		    	
                // Enviando mensagem com os peers que possuem o arquivo 
                byte[] bufferEnvio = new byte[2048];
                Mensagem resposta = new Mensagem(3, listaIPs);
                JSONObject JSONresposta = resposta.criarJSONLista();
                bufferEnvio = JSONresposta.toString().getBytes();
                
                // Criando packet que cont�m a resposta e socket exclusivo para envio da resposta, assim n�o oneramos o socket usado na escuta
                try {
                	DatagramPacket sendResposta = new DatagramPacket(bufferEnvio, bufferEnvio.length, IPend, pacote.getPort());
        			DatagramSocket respostaSocket = new DatagramSocket();
					respostaSocket.send(sendResposta);
					respostaSocket.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
                
                System.out.println("Peer " + IPend + ":" + portaPeer + " solicitou arquivo " + arquivoBuscado);
                
		    }
		    
		    // Requisi��o UPDATE - ID 4
		    if (msg.idMensagem == 4) {
                
                // Parseando o conte�do da mensagem
		    	String[] mensagemSearch = msg.conteudo.split("//");
		    	String arquivoNovo = mensagemSearch[0];
		    	String portaPeer = mensagemSearch[1];
		    	
		    	// Chamando m�todo que realizar� UPDATE na tabela de arquivos
		    	this.conexaoBanco.peerUpdate(IPstring, portaPeer, arquivoNovo);
		    	
                // Enviando mensagem com os peers que possuem o arquivo 
                byte[] bufferEnvio = new byte[2048];
                Mensagem resposta = new Mensagem(4, "UPDATE_OK");
                JSONObject JSONresposta = resposta.criarJSONLista();
                bufferEnvio = JSONresposta.toString().getBytes();
                
                // Criando packet que cont�m a resposta e socket exclusivo para envio da resposta, assim n�o oneramos o socket usado na escuta
                try {
                	DatagramPacket sendResposta = new DatagramPacket(bufferEnvio, bufferEnvio.length, IPend, pacote.getPort());
        			DatagramSocket respostaSocket = new DatagramSocket();
					respostaSocket.send(sendResposta);
					respostaSocket.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
                
		    }
		    
			// Requisi��o ALIVE (No caso ALIVE_OK) - ID 5
		    if (msg.idMensagem == 5) {
		    	
		    	// Chamando m�todo que realizar� UPDATE na tabela de arquivos
		    	this.conexaoBanco.atualizarAlive(IPstring, pacote.getPort());
                
		    }
		    
		}
		
	}
	
	// Essa classe ser� respons�vel por manter a thread que realiza o processo de checar se os peers ainda est�o vivos (ALIVE)
	public static class ThreadAlive extends Thread{
		// Criar vari�vel da classe que cont�m datagrama usado no processamento
		private BancoServidor conexaoBanco;
				
		// M�todo construtor da classe que simplesmente instancia o pacote tratado na thread atual e uma conex�o com o BD
		public ThreadAlive(BancoServidor conexaoBanco) {
			this.conexaoBanco = conexaoBanco;
		}		
		
		public void run() {

			while(true) {

				//  Enviando uma mensagem para cada peer, aguardando os 30 segundos a resposta e verificando como est� a tabela de �ltimo alive
				try { 
					
					// Criando mensagem padr�o que ser� enviada para todos os peers
		            byte[] bufferEnvio = new byte[2048];
		            Mensagem msgAlive = new Mensagem(5, "ALIVE");
		            JSONObject JSONalive = msgAlive.criarJSON();
		            bufferEnvio = JSONalive.toString().getBytes();
					
					// Pegando do banco de dados IPs para qual devemos enviar mensagem
					ResultSet resultado = this.conexaoBanco.aliveList();
					
					// Zerando os alives atuais
					this.conexaoBanco.zerarAlive();
					
					// Iterando por cada peer e enviando a mensagem
					while (resultado.next()) {
						InetAddress IPend = InetAddress.getByName(resultado.getString("ip"));
						int portaAlive = Integer.parseInt(resultado.getString("porta"));
						DatagramPacket sendAlive = new DatagramPacket(bufferEnvio, bufferEnvio.length, IPend, portaAlive);
						DatagramSocket aliveSocket = new DatagramSocket();
	        			aliveSocket.send(sendAlive);
	        			aliveSocket.close();
					}
					
					// A thread dorme por 30 segundos
					Thread.sleep(30000);
					
					// Limpeza da tabela de alive e obter quais os peers deletados
					resultado = this.conexaoBanco.limparAlive();
					
					// String que apresentar� peers deletados
					String peersMortos = "";
					
					// Vari�veis que salvam �ltimo ip e porta mostrados
					String ultimoIP = "";
					String ultimaPorta = "";
					
					// Imprimindo na tela quais peers foram deletados
					while (resultado.next()) {
						if (ultimoIP.equals(resultado.getString("ip")) && ultimaPorta.equals(resultado.getString("porta"))) {
							peersMortos = peersMortos + resultado.getString("arquivo") + " ";
						}
						else {
							peersMortos = peersMortos + "\n" + "Peer " + resultado.getString("ip") + ":" + resultado.getString("porta") + 
										  " morto. Eliminando seus arquivos " + resultado.getString("arquivo");
							ultimoIP = resultado.getString("ip");
							ultimaPorta = resultado.getString("porta");
						}
					}
					
					if (!peersMortos.equals("")){
						System.out.println(peersMortos);
					}
					
				}
				catch (Exception e) {
				}
				
				
			}
			
		}
		
		
	}
		
}
