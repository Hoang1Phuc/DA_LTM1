/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import DAO.AccountDAO;
import Models.Account;
import Models.Mail;
import Models.DataPacket;
import Models.Response;
import Server.ClientHandler;
import Server.Server;
import java.awt.Cursor;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import utils.EncryptionUtils;

/**
 *
 * @author Admin
 */
public class MailClient extends javax.swing.JFrame {

    private final int serverPort = 7777;
    private final String serverIP = "127.0.0.1";
    private final Account account;
    private Socket socket;
    private ArrayList<Mail> inboxMail;
    private ArrayList<Mail> sentMail;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String emailToSelected;
    // private SecretKey secretKey;

    /**
     * Creates new form MailClient
     *
     * @param account
     */
    public MailClient(Account account) {
        this.account = account;
        initComponents();
        txtAttachments.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.setTitle(account.getUsername());
        initSocket();
        getInboxMail();
    }

    /* private void initEncryption() {
     try {
     // Tạo khóa bí mật AES
     secretKey = EncryptionUtils.generateKey();
     System.out.println("Generated Secret Key: " + EncryptionUtils.keyToString(secretKey));
     } catch (Exception e) {
     e.printStackTrace();
     }
     }

     private void sendEncryptedMail(Mail mail) {
     try {
     // Mã hóa subject và body của thư
     String encryptedSubject = EncryptionUtils.encrypt(mail.getSubject(), secretKey);
     String encryptedBody = EncryptionUtils.encrypt(mail.getBody(), secretKey);

     // Tạo DataPacket với dữ liệu đã mã hóa
     DataPacket encryptedPacket = new DataPacket(
     account,
     mail.getReceiver(), // Địa chỉ email người nhận
     "##SENDMAIL##", // Loại yêu cầu
     encryptedSubject, // Subject đã mã hóa
     encryptedBody // Body đã mã hóa
     );

     // Gửi DataPacket đến server
     oos = new ObjectOutputStream(socket.getOutputStream());
     oos.writeObject(encryptedPacket);
     System.out.println("Mail đã được gửi với dữ liệu mã hóa!");

     } catch (Exception e) {
     e.printStackTrace();
     JOptionPane.showMessageDialog(this, "Lỗi khi gửi thư mã hóa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
     }
     }*/
    public void getInbox(Response r) {
        inboxMail = (ArrayList<Mail>) r.getT();
        DefaultListModel<String> model = new DefaultListModel<>();
        inboxMail.forEach((mail) -> {
            model.addElement(mail.getSubject());
        });
        listInbox.setModel(model);
        if (model.size() > 0) {
            setMailIndex(0);
            listInbox.setSelectedIndex(0);
        } else {
            System.out.println("Inbox is empty.");
        }
    }

    public void getSent(Response r) {
        sentMail = (ArrayList<Mail>) r.getT();
        DefaultListModel<String> model = new DefaultListModel<>();
        sentMail.forEach((mail) -> {
            model.addElement(mail.getSubject());
        });
        listOutbox.setModel(model);
        if (model.size() > 0) {
            setOutMailIndex(0);
            listOutbox.setSelectedIndex(0);
        } else {
            System.out.println("Outbox is empty.");
        }
    }

    public void getNewEmail(Response r) {
        System.out.println("get new email");
        ArrayList<Mail> mails = (ArrayList<Mail>) r.getT();
        Mail mail = mails.get(0);
        inboxMail.add(0, mail);
        DefaultListModel model = new DefaultListModel();
        inboxMail.forEach((m) -> {
            model.addElement(m.getSubject());
        });
        listInbox.setModel(model);

    }

    public void getNewOutbox(Response r) {
        System.out.println("get new outbox");
        ArrayList<Mail> mails = (ArrayList<Mail>) r.getT();
        Mail mail = mails.get(0);
        sentMail.add(0, mail);
        DefaultListModel model = new DefaultListModel();
        sentMail.forEach((m) -> {
            model.addElement(m.getSubject());
        });
        listOutbox.setModel(model);

    }

    /**
     * Tải về file đính kèm
     *
     * @param r
     */
    public void downloadFile(Response r) {
        JFileChooser f = new JFileChooser();
        f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (f.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f.getSelectedFile().getAbsolutePath() + "/" + txtAttachments.getText());
                fos.write((byte[]) r.getT());
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
    }

    private void getInboxMail() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // Kiểm tra socket trước khi khởi tạo ObjectInputStream
                    if (socket == null || socket.isClosed() || !socket.isConnected()) {
                        System.err.println("Socket đã bị đóng hoặc không kết nối.");
                        return;
                    }

                    // Khởi tạo ObjectInputStream chỉ khi cần
                    if (ois == null) {
                        ois = new ObjectInputStream(socket.getInputStream());
                    }

