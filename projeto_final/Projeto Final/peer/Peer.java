package peer;

import org.json.JSONObject;
import java.lang.Thread;
import java.util.Scanner;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

// Classe que executa o cliente
public class Peer {
	
	static boolean flagJoin = false;
	static int portaPeer = -1;
	static String pastaJoin = null;
	static ThreadDownload threadDownload = null;
	static ThreadAlive threadAlive = null;
	static ServerSocket socketServidor;
	static String arquivoUltimoSearch;
	static ArrayList<String> listaUltimoSearch;
	static boolean flagAlive = false; 

	public static void main(String[] args) {
		
		try {
			
			// Mensagem de boas vindas
			System.out.println("----------------------------------------------------------------------------------");
			System.out.println("Bem vindo ao Wapster, sistema de transferência de arquivos");
			System.out.println("----------------------------------------------------------------------------------");
			
			// Buffer de scanners
        	Scanner opcaoScanner = new Scanner(System.in);
        	Scanner pastaScanner = new Scanner(System.in);
        	Scanner arquivoScanner = new Scanner(System.in);
        	Scanner IPScanner = new Scanner(System.in);
        	Scanner portaScanner = new Scanner(System.in);
        	
        	// Declarando variáveis de thread
        	ThreadEnvio threadEnvio;

			// Laço que ficará rodando esperando uma ordem do usuário que deve ser inserida na linha de comando
            while (true) {
            	
            	// Opções para o usuário
                System.out.println("Selecione uma das opções abaixo:");
                System.out.println("1-) Requisição JOIN para o servidor");
                System.out.println("2-) Requisição LEAVE para o servidor");
                System.out.println("3-) Requisição SEARCH para o servidor");
                System.out.println("4-) Requisição DOWLOAD para outro cliente");
                System.out.println("5-) Sair do programa");
                System.out.println("----------------------------------------------------------------------------------");

                int entrada = 0;
                
                // Captura de entrada
                try {
                entrada = opcaoScanner.nextInt();
                System.out.println("----------------------------------------------------------------------------------");
                }
                catch (NumberFormatException nfe){
                	System.out.println(nfe.getMessage());
                }

                // entrada 1 - Requisição de JOIN para o servidor
                if (entrada == 1) {
                	
                	if (flagJoin == true) {
           
                		// Não dá para fazer JOIN de duas pastas de uma mesma vez
                		System.out.println("Aviso: Você já fez o JOIN usando uma pasta, caso queira adicionar outra pasta ou mais arquivos "
    							+ "faça primeiro um LEAVE e depois outro JOIN!!!");
						System.out.println("----------------------------------------------------------------------------------");
                		
                	}
                	else {
                		// Criando objeto arquivo
                    	File pasta;
                        
                        // Laço que fica iterando até que o usuário digite uma pasta válida
                        while (true) {
                        	
                        	// Escolher pasta com arquivos
                        	System.out.println("Digite o nome da pasta com os arquivos que você gostaria de atualizar no servidor:");
                        	
                        	// Capturando pasta
                        	pastaJoin = pastaScanner.nextLine();
                        	System.out.println("----------------------------------------------------------------------------------");
                        	
                        	// Atribuindo valor ao objeto file
                        	pasta = new File(pastaJoin);
                        	
                        	// Verificando se arquivo existe
                        	if (pasta.isDirectory()) {
                        		break;
                        	}
                        	else {
                        		System.out.println("Erro: Endereço passado é inválida, digite um válido!!!");
                        		System.out.println("----------------------------------------------------------------------------------");
                        	}
                        	
                        }
                        
                        // Lançando thread responsável pelo envio da mensagem ao servidor
                        // Perceba que só criamos thread paralela após termos certeza que a pasta é válida
                        // É no lançamento dessa thread que a thread de download também é lançada
                        threadEnvio = new ThreadEnvio(entrada, pasta);
                        threadEnvio.start();
                	}      	
                    
                }
                
				// entrada 2 - Requisição de LEAVE para o servidor
                if(entrada == 2) {
                	
                	if(flagJoin == false) {
                		System.out.println("----------------------------------------------------------------------------------");
                		System.out.println("Erro: Não é possível fazer LEAVE sem que JOIN tenha sido realizado!!!");
                		System.out.println("----------------------------------------------------------------------------------");
                	}
                	else {
                		// Lançando thread responsável pelo envio da mensagem ao servidor
                        // Perceba que só criamos thread paralela após termos certeza que o peer já fez JOIN
                        threadEnvio = new ThreadEnvio(entrada);
                        threadEnvio.start();
                        
                        // Paralelamente ao lançamento da mensagem, a threadDownload também é interrompida para não haver
                        // conexões TCP após o peer indicar o LEAVE
                        socketServidor.close();
                	}
                	
                }
                
                // entrada 3 - Requisição de SEARCH para o servidor
                if (entrada == 3) {
                    
                   String arquivoSearch = new String();
                   
                   while(true) {
                	   
                	   // Escolher arquivo a ser buscado
                       System.out.println("Digite o nome do arquivo que você gostaria de buscar no servidor:");
                	   
                	   // Capturando pasta
                	   arquivoSearch = arquivoScanner.nextLine();
                	   System.out.println("----------------------------------------------------------------------------------");
                	   
                	   // Verificando se o arquivo capturado não é composto só por espaços em branco
                	   if (!arquivoSearch.trim().isEmpty()) {
                		   break;
                	   }
                	   else {
                		   System.out.println("Erro: Digite um nome que não seja só espaços em branco!!!");
                		   System.out.println("----------------------------------------------------------------------------------");
                	   }
                	   
                   }
                    
                   // Lançando thread responsável pelo envio da mensagem ao servidor
                   // Perceba que só criamos thread paralela após termos certeza que a pasta é válida
                   threadEnvio = new ThreadEnvio(entrada, arquivoSearch);
                   threadEnvio.start();
                    
                   arquivoUltimoSearch = arquivoSearch;
                   
                }

                // entrada 4 - Requisição de DOWNLOAD para outro cliente
                if (entrada == 4) {
                	
                	// Exige-se que o peer já tenha feito JOIN para que a pasta de destino de download já exista
                	if(flagJoin == false) {
                		System.out.println("----------------------------------------------------------------------------------");
                		System.out.println("Erro: Não é possível fazer DOWNLOAD sem que JOIN tenha sido realizado!!!");
                		System.out.println("----------------------------------------------------------------------------------");                		
                	}
                	else {
	                	// Pegando nome do arquivo a ser requisitado
	                	System.out.println("Digite o arquivo que você gostaria de baixar:");
	                	String arquivoDownload = arquivoScanner.nextLine();
	                	
	                	if (arquivoDownload.equals(arquivoUltimoSearch)) {
	                		// Pegando IP qual o cliente deseja se conectar
		                	System.out.println("Digite o IP do peer que deseja-se conectar");
		                	String peerIPDestino = IPScanner.nextLine();
		                	
		                	// Pegando Porta qual o cliente deseja se conectar
		                	System.out.println("Digite a porta do peer que deseja-se conectar");
		                	String peerPortaDestino = portaScanner.nextLine();
		                	
		                	// Verificamos se o peer escolhido realmente está na lista resultante do search, caso não rejeitamos o DOWNLOAD
		                	if (listaUltimoSearch.contains(peerIPDestino + ":" + peerPortaDestino)) {
		                		// Chamando Thread de envio para conexão com outro peer
		                		threadEnvio = new ThreadEnvio(entrada, arquivoDownload, peerIPDestino, peerPortaDestino);
		                		threadEnvio.start();
		                	}
		                	else {
		                		System.out.println("----------------------------------------------------------------------------------");
		                		System.out.println("Erro: Peer digitado não faz parte da lista resultante do SEARCH!!!");
		                		System.out.println("----------------------------------------------------------------------------------");
		                	}
		                	
		                	
	                	}
	                	else {
	                		System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Erro: Antes de realizar DOWNLOAD desse arquivo faça um SEARCH para ele!!!");
	                		System.out.println("----------------------------------------------------------------------------------");  
	                	}
	                
                	}
                	
                }
                
                if (entrada == 5) {
                	if(flagJoin == true) {
                		System.out.println("Aviso: Por favor, faça um LEAVE antes de desligar o Wapster");
                	}
                	else {
                		break;
                	}
                }

            }
            
            // Fechando alguns recursos
            opcaoScanner.close();
            pastaScanner.close();
            arquivoScanner.close();
            IPScanner.close();
            portaScanner.close();
            
            
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}

	}
	
