package si.uni_lj.fri.pbd2019.runsup.model;

import com.j256.ormlite.field.DatabaseField;

public class FriendRequest {
    @DatabaseField(generatedId = true, useGetSet = true)
    private long id;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private long idUser;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String name;
    @DatabaseField(canBeNull = false, useGetSet = true)
    private String profileImageUrl;

    public FriendRequest() {}

    public FriendRequest(long idUser, String name, String profileImageUrl) {
        this.idUser = idUser;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
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
}
