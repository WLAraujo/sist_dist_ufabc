import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientTransfer {

    public static void main(String[] args) {

        try{

            // Arquivo que desejamos enviar
            File arquivoEnviar = new File("./arquivos_p_transferir/planilha_entrada.csv");

            // Stream do arquivo de entrada usando caminho absoluto
            FileInputStream streamEntrada = new FileInputStream(arquivoEnviar.getAbsolutePath());

            // Criar socket de conexão
            Socket socket = new Socket("127.0.0.1", 9876);

            // Criando stream de saída para envio
            DataOutputStream streamSaida = new DataOutputStream(socket.getOutputStream());

            // Pegando nome do arquivo
            String nomeArquivo = arquivoEnviar.getName();

            // Criando array de bytes do nome do arquivo
            byte[] bytesNomeArquivo = nomeArquivo.getBytes();

            // Criando array de bytes do arquivo em si
            byte[] bytesArquivo = new byte[(int)arquivoEnviar.length()];

            // Lendo bytes do array com conteúdo do arquivo para a a stream de entrada
            streamEntrada.read(bytesArquivo);

            // Primeiro vamos enviar um inteiro para o servidor, assim ele sabe quantos bytes deve esperar
            // Vamos começar enviando o nome do arquivo
            streamSaida.writeInt(bytesNomeArquivo.length);
            streamSaida.write(bytesNomeArquivo);

            // Agora, com a mesma estratégia vamos enviar o arquivo em si
            streamSaida.writeInt(bytesArquivo.length);
            streamSaida.write(bytesArquivo);

        }
        catch (IOException e){
            e.printStackTrace();
        }

    }
    
}