	// Classe que possui thread de processamento dos comandos do usuário, especificamente JOIN, LEAVE, SEARCH e DOWNLOAD.
	public static class ThreadEnvio extends Thread{
		
		private int tipoEnvio;
		private File pasta;
		private String arquivo;
		private String IP;
		private String porta;
		
		// Métodos construtores da classe que instanciam e enviam mensagens
		
		// Usado na requisição JOIN
		public ThreadEnvio(int tipoEnvio, File pasta) {
			this.tipoEnvio = tipoEnvio;
			this.pasta = pasta;
			this.IP = null;
			this.porta = null;
		}
		
		
		// Usado na requisição LEAVE
		public ThreadEnvio(int tipoEnvio) {
			this.tipoEnvio = tipoEnvio;
			this.pasta = null;
			this.arquivo = null;
			this.IP = null;
			this.porta = null;
		}
		
		// Usado na requisição SEARCH
		public ThreadEnvio(int tipoEnvio, String arquivo) {
			this.tipoEnvio = tipoEnvio;
			this.pasta = null;
			this.arquivo = arquivo;
			this.IP = null;
			this.porta = null;
		}
		
		// Usado na requisição DOWNLOAD
		public ThreadEnvio(int tipoEnvio, String arquivo, String IP, String porta) {
			this.tipoEnvio = tipoEnvio;
			this.pasta = null;
			this.arquivo = arquivo;
			this.IP = IP;
			this.porta = porta;
		}
		
