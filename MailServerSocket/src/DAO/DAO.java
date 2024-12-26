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

/**
 *
 * @author Admin
 * @param <T> type of data
 */
public class DAO<T> {
  protected String tableName;

    public ResultSet getAll() {
        String query = new StringBuilder("select * from ").append(this.tableName).toString();
        Connection conn = new Connector().connect();
        Statement stm = null;
        ResultSet resultSet = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            resultSet = stm.executeQuery(query);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}