                    while (!socket.isClosed() && socket.isConnected()) {
                        try {
                            Object response = ois.readObject();
                            if (response instanceof Response) {
                                Response<?> r = (Response<?>) response;
                                switch (r.getType()) {
                                    case "##GETINBOX##":
                                        getInbox(r);
                                        break;
                                    case "##GETSENT##":
                                        getSent(r);
                                        break;
                                    case "##NEWEMAIL##":
                                        getNewEmail(r);
                                        break;
                                    case "##NEWOUTBOX##":
                                        getNewOutbox(r);
                                        break;
                                    case "##DOWNLOADFILE##":
                                        downloadFile(r);
                                        break;
                                    case "##ERRORNOTEXIST##":
                                        JOptionPane.showMessageDialog(null, "Email người nhận không tồn tại.");
                                        break;
                                    default:
                                        System.err.println("Phản hồi không xác định từ server.");
                                }
                            } else {
                                System.err.println("Phản hồi không hợp lệ từ server.");
                            }
                        } catch (ClassNotFoundException | IOException e) {
                            System.err.println("Lỗi khi đọc phản hồi từ server: " + e.getMessage());
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Không thể khởi tạo ObjectInputStream: " + e.getMessage());
                }
            }
        };
        new Thread(runnable).start();
    }

    private void initSocket() {
        try {
            socket = new Socket(serverIP, serverPort);

            // Gửi gói tin PING để xác nhận kết nối
            DataPacket data = new DataPacket(this.account, "", "##PING##", "", "");
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(data);
            oos.flush();

            System.out.println("Kết nối đến server thành công.");
        } catch (IOException ex) {
            System.err.println("Không thể kết nối đến server: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến server.\nHãy kiểm tra server và thử lại.");
        }
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
        jButtonThuMoi = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        listInbox = new javax.swing.JList<String>();
        jScrollPane3 = new javax.swing.JScrollPane();
        listOutbox = new javax.swing.JList<String>();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        txtTime = new javax.swing.JLabel();
        txtFrom = new javax.swing.JLabel();
        txtSubject = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtBody = new javax.swing.JTextArea();
        txtCc = new javax.swing.JLabel();
        txtAttachments = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuThoat = new javax.swing.JMenuItem();
        jMenuXoaMail = new javax.swing.JMenu();
        jMenuXoa = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(200, 500));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jButtonThuMoi.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        jButtonThuMoi.setText("Thư mới");
        jButtonThuMoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonThuMoiActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonThuMoi, java.awt.BorderLayout.PAGE_START);

        listInbox.setPreferredSize(null);
        listInbox.setRequestFocusEnabled(false);
        listInbox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listInboxValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(listInbox);

        jTabbedPane1.addTab("Hộp thư đến", jScrollPane1);

        listOutbox.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listOutboxValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(listOutbox);

        jTabbedPane1.addTab("Hộp thư đi", jScrollPane3);

        jPanel1.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.LINE_START);

        jPanel2.setLayout(new java.awt.BorderLayout());

        txtTime.setText(" ");

        txtFrom.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        txtFrom.setText(" ");

        txtSubject.setFont(new java.awt.Font("sansserif", 0, 24)); // NOI18N
        txtSubject.setText(" ");

        txtBody.setEditable(false);
        txtBody.setColumns(20);
        txtBody.setLineWrap(true);
        txtBody.setRows(5);
        txtBody.setBorder(null);
        jScrollPane2.setViewportView(txtBody);

        txtCc.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        txtCc.setText(" ");

        txtAttachments.setFont(new java.awt.Font("sansserif", 2, 12)); // NOI18N
        txtAttachments.setForeground(new java.awt.Color(0, 153, 255));
        txtAttachments.setText(" ");
        txtAttachments.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txtAttachmentsMouseClicked(evt);
            }
        });

        jLabel1.setText("Tệp đính kèm");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 888, Short.MAX_VALUE)
                            .addComponent(txtTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtAttachments, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(567, 567, 567)))
                        .addContainerGap())
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCc, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSubject)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFrom)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCc)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAttachments)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Hệ Thống");

        jMenuThoat.setText("Thoát");
        jMenuThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuThoatActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuThoat);

        jMenuBar1.add(jMenu1);

        jMenuXoaMail.setText("Edit");

        jMenuXoa.setText("Xóa Thư");
        jMenuXoa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuXoaActionPerformed(evt);
            }
        });
        jMenuXoaMail.add(jMenuXoa);

        jMenuItem3.setText("jMenuItem3");
        jMenuXoaMail.add(jMenuItem3);

        jMenuBar1.add(jMenuXoaMail);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonThuMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonThuMoiActionPerformed
        int y = this.getY() + this.getHeight() - 410;
        int x = this.getX() + this.getWidth() - 415;
        ComposeMail cm = new ComposeMail(account, socket, x, y);
        cm.setVisible(true);
    }//GEN-LAST:event_jButtonThuMoiActionPerformed

    private void listInboxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listInboxValueChanged
        if (listInbox.getSelectedIndex() != -1) {
            this.setMailIndex(listInbox.getSelectedIndex());
        }
    }//GEN-LAST:event_listInboxValueChanged

    private void listOutboxValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listOutboxValueChanged
        if (listOutbox.getSelectedIndex() != -1) {
            this.setOutMailIndex(listOutbox.getSelectedIndex());
        }
    }//GEN-LAST:event_listOutboxValueChanged

    private void txtAttachmentsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txtAttachmentsMouseClicked

        try {
            DataPacket data = new DataPacket(this.account, this.emailToSelected, "##DOWNLOADFILE##", "", txtAttachments.getText());
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(data);
        } catch (IOException ex) {
            Logger.getLogger(MailClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_txtAttachmentsMouseClicked
    private void closeResources() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void jMenuThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuThoatActionPerformed
        try {
            if (socket != null && !socket.isClosed() && account != null && !account.getUsername().isEmpty()) {
                // Gửi gói tin thoát
                DataPacket exitPacket = new DataPacket(account, "", "##EXIT##", "", "");
                if (oos == null) {
                    oos = new ObjectOutputStream(socket.getOutputStream());
                }
                oos.writeObject(exitPacket);
                oos.flush();
            }
        } catch (IOException ex) {
            System.err.println("Lỗi khi gửi gói tin thoát: " + ex.getMessage());
        } finally {
            closeResources(); // Đóng tài nguyên
            System.exit(0);   // Thoát ứng dụng
        }
    }//GEN-LAST:event_jMenuThoatActionPerformed
    private void sendRequest(DataPacket dp) {
        try {
            if (oos == null) {
                oos = new ObjectOutputStream(socket.getOutputStream());
            }
            oos.writeObject(dp);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi gửi yêu cầu đến server.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object receiveResponse() {
        try {
            if (ois == null) {
                ois = new ObjectInputStream(socket.getInputStream());
            }

            // Kiểm tra nếu socket đã đóng
            if (socket.isClosed() || !socket.isConnected()) {
                System.err.println("Kết nối với server đã bị đóng.");
                return null;
            }
            // Đọc phản hồi từ server
            return ois.readObject();
        } catch (EOFException eof) {
            System.err.println("Phản hồi từ server kết thúc (EOF).");
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi khi đọc phản hồi từ server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void jMenuXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuXoaActionPerformed
      
    }//GEN-LAST:event_jMenuXoaActionPerformed
  
    public void setMailIndex(int i) {
        if (inboxMail != null && !inboxMail.isEmpty() && i >= 0 && i < inboxMail.size()) {
            Mail m = inboxMail.get(i);
            txtSubject.setText(m.getSubject());
            String pattern = "dd/MM/yyyy HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            txtTime.setText(sdf.format(m.getSentTime()));
            Account sender = new AccountDAO().getById(m.getSenderId());
            txtFrom.setText("From: " + sender.getUsername());
            txtBody.setText(m.getBody());
            txtCc.setText("CC: " + m.getCc());
            txtAttachments.setText(m.getAttachments());
            emailToSelected = new AccountDAO().getById(m.getReceiverId()).getUsername();
        } else {
            System.out.println("InboxMail list is empty or index is invalid.");
        }
    }

    public void setOutMailIndex(int i) {
        if (sentMail != null && !sentMail.isEmpty() && i >= 0 && i < sentMail.size()) {
            Mail m = sentMail.get(i);
            txtSubject.setText(m.getSubject());
            String pattern = "dd/MM/yyyy HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            txtTime.setText(sdf.format(m.getSentTime()));
            Account sender = new AccountDAO().getById(m.getSenderId());
            txtFrom.setText("From: " + sender.getUsername());
            txtBody.setText(m.getBody());
            txtCc.setText("CC: " + m.getCc());
            txtAttachments.setText(m.getAttachments());
            emailToSelected = new AccountDAO().getById(m.getReceiverId()).getUsername();
        } else {
            System.out.println("SentMail list is empty or index is invalid.");
        }
    }

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
            java.util.logging.Logger.getLogger(MailClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MailClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MailClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MailClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonThuMoi;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuThoat;
    private javax.swing.JMenuItem jMenuXoa;
    private javax.swing.JMenu jMenuXoaMail;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JList<String> listInbox;
    private javax.swing.JList<String> listOutbox;
    private javax.swing.JLabel txtAttachments;
    private javax.swing.JTextArea txtBody;
    private javax.swing.JLabel txtCc;
    private javax.swing.JLabel txtFrom;
    private javax.swing.JLabel txtSubject;
    private javax.swing.JLabel txtTime;
    // End of variables declaration//GEN-END:variables
}
