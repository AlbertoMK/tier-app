package server.Database;

import server.Model.FriendRequest;
import server.Model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {

    public Optional<User> findByUsername(String username);
    public List<User> findAllUsers();
    public void addUser(User user);
    public void updateUser(User user); // updates the user with the username given with the new attributes
    public void addFriendship(User user1, User user2);
    public void addFriendRequest(User requester, User requested);
    public void deleteFriendRequest(User requester, User requested);
    public Set<FriendRequest> findFriendRequestsByRequester(User requester);
    public Set<FriendRequest> findFriendRequestsByRequested(User requested);
}
