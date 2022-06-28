package threads;

// Classe de threads
public class DemoThread extends Thread{

    // Variável que declara nome da thread
    private String threadName;

    // Método construtor
    public DemoThread(String name){
        threadName = name;
    }

    // Classe que dá start em uma thread, chamado pelo método start()
    public void run() {

        // Imprimindo uma mensagem inicial
        System.out.println("Sou a thread");

        // Fazendo com que a thread "durma" durante alguns segundos 
        // A maioria dos métodos de thread envolve lançamento de exceções
        try{
            for(int i = 4; i > 0; i --) {
                System.out.println("Thread:" + threadName + " Tempo:" + i);
                Thread.sleep(50);
            }
        }
        catch (InterruptedException e){}
    }

}