package threads;

public class Main {

    // Classe mais do nosso projeto
    public static void main(String args[]) {

        // Instanciando threads
        DemoThread dt1 = new DemoThread("1");
        DemoThread dt2 = new DemoThread("2");

        // Esse método start chama o método run da classe
        // Como aqui estamos chamando duas threads, então as execuções serão intercaladas
        dt1.start();
        dt2.start();

    }

}