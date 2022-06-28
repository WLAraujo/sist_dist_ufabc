package java_sqlite;

import java.sql.*;


// Classe definida para realizar operações de conexão
public class ConexaoSQLite {
    
    // Variável de conexão
    private Connection conexao;

    // Estabelecer conexão com banco de dados
    public boolean conectar(){

        try{

            // String de conexão
            String url = "jdbc:sqlite:./banco_de_dados/banco_teste_sqlite.db";

            // Criando driver manager
            this.conexao = DriverManager.getConnection(url);

        }
        catch(SQLException e){
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

    // Desconectar do banco de dados
    public boolean desconectar(){

        try{
            // Verifica se conexão está estabelecida e fecha se for o caso
            if (this.conexao.isClosed() == false){
                this.conexao.close();
            }

        }
        catch(SQLException e){
            System.err.println(e.getMessage());
            return false;
        }

        return true;
    }

}
