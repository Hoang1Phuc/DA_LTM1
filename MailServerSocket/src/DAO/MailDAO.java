/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import DatabaseConnection.Connector;
import Models.Account;
import Models.Mail;
import Models.DataPacket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class MailDAO {

    AccountDAO accountDAO = new AccountDAO();

    public boolean saveEmail(DataPacket dp) {
        Account receiverAcc = accountDAO.getByUsername(dp.getTo());
        if (receiverAcc != null) {
            Mail mail = new Mail(dp.getFrom().getId(), receiverAcc.getId(), dp.getSubject(), dp.getBody(), new Date(), dp.getCc(), dp.getAttachments());
            //lưu email vào database
            String query = "insert into Mail(SenderId, ReceiverId, Subject, Body, SentTime, State, IsDeleted, Cc, Attachments) values (" + mail.getListFieldValues() + ")";
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
            return true;
        } else {
            return false;
        }
    }
     public ArrayList<Mail> getInbox(Account account) {
        ArrayList<Mail> inboxMail = new ArrayList<>();
        String query = "select * from Mail where ReceiverId = " + account.getId() + " or Cc like '%" + account.getUsername() + "%' and IsDeleted = 0 Order By SentTime DESC";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);

            while (rs.next()) {
                int Id = rs.getInt("Id");
                int SenderId = rs.getInt("SenderId");
                int ReceiverId = rs.getInt("ReceiverId");
                String Subject = rs.getString("Subject");
                String Body = rs.getString("Body");
                Timestamp t = rs.getTimestamp("SentTime");
                Date SentTime = new Date(t.getTime());
                String State = rs.getString("State");
                boolean IsDeleted = false;
                String Cc = rs.getString("Cc");
                String Attachments = rs.getString("Attachments");
                inboxMail.add(new Mail(Id, SenderId, ReceiverId, Subject, Body, SentTime, State, IsDeleted, Cc, Attachments));
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
        return inboxMail;
    }

   public ArrayList<Mail> getSent(Account account) {
        ArrayList<Mail> inboxMail = new ArrayList<>();
        String query = "select * from Mail where SenderId = " + account.getId() + " and IsDeleted = 0 Order By SentTime DESC";
        Connection conn = new Connector().connect();
        Statement stm = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(query);

            while (rs.next()) {
                int Id = rs.getInt("Id");
                int SenderId = rs.getInt("SenderId");
                int ReceiverId = rs.getInt("ReceiverId");
                String Subject = rs.getString("Subject");
                String Body = rs.getString("Body");
                Timestamp t = rs.getTimestamp("SentTime");
                Date SentTime = new Date(t.getTime());
                String State = rs.getString("State");
                boolean IsDeleted = false;
                String Cc = rs.getString("Cc");
                String Attachments = rs.getString("Attachments");
                inboxMail.add(new Mail(Id, SenderId, ReceiverId, Subject, Body, SentTime, State, IsDeleted, Cc, Attachments));
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
        return inboxMail;
    }
}
