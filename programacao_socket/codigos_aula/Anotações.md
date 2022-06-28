O processo do servidor deve estar rodando primeiro com socket definido

Quando cliente cria socket ele estabelece conexão com servidor

Quando contatado pelo cliente, servidor TCP cria novo socket, isso permite comunicação com vários clientes

O processo do servidor criar um novo socket é automático

Lembrando que o processo do servidor envolve duas portas, a pública de apresentação que o cliente usa para se comunicar e a de conexão usada para estabelecer a conexão cliente-servidor

Para ler ou receber informações de rede em Java é necessário uso dos streams

Classe que define sockets usados na comunicação estabelecida, o socket do cliente e o socket de conexão do servidor

public class Socket{

    Método para servir como entrada e "ler" informações enviadas pela saída do outro lado da comunicação
    
    public inputStream getInputStream()

    Método para servir de saída e "escrever" as informações a serem lidas pela outra ponta da comunicação

    public outputStream getOutputStream()

    Método para fechar a conexão

    public void close()

}

Classe que implementa o socket receptivo do servidor, ela apresenta os mecanismos para um nó, como o server, escutar, aceitar e estabelecer comunicação com outro nó

public class ServerSocket(){

    Método que cria o mecanismo para escutar requisições de conexão através de uma porta

    public ServerSocker(int port)

    Método bloqueante que espera pelo ingresso de uma conexão. O método cria um novo socker com o nó que iniciou a conexão

}