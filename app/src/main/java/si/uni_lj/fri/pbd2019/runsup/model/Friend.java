package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.Date;

public class Friend implements Serializable {


    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long friendUserId;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String name;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String profileImageUrl;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date friendsSince;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private Date dateJoined;


    public Friend() {}

    public Friend(long friendUserId, String name, String surname, String profileImageUrl, Date friendsSince, Date dateJoined) {
        this.friendUserId = friendUserId;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.friendsSince = friendsSince;
        this.dateJoined = dateJoined;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(long friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Date getFriendsSince() {
        return friendsSince;
    }

    public void setFriendsSince(Date friendsSince) {
        this.friendsSince = friendsSince;
    }

    public Date getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(Date dateJoined) {
        this.dateJoined = dateJoined;
    }
}
