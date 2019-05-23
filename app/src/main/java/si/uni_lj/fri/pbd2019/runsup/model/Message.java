package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class Message {

    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(foreign=true, foreignAutoRefresh=true)
    private User userSender;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long idReceiver;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long idSender;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String content;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date sentDate;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String profileImageUri;

    public Message() {}

    public Message(long idReceiver, long idSender, User userSender, String content, Date sentDate, String profileImageUri) {
        this.idReceiver = idReceiver;
        this.idSender = idSender;
        this.userSender = userSender;
        this.content = content;
        this.sentDate = sentDate;
        this.profileImageUri = profileImageUri;
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

    public void setIdReceiver(long idReciever) {
        this.idReceiver = idReciever;
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

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public String getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(String profileImageUri) {
        this.profileImageUri = profileImageUri;
    }
}
