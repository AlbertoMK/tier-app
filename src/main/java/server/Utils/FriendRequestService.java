package server.Utils;

import server.Database.UserRepository;
import server.Model.FriendRequest;
import server.Model.User;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FriendRequestService {

    private UserRepository userRepository;
    private static FriendRequestService instance;

    private FriendRequestService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static FriendRequestService getInstance() {
        if (instance.userRepository == null)
            throw new RuntimeException("Please, call init method first.");
        return instance;
    }

    public static void init(UserRepository userRepository) {
        instance = new FriendRequestService(userRepository);
    }

    public boolean addRequest(FriendRequest friendRequest) {
        String requested = friendRequest.getRequested().getUsername();
        String requester = friendRequest.getRequester().getUsername();

        if (getRequestsAsRequester(requester).contains(requested))
            return false;
        userRepository.addFriendRequest(friendRequest);
        return true;
    }

    public boolean removeRequest(FriendRequest friendRequest) {
        String requested = friendRequest.getRequested().getUsername();
        String requester = friendRequest.getRequester().getUsername();

        if (getRequestsAsRequester(requester).contains(requested)) {
            userRepository.deleteFriendRequest(friendRequest);
            return true;
        } else
            return false;
    }

    // return a Set containing the requested users' usernames
    public Set<String> getRequestsAsRequester(String requester) {
        User userRequester = userRepository.findByUsername(requester).get();
        return userRepository.findFriendRequestsByRequester(userRequester).stream()
                  .map(friendRequest -> friendRequest.getRequested().getUsername())
                  .collect(Collectors.toSet());
    }

    // return a Set containing the requesters' usernames
    public Set<String> getRequestsAsRequested(String requested) {
        User userRequested = userRepository.findByUsername(requested).get();
        return userRepository.findFriendRequestsByRequested(userRequested).stream()
                  .map(friendRequest -> friendRequest.getRequester().getUsername())
                  .collect(Collectors.toSet());
    }

    // verifies if there is a friend request with the requester & requested given
    public boolean friendRequestExists(User requester, User requested ) {
        return getRequestsAsRequester(requester.getUsername()).contains(requested.getUsername());
    }
}