		public void run() {
			
			try {
				
				// Endereço de IP do servidor
				final InetAddress ServerAdress = InetAddress.getByName("127.0.0.1");
				
				// Canal de comunicação não orientado à conexão para comunicação com servidor por UDP
				DatagramSocket clientSocket;
				if (portaPeer == -1 || tipoEnvio == 2 || tipoEnvio == 3 || tipoEnvio == 4) {
					clientSocket = new DatagramSocket();
				}
				else {
					clientSocket = new DatagramSocket(portaPeer);
				}
				
				// Setando timeout padrão de 15 segundos para socket não ficar esperando mensagem para sempre
				clientSocket.setSoTimeout(15000);
			
				// Escolhendo qual mensagem será enviada

				// Se JOIN (1)
				if (this.tipoEnvio == 1) {
					
					// Criando lista dos arquivos da pasta
	                String[] arquivos = this.pasta.list();		    
	                
	                // Criando string com nome dos arquivos separados por "//"
	                StringBuffer bufferArquivos = new StringBuffer();
	                for(String arquivo : arquivos) {
	                    bufferArquivos.append(arquivo);
	                    bufferArquivos.append("//");
	                }    
	                String stringArquivos = bufferArquivos.toString();
	                if (stringArquivos.length() == 0) {
	                	stringArquivos = " ";
	                }
	                
	                // Compondo mensagem a ser enviada para o servidor
	                Mensagem msg = new Mensagem(1, stringArquivos);
	                JSONObject jsonmsg = msg.criarJSON();
	                
	                // Buffer com mensagem que será enviada
	                byte[] bufferMsg = new byte[jsonmsg.toString().length()];
	                bufferMsg = jsonmsg.toString().getBytes();
	                
	                // Criação de datagrama para envio de informações
	                DatagramPacket pacoteMsg = new DatagramPacket(bufferMsg, bufferMsg.length, ServerAdress, 10098);  
		            
	                // Buffer e datagrama para receber mensagem
	                byte[] bufferReceber = new byte[1024];
	                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);
	                
	                // Criando algumas strings com informações importantes
			    	String IPstring = new String();
		            String porta = new String();
	                
	                // Flag de pacote recebido 
	                boolean flagPacote = false;
	                
	                // Loop para fazer retry de envio e esperar JOIN_OK
	                for (int contadorTentativas = 0; contadorTentativas <3; contadorTentativas++) {
	                	if (flagPacote == true) {
	                		break;
	                	}
	                	try {
	                		clientSocket.send(pacoteMsg);
	                		clientSocket.receive(pacoteRecebido);
	                		porta = Integer.toString(clientSocket.getLocalPort());
	                		IPstring = pacoteRecebido.getAddress().toString();
	                		flagPacote = true;
	                	}
	                	catch(SocketTimeoutException e) {
	                		System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Aviso: JOIN sem sucesso, tentando novamente");
	                		System.out.println("----------------------------------------------------------------------------------");
	                	}
	                }               
	                
	                // Fechando socket
	                clientSocket.close();
	                
	                // flagPacote faz o controle se algum pacote foi recebido
	                if(flagPacote == false) {
	                	System.out.println("----------------------------------------------------------------------------------");
	                	System.out.println("Aviso: Não foi possível realizar JOIN, tente novamente mais tarde");
	                	System.out.println("----------------------------------------------------------------------------------");
	                	return;	                	
	                }

	                // Transformando conteúdo do pacote recebido em String, depois em JSON e depois em objeto mensagem
	                String stringResposta = new String(pacoteRecebido.getData(), pacoteRecebido.getOffset(), pacoteRecebido.getLength());
	                JSONObject JSONReposta = new JSONObject(stringResposta);
	                Mensagem msgResposta = new Mensagem(JSONReposta);
	                
	                // Verificando se o conteúdo da mensagem recebida foi um JOIN_OK
	                // Caso seja apresenta mensagem e já lança thread de dowload na mesma porta que foi usada
	                if (msgResposta.conteudo.equals("JOIN_OK")) {
	                	flagJoin = true;
	                	flagAlive = true;
	                	String stringArquivosPrint = stringArquivos.replace("//", ",");
	                	stringArquivosPrint = stringArquivosPrint.substring(0, stringArquivosPrint.length()-1);
	                	System.out.println("----------------------------------------------------------------------------------");
	                	System.out.println("Sou o peer " + IPstring + ":" + porta + " com os arquivos " + stringArquivosPrint);
	                	System.out.println("----------------------------------------------------------------------------------");
	                	portaPeer = Integer.valueOf(porta);
	                	threadDownload = new ThreadDownload(this.pasta, portaPeer);
	                	threadDownload.start();
	                	threadAlive = new ThreadAlive(portaPeer);
	                	threadAlive.start();
	                }
	                         
				}
				
				// Se LEAVE(2)
				if (this.tipoEnvio == 2) {
					
	                // Composição de mensagem a ser enviada
					String stringMSG = "LEAVE" + "//" + Integer.toString(portaPeer);
	                Mensagem msg = new Mensagem(2, stringMSG);
	                JSONObject jsonmsg = msg.criarJSON();
	                
	                // Buffer com mensagem que será enviada
	                byte[] bufferMsg = new byte[jsonmsg.toString().length()];
	                bufferMsg = jsonmsg.toString().getBytes();
	                
	                // Criação de datagrama para envio de informações
	                DatagramPacket pacoteMsg = new DatagramPacket(bufferMsg, bufferMsg.length, ServerAdress, 10098);  
	                
	                // Buffer e datagrama para receber mensagem
	                byte[] bufferReceber = new byte[1024];
	                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);
	                
	                // Flag de pacote recebido 
	                boolean flagPacote = false;
	                
					// Loop para fazer retry de envio e esperar LEAVE_OK
	                for (int contadorTentativas = 0; contadorTentativas <3; contadorTentativas++) {
	                	if (flagPacote == true) {
	                		break;
	                	}
	                	try {
	                		clientSocket.send(pacoteMsg);
	                		clientSocket.receive(pacoteRecebido);
	                		flagPacote = true;
	                	}
	                	catch(SocketTimeoutException e) {
	                		System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Aviso: LEAVE sem sucesso, tentando novamente");
	                		System.out.println("----------------------------------------------------------------------------------");
	                	}
	                }
	                
	                // flagPacote faz o controle se algum pacote foi recebido
	                if(flagPacote == false) {
	                	System.out.println("----------------------------------------------------------------------------------");
	                	System.out.println("Aviso: Não foi possível realizar LEAVE, tente novamente mais tarde");
	                	System.out.println("----------------------------------------------------------------------------------");
	                	return;	                	
	                }
	                
					// Valores das flags
					flagJoin = false;
					flagAlive = false;
					
					// Quando LEAVE é realizado com sucesso
					System.out.println("----------------------------------------------------------------------------------");
	                System.out.println("Aviso: LEAVE realizado com sucesso");
	                System.out.println("----------------------------------------------------------------------------------");
	                               
					// Fechando socket
	                clientSocket.close();
					
					// Liberando outras threads
					threadDownload.interrupt();
                    threadDownload = null;
				}

