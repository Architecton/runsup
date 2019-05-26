package si.uni_lj.fri.pbd2019.runsup.sync;

import si.uni_lj.fri.pbd2019.runsup.model.FriendRequest;

public class CloudDataFriendRequest {
    private String name;
    private String profileImageUrl;
    private long idUser;

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

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public FriendRequest toFriendRequest() {
        FriendRequest res = new FriendRequest();
        res.setIdUser(this.idUser);
        res.setName(this.name);
        res.setProfileImageUrl(this.profileImageUrl);
        return res;
    }
}
