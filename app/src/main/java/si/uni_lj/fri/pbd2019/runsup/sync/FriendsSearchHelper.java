package si.uni_lj.fri.pbd2019.runsup.sync;

import si.uni_lj.fri.pbd2019.runsup.FriendsActivity;
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

    public void sendFriendRequest(long idUser, long idFriend, String currentUserName, String currentUserImageUrl, String jwt, FriendsSearchActivity.GetSendFriendRequestResponse getSendFriendRequestResponse) {
        this.apiCallHelper.sendFriendRequest(idUser, idFriend, currentUserName, currentUserImageUrl, jwt, getSendFriendRequestResponse);
    }

    public void acceptFriendRequest(long idUser, long idFriend, String jwt, FriendsActivity.GetAcceptFriendRequestResponse getAcceptFriendRequestResponse) {
        this.apiCallHelper.acceptFriendRequest(idUser, idFriend, jwt, getAcceptFriendRequestResponse);
    }

    public void fetchFriendRequests(long idUser, String jwt, FriendsActivity.GetFetchFriendsRequestResponse getFetchFriendsRequestResponse) {
        this.apiCallHelper.fetchFriendRequests(idUser, jwt, getFetchFriendsRequestResponse);
    }

    public void getJwt(long idUser, String name, String profileImageUrl, FriendsActivity.GetJwtRequestResponse getJwtRequestResponse) {
        this.apiCallHelper.loginOrSignUp(idUser, name, profileImageUrl, getJwtRequestResponse);
    }

    public void fetchFriends(long idUser, String jwt, FriendsActivity.GetFetchFriendsResponse getFetchFriendsResponse) {
        this.apiCallHelper.fetchFriends(idUser, jwt, getFetchFriendsResponse);
    }

    public void sendMessage(long idUser, long idReceiver, String jwt, String message, FriendsActivity.GetSendMessageRequestResponse getSendMessageRequestResponse) {
        this.apiCallHelper.sendMessage(idUser, idReceiver, jwt, message, getSendMessageRequestResponse);
    }

    public void unfriend(long idUser, long friendUserId, String jwt, FriendsActivity.GetUnfriendRequestResponse getUnfriendRequestResponse) {
        this.apiCallHelper.unfriend(idUser, friendUserId, jwt, getUnfriendRequestResponse);
    }
}