				// Se SEARCH (3)
				if (this.tipoEnvio == 3) {
					
					// Composição de mensagem a ser enviada
					String stringMSG = this.arquivo + "//" + Integer.toString(portaPeer);
					
					// Compondo mensagem a ser enviada para o servidor
	                Mensagem msg = new Mensagem(3, stringMSG);
	                JSONObject jsonmsg = msg.criarJSON();
	                
	                // Buffer com mensagem que será enviada
	                byte[] bufferMsg = new byte[jsonmsg.toString().length()];
	                bufferMsg = jsonmsg.toString().getBytes();
	                
	                // Criação de datagrama para envio de informações
	                DatagramPacket pacoteMsg = new DatagramPacket(bufferMsg, bufferMsg.length, ServerAdress, 10098);  
	                
	                // Buffer e datagrama para receber mensagem
	                byte[] bufferReceber = new byte[1024];
	                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);
	                
	                // Flag de pacote recebido 
	                boolean flagPacote = false;
	                
	                // Loop para fazer retry de envio e esperar resposta do SEARCH
	                for (int contadorTentativas = 0; contadorTentativas <3; contadorTentativas++) {
	                	if (flagPacote == true) {
	                		break;
	                	}
	                	try {
	                		clientSocket.send(pacoteMsg);
	                		clientSocket.receive(pacoteRecebido);
	                		flagPacote = true;
	                	}
	                	catch(SocketTimeoutException e) {
	                		System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Aviso: SEARCH sem sucesso, tentando novamente");
	                		System.out.println("----------------------------------------------------------------------------------");
	                	}
	                }
	                
