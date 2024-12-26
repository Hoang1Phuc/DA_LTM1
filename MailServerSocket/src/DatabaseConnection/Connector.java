/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Admin
 */
public class Connector {

    private final String databaseName = "MailServer";
    private final String server = "127.0.0.1";
    private final int port = 1433;
    private final String username = "sa";
    private final String password = "123456";

    public Connection connect() {
        Connection conn;
        try {
            String connectionString = "jdbc:sqlserver://" + this.server + ":" + this.port + ";databaseName=" + this.databaseName + ";user=" + this.username + ";password=" + this.password;
            conn = DriverManager.getConnection(connectionString);

        } catch (SQLException e) {
            System.out.println(e.toString());
            return null;
        }
        return conn;
    }

    public static void main(String[] args) {
        Connector c = new Connector();
        if(c.connect()!=null){
            System.out.println("Thanh cong");
        } else{
            System.out.println("That bai");
        }
    }
}
