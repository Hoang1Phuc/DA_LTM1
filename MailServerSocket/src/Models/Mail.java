/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Admin
 */
public class Mail implements Serializable {

    private int id;
    private int senderId;
    private int receiverId;
    private String subject;
    private String body;
    private Date sentTime;
    private String state;
    private boolean isDeleted;
    private String cc;
    private String attachments;

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public Mail(int id, int senderId, int receiverId, String subject, String body, Date sentTime, String state, boolean isDeleted, String cc, String attachments) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.body = body;
        this.sentTime = sentTime;
        this.state = state;
        this.isDeleted = isDeleted;
        this.cc = cc;
        this.attachments = attachments;

    }

    public Mail(int senderId, int receiverId, String subject, String body, Date sentTime, String cc, String attachments) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.body = body;
        this.sentTime = sentTime;
        this.state = "unread";
        this.isDeleted = false;
        this.cc = cc;
        this.attachments = attachments;
    }

    public Mail(DataPacket dp, int receiverId) {
        this.senderId = dp.getFrom().getId();
        this.receiverId = receiverId;
        this.subject = dp.getSubject();
        this.body = dp.getBody();
        this.sentTime = new Date();
        this.state = "unread";
        this.isDeleted = false;
        this.cc = dp.getCc();
        this.attachments = dp.getAttachments();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
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

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getListFieldValues() {
        String pattern = "yyyyMMdd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return senderId + ", " + receiverId + ", N'" + subject + "', N'" + body + "', '" + sdf.format(sentTime) + "', '" + state + "', " + (isDeleted ? 1 : 0) + ", '" + cc + "', N'" + attachments + "'";
    }
}