	                // Fechando socket
	                clientSocket.close();
	                
	                // flagPacote faz o controle se algum pacote foi recebido
	                if(flagPacote == false) {
	                	System.out.println("----------------------------------------------------------------------------------");
	                	System.out.println("Aviso: Não foi possível realizar SEARCH, tente novamente mais tarde");
	                	System.out.println("----------------------------------------------------------------------------------");
	                	return;	                	
	                }
	                
	                // Transformando conteúdo do pacote recebido em String, depois em JSON e depois em objeto mensagem
	                String stringResposta = new String(pacoteRecebido.getData(), pacoteRecebido.getOffset(), pacoteRecebido.getLength());
	                JSONObject JSONReposta = new JSONObject(stringResposta);
	    			Mensagem msgResposta = new Mensagem(JSONReposta);
	                
	                // Verificando se o conteúdo da mensagem recebida foi um JOIN_OK
	                if (msgResposta.tipoMensagem.equals("SEARCH")) {
	                	if(msgResposta.conteudoLista == null) {
	                		System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Aviso: Não existem peers com '" + this.arquivo + "', verifique grafia e/ou extensão!");
	                		System.out.println("----------------------------------------------------------------------------------");
	                	}
	                	else {
	                		System.out.println("Peers com arquivo '" + this.arquivo + "' :");
	                		ArrayList<String> listaIPs = msgResposta.conteudoLista;
	                		listaUltimoSearch = listaIPs;
	                		for (String IPPorta : listaIPs) {
	                			System.out.println(IPPorta);
	                		}
	                	}
	                }
	                
