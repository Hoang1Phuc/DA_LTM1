/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import DAO.MailDAO;
import Models.DataPacket;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollBar;

/**
 *
 * @author Admin
 */
public class Server extends javax.swing.JFrame {

    private final int port = 7777;
    private final ArrayList<ClientHandler> listClients = new ArrayList<>();
    private final ExecutorService pool = Executors.newFixedThreadPool(10);
    private String resource;
    MailDAO md = new MailDAO();

    public ArrayList<ClientHandler> getListClients() {
        return listClients;
    }

    public void log(String text, String type) {
        if (resource.startsWith("<html>")) {
            resource = "";
        }
        switch (type) {
            case "info": {
                String tmp = "<div><span style='color: black'>" + text + "</span></div>";
                resource = resource + tmp;
                this.txtMainConsole.setText(resource);
                break;
            }
            case "alert": {
                String tmp = "<div><span style='color: black'>" + text + "</span></div>";
                resource = resource + tmp;
                this.txtMainConsole.setText(resource);
                break;
            }
            case "normal": {
                String tmp = "<div><span>" + text + "</span></div>";
                resource = resource + tmp;
                this.txtMainConsole.setText(resource);
                break;
            }
        }
        JScrollBar bar = jScrollPane1.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    public void removeClient(ClientHandler client) {
        this.listClients.remove(client);
    }

    public void saveAttachments(String email, byte[] file, String filename) {
        //thư mực lưu file đính kèm có dạng "Attachments/{emailngnhan}/"
        String saveFolder = "Attachments";
        FileOutputStream fos = null;
        try {
            //kiểm tra thư mục theo tên đã tồn tại chưa
            Path path = Paths.get(saveFolder + "/" + email);
            if (Files.exists(path) == false) {
                new File(saveFolder + "/" + email).mkdir();
            }
            fos = new FileOutputStream(saveFolder + "/" + email + "/" + filename);
            fos.write(file);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void sendEmail(DataPacket dp, ClientHandler c) {
        this.log("Đang gửi email đến " + dp.getTo(), "normal");
        if (dp.getCc().length() > 0) {
            this.log("Đang cc email đến " + dp.getCc(), "normal");
        }
        //lưu email vào DB
        boolean result = md.saveEmail(dp);
        //lưu file đính kèm trên server
        if (dp.getAttachments().length() > 0) {
            this.saveAttachments(dp.getTo(), dp.getFile(), dp.getAttachments());
        }

        //check xem nguoi nhan co dang dang nhap ko,
        //neu co se gui email thong qua socket cho nguoi do
        if (result) {
            for (ClientHandler client : listClients) {
                if (client.getClientAccount().getUsername().equals(dp.getTo())) {
                    client.sendEmail(dp);
                    break;
                }
            }
        } else {
            this.log("Tài khoản email " + dp.getTo() + " không tồn tại", "alert");
            c.sendError();
        }
    }

    public void startServer() {
        try {
            ServerSocket server = new ServerSocket(port);
            resource = "";
            this.log("Máy chủ đang khởi chạy...", "info");

            while (true) {
                Socket client = server.accept();
                //nhận diện có client online
                //tạo 1 thread mới
                ClientHandler clientThread = new ClientHandler(client, this);
                listClients.add(clientThread);
                pool.execute(clientThread);
            }
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    /**
     * Creates new form Server
     */
    public Server() {

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtMainConsole = new javax.swing.JEditorPane();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Server");

        jScrollPane1.setName("Server"); // NOI18N

        txtMainConsole.setEditable(false);
        txtMainConsole.setContentType("text/html"); // NOI18N
        txtMainConsole.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jScrollPane1.setViewportView(txtMainConsole);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        Server s = new Server();
        s.setVisible(true);
        s.startServer();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane txtMainConsole;
    // End of variables declaration//GEN-END:variables
}
