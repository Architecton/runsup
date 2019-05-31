package si.uni_lj.fri.pbd2019.runsup.sync;

import java.util.ArrayList;

import si.uni_lj.fri.pbd2019.runsup.model.Friend;
import si.uni_lj.fri.pbd2019.runsup.model.Message;
import si.uni_lj.fri.pbd2019.runsup.model.User;
import si.uni_lj.fri.pbd2019.runsup.model.Workout;

public class CloudContentUpdatesFetchHelper {

    private ApiCallHelper apiCallHelper;
    private String baseUrl;


    // ### Callback Interfaces ###
    public interface FetchMessagesResponse {
        void response(ArrayList<Message> messages);
    }

    public interface FetchFriendRequestsResponse {
        void response(ArrayList<Friend> friendshipRequesters);
    }

    public interface FetchSharedWorkoutsResponse {
        void response(ArrayList<Workout> workouts);
    }

    public interface SendMessageResponse {
        void response(boolean result);
    }

    public interface GetLastMessageIdRequestResponse {
        void response(Long id);
    }

    // ### /Callback Interfaces ###


    // constructor
    public CloudContentUpdatesFetchHelper(String baseUrl) {
        this.baseUrl = baseUrl;
        this.apiCallHelper = new ApiCallHelper(baseUrl);
    }


    // fetchMessages: fetch user's messages.
    public void fetchMessages(User user, String jwt, FetchMessagesResponse fetchMessagesResponse) {
        // this.apiCallHelper.fetchMessages(user, jwt, fetchMessagesResponse);
        // TODO
    }

    // fetchFriendRequests: fetch user's pending friend requests.
    public void fetchFriendRequests(User user, String jwt, FetchFriendRequestsResponse fetchFriendRequestsResponse) {
        // this.apiCallHelper.fetchFriendRequests(user, jwt, fetchFriendRequestsResponse);
        // TODO
    }

    // fetchSharedWorkouts: fetch user's received shared workouts.
    public void fetchSharedWorkouts(User user, String jwt, FetchSharedWorkoutsResponse fetchSharedWorkoutsResponse) {
        // this.apiCallHelper.fetchSharedWorkouts(user, jwt, fetchSharedWorkoutsResponse);
        // TODO
    }

}