	                System.out.println("----------------------------------------------------------------------------------");
					
				}
				
				// Se DOWNLOAD (4)
				if (this.tipoEnvio == 4) {
					
					boolean flagDownloadSucesso = false;
					int contadorTentativas = 0;
					String primeiroIP = this.IP;
					String primeiraPorta = this.porta;
					
					while (flagDownloadSucesso != true && contadorTentativas < 5) {
						
						// Criando conexão TCP com outro peer
						Socket socketRequisicao = new Socket(this.IP, Integer.valueOf(this.porta));
						
						// Criação de stream de saída (escrita) de informações pelo socket e writer que escreve nessa stream
				        OutputStream streamSaida =  socketRequisicao.getOutputStream();
				        DataOutputStream writer = new DataOutputStream(streamSaida);
				        
				        // Criação de stream de entrada e reader de informações do socketCliente
		                InputStreamReader streamEntrada =  new InputStreamReader(socketRequisicao.getInputStream());
		                BufferedReader reader = new BufferedReader(streamEntrada);
						
			            // Enviando nome do arquivo
			            writer.writeBytes(this.arquivo + "\n");     
			            
			            // Aguardando mensagem de resposta do peerServidor
			            String msgString = reader.readLine();
			            
			            // Criando objeto mensagem a partir de JSON
		                JSONObject JSONReposta = new JSONObject(msgString);
		    			Mensagem msgResposta = new Mensagem(JSONReposta);
		    			
		    			// Verificando se a requisição DOWNLOAD foi negada ou não
		    			if (msgResposta.idMensagem == 6) {
							
							// Mensagem para desbloquear peer servidor
		    				writer.writeBytes("Keep on" + "\n");  
		    				
		    				// Compondo string que possui caminho que o arquivo vai ficar
	    					String caminhoString = pastaJoin + "/" + this.arquivo;					
		    					
		    				// Criando objeto que após dowload será nosso arquivo
		                    File arquivoBaixado = new File(caminhoString);
							
							// Criação de stream para recebimento dos dados do arquivo
		    				DataInputStream streamArquivo = new DataInputStream(socketRequisicao.getInputStream());
		                        
		                    // Criando stream de saída para escrever da stream com dados recebidos do peer para o arquivo em si
	                        FileOutputStream streamArquivoFinal = new FileOutputStream(arquivoBaixado);
	                        int contador;
							while ((contador = streamArquivo.readInt()) > 0)
							{
							  byte[] buffer = new byte[contador];
							  streamArquivo.readFully(buffer);
							  streamArquivoFinal.write(buffer,0,contador);
							}
							
							// Mensagem de sucesso
			                System.out.println("----------------------------------------------------------------------------------");
			    	        System.out.println("Arquivo " + this.arquivo + " baixado com sucesso na pasta " + pastaJoin);
			    	        System.out.println("----------------------------------------------------------------------------------");
							
							// Fechando recursos
							streamArquivoFinal.close();
							
							// Composição conteúdo da mensagem a ser enviada
							String stringMSG = this.arquivo + "//" + Integer.toString(portaPeer);
		    				
		    				// Compondo mensagem de UPDATE a ser enviada para o servidor
			                Mensagem msg = new Mensagem(4, stringMSG);
			                JSONObject jsonmsg = msg.criarJSON();
			                
			                // Buffer com mensagem que será enviada
			                byte[] bufferMsg = new byte[jsonmsg.toString().length()];
			                bufferMsg = jsonmsg.toString().getBytes();
			                
			                // Criação de datagrama para envio de informações
			                DatagramPacket pacoteMsg = new DatagramPacket(bufferMsg, bufferMsg.length, ServerAdress, 10098);               
			                
			                // Buffer e datagrama para receber mensagem
			                byte[] bufferReceber = new byte[1024];
			                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);
			                
			                // Flag de sucesso de download 
			                flagDownloadSucesso = true;
			                
			                // Loop para fazer retry de envio e esperar UPDATE_OK
			                while (true) {
			                	try {
			                		// Enviando pacote com mensagem e recebendo resposta do servidor
			                		clientSocket.send(pacoteMsg);
			                		clientSocket.receive(pacoteRecebido);
			                		break;		           
			                	}
			                	catch(SocketTimeoutException e) {
			                		System.out.println(e.getMessage());
			                	}
	
			                }
			                
			                // Fechando socket
			                clientSocket.close();
							
							// Encerrando thread
							return;
		    			}
		    			else {
		    				
	                		// Removemos o peer que resultou em download negado 
							if (listaUltimoSearch.size() != 1) {
								Random random = new Random();
								int indAleatorio = random.nextInt(listaUltimoSearch.size());
								String peerProximaRequest = listaUltimoSearch.get(indAleatorio);
								String[] peer = peerProximaRequest.split(":");
								this.IP = peer[0];
								System.out.println(this.IP);
								this.porta = peer[1];
								System.out.println(this.porta);
							}
							else {
								Thread.sleep(15000);
							}
							contadorTentativas = contadorTentativas + 1;
							
							// Mensagem de DOWNLOAD_NEGADO
		    				System.out.println("----------------------------------------------------------------------------------");
	                		System.out.println("Peer " + primeiroIP + ":" + primeiraPorta + " negou o download, pedindo agora para o peer "
	                							+ this.IP + ":" + this.porta);
	                		System.out.println("----------------------------------------------------------------------------------");
		    			}
		    			
			            // Fechando canal do comunicação com outro peer
			            socketRequisicao.close();
			            streamEntrada.close();
		    			streamSaida.close();
					}
					
