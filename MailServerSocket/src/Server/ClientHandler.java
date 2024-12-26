/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import Models.DataPacket;
import DAO.MailDAO;
import Models.Account;
import Models.Mail;
import Models.Response;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;

/**
 *
 * @author Admin
 */
public class ClientHandler extends Thread {

    private Account clientAccount;
    private final Socket client;
    private ObjectInputStream ois;
    private final Server server;
    private String resource;
    MailDAO md = new MailDAO();
    private ObjectOutputStream oos;

    public ClientHandler(Socket client, Server server) {
        this.client = client;
        this.server = server;
    }

    public Account getClientAccount() {
        return clientAccount;
    }

    public void sendEmail(DataPacket dp) {
        Mail mail = new Mail(dp, this.clientAccount.getId());
        ArrayList<Mail> mails = new ArrayList<>();
        mails.add(mail);
        Response<ArrayList<Mail>> r = new Response<>("##NEWEMAIL##", mails);
        try {
            oos.writeObject(r);
            // server.log("Sent to " + acc.getUsername(), "normal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOutbox(DataPacket dp) {
        Mail mail = new Mail(dp, this.clientAccount.getId());
        ArrayList<Mail> mails = new ArrayList<>();
        mails.add(mail);
        Response<ArrayList<Mail>> r = new Response<>("##NEWOUTBOX##", mails);
        try {
            oos.writeObject(r);
            // server.log("Sent to " + acc.getUsername(), "normal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getInboxAndOutbox(Account acc) {
        server.log("Đang lấy dữ liệu hộp thư đến của " + acc.getUsername(), "normal");
        ArrayList<Mail> inboxMail = md.getInbox(acc);
        server.log("Đang lấy dữ liệu hộp thư đi của " + acc.getUsername(), "normal");
        ArrayList<Mail> outboxMail = md.getSent(acc);
        Response<ArrayList<Mail>> r = new Response<>("##GETINBOX##", inboxMail);
        Response<ArrayList<Mail>> r2 = new Response<>("##GETSENT##", outboxMail);
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            oos.writeObject(r);
            oos.writeObject(r2);
            server.log("Đã gửi cho " + acc.getUsername(), "normal");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String to, String attachments) {
        try {
            byte[] f = Files.readAllBytes(new File("Attachments/" + to + "/" + attachments).toPath());
            Response<byte[]> r = new Response<>("##DOWNLOADFILE##", f);
            //guiwr file cho nguoi tai
            oos.writeObject(r);
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendError() {
        Response<String> r = new Response<>("##ERRORNOTEXIST##", "");
        try {
            oos.writeObject(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                ois = new ObjectInputStream(client.getInputStream());
                DataPacket dp = (DataPacket) ois.readObject();
                clientAccount = dp.getFrom();
                switch (dp.getSubject()) {
                    case "##PING##": {
                        server.log(clientAccount.getUsername() + " đã kết nối", "info");
                        System.out.println(clientAccount.getUsername() + " connected");
                        this.getInboxAndOutbox(clientAccount);
                        break;
                    }
                    case "##DOWNLOADFILE##": {
                        server.log(clientAccount.getUsername() + " tải tệp tin đính kèm", "normal");
                        this.downloadFile(dp.getTo(), dp.getAttachments());
                        break;
                    }
                    default: {
                        server.sendEmail(dp, this);
                        this.sendOutbox(dp);
                    }
                }
            }
        } catch (IOException i) {
            System.out.println(i);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            server.log(clientAccount.getUsername() + " đã ngắt kết nối", "alert");
            server.removeClient(this);
        }
    }

}
