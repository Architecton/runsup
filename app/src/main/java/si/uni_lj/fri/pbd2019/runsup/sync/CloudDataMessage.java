package si.uni_lj.fri.pbd2019.runsup.sync;

import java.util.Date;

import si.uni_lj.fri.pbd2019.runsup.model.Message;

class CloudDataMessage {

    private long id;
    private long idReceiver;
    private long idSender;
    private String content;
    private Date sentDate;
    private String profileImageUri;
    private String senderName;


    public CloudDataMessage(long id, long idReceiver, long idSender, String content, Date sentDate, String profileImageUri, String senderName) {
        this.id = id;
        this.idReceiver = idReceiver;
        this.idSender = idSender;
        this.content = content;
        this.sentDate = sentDate;
        this.profileImageUri = profileImageUri;
        this.senderName = senderName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(long idReceiver) {
        this.idReceiver = idReceiver;
    }

    public long getIdSender() {
        return idSender;
    }

    public void setIdSender(long idSender) {
        this.idSender = idSender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    Message toMessage() {
        Message res = new Message();
        res.setContent(this.content);
        res.setIdReceiver(this.idReceiver);
        res.setIdSender(this.idSender);
        res.setProfileImageUri(this.getProfileImageUri());
        res.setSenderName(this.senderName);
        res.setSentDate(this.sentDate);
        return res;
    }
}
