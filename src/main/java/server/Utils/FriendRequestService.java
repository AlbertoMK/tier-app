package server.Utils;

import server.Database.UserRepository;
import server.Model.User;

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

    public boolean addRequest(String requester, String requested) {
        if (getRequestsAsRequester(requester).contains(requested))
            return false;
        User userRequester = userRepository.findByUsername(requester).get();
        User userRequested = userRepository.findByUsername(requested).get();
        userRepository.addFriendRequest(userRequester, userRequested);
        return true;
    }

    public boolean removeRequest(String requester, String requested) {
        if (getRequestsAsRequester(requester).contains(requested)) {
            User userRequester = userRepository.findByUsername(requester).get();
            User userRequested = userRepository.findByUsername(requested).get();
            userRepository.deleteFriendRequest(userRequester, userRequested);
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
        return userRepository.findFriendRequestsByRequester(userRequested).stream()
                  .map(friendRequest -> friendRequest.getRequester().getUsername())
                  .collect(Collectors.toSet());
    }
}
