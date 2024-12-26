package Models;

import java.io.Serializable;

/**
 *
 * @author Admin
 */
public class DataPacket implements Serializable {

    private Account fromAccount;
    private String to;
    private String cc;
    private String subject;
    private String body;
    private String attachments;
    private byte[] file;

    // Constructor mặc định (không tham số)
    public DataPacket() {
        this.fromAccount = null;
        this.to = "";
        this.cc = "";
        this.subject = "";
        this.body = "";
        this.attachments = "";
        this.file = null;
    }

    // Constructor đầy đủ
    public DataPacket(Account from, String to, String cc, String subject, String body, String attachments) {
        this.fromAccount = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    // Constructor không có CC
    public DataPacket(Account from, String to, String subject, String body, String attachments) {
        this.fromAccount = from;
        this.to = to;
        this.cc = "";
        this.subject = subject;
        this.body = body;
        this.attachments = attachments;
    }

    // Getter và Setter cho tất cả các thuộc tính
    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public Account getFrom() {
        return fromAccount;
    }

    public void setFrom(Account from) {
        this.fromAccount = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
