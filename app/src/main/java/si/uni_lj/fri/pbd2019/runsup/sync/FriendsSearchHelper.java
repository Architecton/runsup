package si.uni_lj.fri.pbd2019.runsup.sync;

import si.uni_lj.fri.pbd2019.runsup.FriendsSearchActivity;
import si.uni_lj.fri.pbd2019.runsup.model.User;

public class FriendsSearchHelper {
    private ApiCallHelper apiCallHelper;
    private String baseUrl;

    public FriendsSearchHelper(String baseUrl) {
       this.baseUrl = baseUrl;
       this.apiCallHelper = new ApiCallHelper(baseUrl);
    }

    public void searchForFriends(String searchInput, User currentUser, String name, String profileImageUrl, FriendsSearchActivity.GetFriendsSearchResponse getFriendsSearchResponse) {
        this.apiCallHelper.loginOrSignupAndSearchFriends(searchInput, currentUser, name, profileImageUrl, getFriendsSearchResponse);
    }

    public void sendFriendRequest(long idFriend, String jwt, FriendsSearchActivity.GetSendFriendRequestResponse getSendFriendRequestResponse) {
        this.apiCallHelper.sendFriendRequest(idFriend, jwt, getSendFriendRequestResponse);
    }

    public void acceptFriendRequest(long idFriend, String jwt, FriendsSearchActivity.GetAcceptFriendRequestResponse getAcceptFriendRequestResponse) {
        this.apiCallHelper.acceptFriendRequest(idFriend, jwt, getAcceptFriendRequestResponse);
    }
}