					// Mensagem de quando não foi possível realizar o DOWNLOAD
    				System.out.println("----------------------------------------------------------------------------------");
            		System.out.println("Aviso: Não possível atender requisição de DOWNLOAD, tente novamente mais tarde!!!");
            		System.out.println("----------------------------------------------------------------------------------");
					
				}
				
			} 
			catch (Exception e) {
				e.getStackTrace();
			}
						
			
		}
		
	}
	
	// Nessa classe temos a ThreadDownload, aquela que vai ficar escutando as requisições TCP de outros peers
	public static class ThreadDownload extends Thread{
			
		private File pasta;
		private int porta;
		
		// Método construtor da classe que simplesmente instancia uma mensagem enviada ao servidor
		public ThreadDownload(File pasta, int porta) {
			this.pasta = pasta;
			this.porta = porta;
		}
			
		public void run() {
			
			try {
				
				// Criando socket de conexão TCP
				socketServidor = new ServerSocket(this.porta);

				while(true){
					
					// Método bloqueante onde o servidor espera comunicação
	                Socket socketCliente = socketServidor.accept();
					
					// Criação de stream de entrada e reader de informações do socketCliente
	                InputStreamReader streamEntrada =  new InputStreamReader(socketCliente.getInputStream());
	                BufferedReader reader = new BufferedReader(streamEntrada);		
					
					// Criação de stream de saída e escrita de informações pelo socketServidor
	                OutputStream streamSaida =  socketCliente.getOutputStream();
	                DataOutputStream writer = new DataOutputStream(streamSaida);
					
					// Realizando a leitura para array de bytes criado usando stream de entrada de dados
	                String nomeArquivo = reader.readLine();
					
					// Compondo nome do arquivo com nome da pasta
					String caminhoString = this.pasta.getAbsolutePath() + "/" + nomeArquivo;
					
					// Criando objeto file
					File caminhoFile = new File(caminhoString);	
	                
									
					// Quando recebe um pedido de DOWLOAD verifica se na pasta de JOIN existe o arquivo solicitado
					// Caso exista escolhe aleatoriamente se envia ou não
					// Caso não exista já rejeita o DOWNLOAD de cara
					if (caminhoFile.exists()) {
										
						// Variável que vai decidir se o arquivo vai ser enviado ou não
						Random random = new Random();
						int resultado = random.nextInt(2);
						
						// Composição e envio de arquivo com mensagem DOWLOAD ou da mensagem DOWNLOAD_NEGADO
						if (resultado == 0) {
							
							// Aqui enviamos uma mensagem DOWNLOAD para sinalizar que o DOWNLOAD
				            Mensagem msg = new Mensagem(6, "DOWNLOAD");
				            JSONObject jsonmsg = msg.criarJSON();		
				            writer.writeBytes(jsonmsg.toString() + "\n");
				            
				            // Esperando mensagem de keepOn para enviar os arquivos       
				            reader.readLine();
				            
				            // Enviando o arquivo em si para o cliente através de chunks
				            FileInputStream leituraArquivo = new FileInputStream(caminhoFile.getAbsolutePath());
							byte[] buffer = new byte[8192];
							int contador;
							while ((contador = leituraArquivo.read(buffer)) > 0){
							  writer.writeInt(contador);
							  writer.write(buffer,0,contador);
							}
							writer.writeInt(contador);
							leituraArquivo.close();
							
						}
						else {
							
							// Aqui enviamos uma mensagem DOWNLOAD_NEGADO
				            Mensagem msg = new Mensagem(7, "DOWNLOAD_NEGADO");
				            JSONObject jsonmsg = msg.criarJSON();
				            writer.writeBytes(jsonmsg.toString() + "\n");
				            
						}
					}
					else {
						// Aqui enviamos uma mensagem DOWNLOAD_NEGADO
			            Mensagem msg = new Mensagem(7, "DOWNLOAD_NEGADO");
			            JSONObject jsonmsg = msg.criarJSON();
			            writer.writeBytes(jsonmsg.toString());
					}
					
				}
				
			}
			catch(Exception e) {
				
			}		
			
		}
		
	}
	
	// Classe de thread responsável pelo envio de mensagens ALIVE ao servidor
	public static class ThreadAlive extends Thread{
		
		private int porta;
		
		public ThreadAlive(int porta) {
			this.porta = porta;
		}
		
		public void run() {
				
			try {
				
				// Laço while que itera enquanto a thread estiver ativa				
				while(true) {
					
					// Endereço de IP do servidor
					final InetAddress ServerAdress = InetAddress.getByName("127.0.0.1");
		            
					// Canal de comunicação não orientado à conexão para escuta de mensagens ALIVE usando UDP
					DatagramSocket clientSocket;
					clientSocket = new DatagramSocket(this.porta);
					
	                // Buffer e datagrama para receber mensagem de ALIVE
	                byte[] bufferReceber = new byte[1024];
	                DatagramPacket pacoteRecebido = new DatagramPacket(bufferReceber, bufferReceber.length);    
	                
	                // Aguardando comunicação do servidor por ALIVE
	        		clientSocket.receive(pacoteRecebido);
	        		
	        		// Transformando conteúdo recebido no Datagrama em uma mensagem
	        		String stringResposta = new String(pacoteRecebido.getData(), pacoteRecebido.getOffset(), pacoteRecebido.getLength());
	                JSONObject JSONReposta = new JSONObject(stringResposta);
	    			Mensagem msgResposta = new Mensagem(JSONReposta);
	    			
	    			// Verificando conteúdo da mensagem enviada
	    			if (msgResposta.conteudo.equals("ALIVE") ) {
		        		
		        		// Compondo mensagem que será enviada com ALIVE_OK
		                Mensagem msg = new Mensagem(5, "ALIVE_OK");
		                JSONObject jsonmsg = msg.criarJSON();
		                
		                // Buffer com mensagem que será enviada
		                byte[] bufferMsg = new byte[jsonmsg.toString().length()];
		                bufferMsg = jsonmsg.toString().getBytes();
		                
		                // Criação de datagrama para envio de informações
		                DatagramPacket pacoteMsg = new DatagramPacket(bufferMsg, bufferMsg.length, ServerAdress, 10098);                 
		                
		                // Enviando mensagem
		                clientSocket.send(pacoteMsg);   
		                
	    			}     		
	    			else {
	    				System.out.println("Erro: Desconectando por razão não conhecida!!!");
	    			}
	    			
	    			// Fechando recurso de socket para cliente
	    			clientSocket.close();
	    			
				}
				
			}	
			catch(Exception e) {
				e.getStackTrace();
			}
			
		}
		
	}
	

}


