/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import DatabaseConnection.Connector;
import Models.Account;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Admin
 */
public class AccountDAO {

    public AccountDAO() {
    }

    public boolean checkExist(String username) {
        String query = "select Id, Username from Account where Username='" + username + "'";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(query);
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Account checkLogin(String username, String password) {
        String query = "select Id, Username, Password from Account where Username='" + username + "' and Password='" + password + "'";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(query);
            if (resultSet.next()) {
                Account acc = new Account();
                acc.setId(resultSet.getInt("Id"));
                acc.setUsername(resultSet.getString("Username"));
                acc.setPassword(resultSet.getString("Password"));
                return acc;
            } else {
                return null;
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void registerAccount(String username, String password) {
        String query = "insert into Account values ('" + username + "', '" + password + "')";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            stm.executeUpdate(query);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList<Account> getAll() {
        String query = "select * from Account";
        Connection conn = new Connector().connect();
        Statement stm = null;
        ArrayList<Account> accounts = new ArrayList<>();
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(query);
            while (resultSet.next()) {
                Account acc = new Account();
                acc.setUsername(resultSet.getString("Username"));
                acc.setPassword(resultSet.getString("Password"));
                accounts.add(acc);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return accounts;
    }

    public Account getByUsername(String username) {
       String query = "select * from Account where Username = N'" + username + "'";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(query);
            if (resultSet.next()) {
                Account acc = new Account();
                acc.setId(resultSet.getInt("Id"));
                acc.setUsername(resultSet.getString("Username"));
                acc.setPassword(resultSet.getString("Password"));
                return acc;
            } else {
                return null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Account getById(int id) {
      String query = "select * from Account where Id = " + id;
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet resultSet = stm.executeQuery(query);
            if (resultSet.next()) {
                Account acc = new Account();
                acc.setId(resultSet.getInt("Id"));
                acc.setUsername(resultSet.getString("Username"));
                acc.setPassword(resultSet.getString("Password"));
                return acc;
            } else {
                return null;
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(AccountDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
