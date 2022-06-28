package java_sqlite;

import ConexaoSQLite;

public class Main {
    
    public static void main(String[] args){

        ConexaoSQLite Conexao = new ConexaoSQLite();
        Conexao.conectar();
        Conexao.desconectar();

    }

}
