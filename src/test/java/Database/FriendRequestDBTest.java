package Database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Database.MySqlConnector;
import server.Model.FriendRequest;
import server.Model.User;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class FriendRequestDBTest {


    static Calendar albertoBirthday = Calendar.getInstance();
    static Calendar alonsoBirthday = Calendar.getInstance();
    static Calendar nicoBirthday = Calendar.getInstance();
    static Calendar unaiBirthday = Calendar.getInstance();

    static {
        albertoBirthday.set(2004, Calendar.JANUARY, 3);
        alonsoBirthday.set(2004, Calendar.JULY, 24);
        nicoBirthday.set(2004, Calendar.JANUARY, 24);
        unaiBirthday.set(2004, Calendar.FEBRUARY, 7);
    }

    static User Alberto = new User("Alberto", "password", albertoBirthday);
    static User Alonso = new User("Alonso", "password", alonsoBirthday);
    static User Nico = new User("Nico1", "password", nicoBirthday);
    static User Unai = new User("Unai1", "password", unaiBirthday);

    private static MySqlConnector connector;

    @BeforeAll
    public static void setup() throws SQLException {
        connector = new MySqlConnector();
        connector.connectDatabase();
    }

    @AfterAll
    public static void cleanup() {
        connector.closeDatabse();
    }

    @BeforeEach
    public void truncateTables() {
        connector.truncateTables();
    }


    // FriendRequests

    @Test
    public void createAndRetrieveFriendRequestThrowsError() {
        assertThrows(Exception.class, () -> connector.addFriendRequest(new FriendRequest(null, null, null)));
    }

    @Test
    public void createThenDeleteFriendRequestThrowsError() {
        assertThrows(Exception.class,() -> connector.deleteFriendRequest(new FriendRequest(null, null, null)));
    }

    // Find FR by requesters

    // By requester
    @Test
    public void retrieveFriendRequestByRequester() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        int size = connector.findFriendRequestsByRequester(Alonso).size();
        assertEquals(1, size);
        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByRequester(Alonso).iterator().next();
        assertEquals(friendRequest.getRequested().getUsername(), friendRequestRetrieved.getRequested().getUsername());
        assertEquals(friendRequest.getRequester().getUsername(), friendRequestRetrieved.getRequester().getUsername());
        assertEquals(friendRequest.getDate().getTimeInMillis() / 10000, friendRequestRetrieved.getDate().getTimeInMillis() / 10000);
    }

    @Test
    public void createThenDeleteFriendRequestByRequesterAndCheckList() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        int size = connector.findFriendRequestsByRequester(Alonso).size();
        assertEquals(1, size);
        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByRequester(Alonso).iterator().next();
        assertEquals(friendRequest.getRequested().getUsername(), friendRequestRetrieved.getRequested().getUsername());
        assertEquals(friendRequest.getRequester().getUsername(), friendRequestRetrieved.getRequester().getUsername());
        assertEquals(1, connector.findFriendRequestsByRequester(Alonso).size());
        connector.deleteFriendRequest(friendRequest);
        assertEquals(0, connector.findFriendRequestsByRequester(Alonso).size());
    }

    @Test
    public void nonexistentUserWhileRetrievingFriendRequestByRequester() {
        assertTrue(connector.findFriendRequestsByRequester(Alonso).isEmpty());
    }

    // By requested
    @Test
    public void createFriendRequestsByRequestedAndCheckList() {
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addFriendRequest(friendRequest);
        int size = connector.findFriendRequestsByRequested(Alberto).size();
        assertEquals(1, size);
        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByRequested(Alberto).iterator().next();
        assertEquals(friendRequest.getRequested().getUsername(), friendRequestRetrieved.getRequested().getUsername());
        assertEquals(friendRequest.getRequester().getUsername(), friendRequestRetrieved.getRequester().getUsername());
    }

    @Test
    public void createThenDeleteFriendRequestByRequestedAndCheckList() {
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addFriendRequest(friendRequest);
        assertEquals(1, connector.findFriendRequestsByRequested(Alberto).size());
        connector.deleteFriendRequest(friendRequest);
        assertEquals(0, connector.findFriendRequestsByRequested(Alberto).size());
    }

    @Test
    public void nonexistentUserWhileRetrievingFriendRequestByRequested() {
        assertTrue(connector.findFriendRequestsByRequested(Alonso).isEmpty());
    }

    // Both users
    @Test
    public void createFriendRequestByBothUsersAndCheck() {
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByBothUsers(Alonso, Alberto).get();
        assertEquals(friendRequest.getRequester().getUsername(), friendRequestRetrieved.getRequester().getUsername());
        assertEquals(friendRequest.getRequested().getUsername(), friendRequestRetrieved.getRequested().getUsername());
    }

    @Test
    public void createThenDeleteFriendRequestByBothUsersAndCheck() {
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        assertFalse(connector.findFriendRequestsByBothUsers(Alonso, Alberto).isEmpty());
        connector.deleteFriendRequest(friendRequest);
        assertTrue(connector.findFriendRequestsByBothUsers(Alonso, Alberto).isEmpty());
    }

    @Test
    public void nonexistentFriendRequestWhileRetrievingFriendRequestsByBothUsers() {
        assertTrue(connector.findFriendRequestsByBothUsers(Alonso, Alberto).isEmpty());
    }

    // Friends
    @Test
    public void addFriend() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);
        Set<User> friendsAlonso = connector.findFriendsFromUser(Alonso);
        assertEquals(1, friendsAlonso.stream().filter(user -> user.getUsername().equals(Alberto.getUsername())).count());
        Set<User> friendsAlberto = connector.findFriendsFromUser(Alberto);
        assertEquals(1, friendsAlberto.stream().filter(user -> user.getUsername().equals(Alonso.getUsername())).count());
    }

    @Test
    public void addFriendThrowsError() {
        assertThrows(Exception.class, () -> connector.addFriend(new FriendRequest(null, null, null)));
    }

    @Test
    public void deleteFriend() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);
        Set<User> friendsRestrievedBeforeDelete = connector.findFriendsFromUser(Alonso);
        assertTrue(friendsRestrievedBeforeDelete.stream().anyMatch(user -> user.getUsername().equals(Alberto.getUsername())));
        connector.deleteFriend(friendRequest);
        Set<User> friendsRestrievedAfterDelete = connector.findFriendsFromUser(Alonso);
        assertTrue(friendsRestrievedAfterDelete.stream().noneMatch(user -> user.getUsername().equals(Alberto.getUsername())));
    }

    @Test
    public void deleteFriendThrowsError() {
        assertThrows(Exception.class, () -> connector.deleteFriend(new FriendRequest(null, null, null)));
    }

    @Test
    public void retrieveAFriendFromExistingUser() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);
        assertEquals(1, connector.findFriendsFromUser(Alonso).stream().filter(user -> user.getUsername().equals(Alberto.getUsername())).count());
    }

    @Test
    public void retrieveNonexistentFriend() {
        assertTrue(connector.findFriendsFromUser(Alonso).isEmpty());
    }

    @Test
    public void retrieveFriendsGroupFromExistingUser() {
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addUser(Nico);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        FriendRequest friendRequest2 = new FriendRequest(Alonso, Nico, Calendar.getInstance());
        HashSet<User> friends = new HashSet<>(Set.of(Alberto, Nico));
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);
        connector.addFriendRequest(friendRequest2);
        connector.addFriend(friendRequest2);
        Set<User> friendsRestrieved = connector.findFriendsFromUser(Alonso);
        for (User friend : friends) {
            assertTrue(friendsRestrieved.stream().anyMatch(user -> user.getUsername().equals(friend.getUsername())));
        }
    }
}
