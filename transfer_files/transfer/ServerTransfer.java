import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerTransfer {

    static ArrayList<Arquivo> arquivos = new ArrayList<>();

    public static void main(String[] args) {
    
        int arquivoID = 0;

        try{

        // Criação de socket de escuta
        ServerSocket socketServidor = new ServerSocket(9876);

        // Laço while de repetição
        while(true){

            try{

                // Método bloqueante onde o servidor espera comunicação
                Socket socket = socketServidor.accept();

                // Criando stream de leitura com base no conteúdo da stream transferida pelo socket
                DataInputStream streamEntrada = new DataInputStream(socket.getInputStream());

                // Baseando-se na ordem que definimos o stream vamos o objeto Arquivo 

                // Quantos bytes tem o nome
                int tamanhoNome = streamEntrada.readInt();

                // Leitura do nome em um array de bytes
                if (tamanhoNome > 0){
                    // Definindo array
                    byte[] bytesNome = new byte[tamanhoNome];
                    // Realizando a leitura para array criado
                    streamEntrada.readFully(bytesNome, 0, bytesNome.length);
                    String nomeArquivo = new String(bytesNome);

                    // Tamanho do conteúdo do arquivo
                    int tamanhoArquivo = streamEntrada.readInt();

                    // Leitura do arquivo em um array de bytes
                    if (tamanhoArquivo > 0){
                        // Definindo array
                        byte[] bytesArquivo = new byte[tamanhoArquivo];
                        // Realizando a leitura para array criado
                        streamEntrada.readFully(bytesArquivo, 0, bytesArquivo.length);

                        // Criando objeto que após dowload será nosso arquivo
                        File arquivoBaixado = new File(nomeArquivo);

                        try{

                            // Criando stream de saída para escrever no arquivo criado
                            FileOutputStream streamSaida = new FileOutputStream(arquivoBaixado);

                            // Escrevendo dados no arquivo criado para ser nossa versão baixada 
                            streamSaida.write(bytesArquivo);
                            streamSaida.close();

                            // Adicionando o arquivo baixado à lista de meus arquivos
                            arquivos.add(new Arquivo(arquivoID, nomeArquivo, bytesArquivo, getFileExtension(nomeArquivo)));

                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }

                    }
                }
                
            }
            catch(IOException e){
                e.getStackTrace();
            }

        }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static class Arquivo{

        private int id;
        private String nome;
        private byte[] dados;
        private String extensao;

        public Arquivo(int id, String nome, byte[] dados, String extensao){
            this.id = id;
            this.nome = nome;
            this.dados = dados;
            this.extensao = extensao;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public void setDados(byte[] dados) {
            this.dados = dados;
        }

        public void setExtensao(String extensao) {
            this.extensao = extensao;
        }

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public byte[] getDados() {
            return dados;
        }

        public String getExtensao() {
            return extensao;
        }

    }

    public static String getFileExtension(String nomeArquivo){
        
        // Index que marca fim do nome e começo da extensão
        int i = nomeArquivo.lastIndexOf('.');

        // Retorna substring após último .
        return nomeArquivo.substring(i+1);

    }

}
