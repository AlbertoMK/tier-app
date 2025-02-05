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
    public void addFriendRequest(FriendRequest friendRequest);
    public void deleteFriendRequest(FriendRequest friendRequest);
    public void addFriend(FriendRequest friendRequest);
    public void deleteFriend(FriendRequest friendRequest);
    public Set<FriendRequest> findFriendRequestsByRequester(User requester);
    public Set<FriendRequest> findFriendRequestsByRequested(User requested);
    public FriendRequest findFriendRequestsByBothUsers(User requester, User requested);
    public User findFriendFromFriend(User friend);
}
