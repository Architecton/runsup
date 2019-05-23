package si.uni_lj.fri.pbd2019.runsup.sync;

import java.util.Date;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;

public class CloudDataFriend {

    private long id;
    private long friendUserId;
    private String name;
    private String surname;
    private String profileImageUrl;
    private long friendsSince;
    private long dateJoined;

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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getFriendsSince() {
        return friendsSince;
    }

    public void setFriendsSince(long friendsSince) {
        this.friendsSince = friendsSince;
    }

    public long getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(long dateJoined) {
        this.dateJoined = dateJoined;
    }

    Friend toFriend() {
        Friend res = new Friend();
        res.setId(this.id);
        res.setName(this.name);
        res.setFriendUserId(this.friendUserId);
        res.setProfileImageUrl(this.profileImageUrl);
        res.setDateJoined(new Date(this.dateJoined));
        res.setFriendsSince(new Date(this.friendsSince));
        return res;
    }
}
