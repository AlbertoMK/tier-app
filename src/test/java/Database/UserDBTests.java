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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserDBTests {

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

    @Test
    public void createAndRetrieveUser() {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        User user1 = new User("nombre", "password", date);
        connector.addUser(user1);
        User retrievedUser = connector.findByUsername("nombre").get();
        assertEquals(user1.getUsername(), retrievedUser.getUsername());
        assertEquals(user1.getPassword(), retrievedUser.getPassword());
        assertEquals(user1.getDateOfBirth().getTimeInMillis(), retrievedUser.getDateOfBirth().getTimeInMillis());
    }

    @Test
    public void retrieveUnexistentUser() {
        assertTrue(connector.findByUsername("nombre").isEmpty());
    }

    @Test
    public void findSeveralUsers() {
        User user1 = new User("nombre1", "password", Calendar.getInstance());
        User user2 = new User("nombre2", "password", Calendar.getInstance());
        User user3 = new User("nombre3", "password", Calendar.getInstance());

        connector.addUser(user1);
        connector.addUser(user2);
        connector.addUser(user3);

        List<User> users = connector.findAllUsers();
        assertTrue(users.size() == 3);
        assertTrue(users.stream().map(User::getUsername).collect(Collectors.toSet())
                .containsAll(Set.of("nombre1", "nombre2", "nombre3")));
    }

    @Test
    public void findEmptySetUsers() {
        List<User> users = connector.findAllUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    public void addAndRetrieveFriendRequest() {
        User requester = new User("requester", "password", Calendar.getInstance());
        User requested = new User("requested", "password", Calendar.getInstance());
        connector.addUser(requester);
        connector.addUser(requested);
        Calendar date = Calendar.getInstance();
        date.set(Calendar.MILLISECOND, 0);
        connector.addFriendRequest(new FriendRequest(requester, requested, date));
        Set<FriendRequest> requestes = connector.findFriendRequestsByRequester(requester);
        Set<FriendRequest> requestes2 = connector.findFriendRequestsByRequested(requested);

        assertEquals(1, requestes.size());
        assertEquals(1, requestes2.size());
        FriendRequest requestByRequester = requestes.iterator().next();
        FriendRequest requestByRequested = requestes2.iterator().next();
        assertEquals(requester.getUsername(), requestByRequester.getRequester().getUsername());
        assertEquals(requester.getUsername(), requestByRequested.getRequester().getUsername());
        assertEquals(requested.getUsername(), requestByRequester.getRequested().getUsername());
        assertEquals(requested.getUsername(), requestByRequested.getRequested().getUsername());
        assertEquals(date.getTimeInMillis(), requestByRequester.getDate().getTimeInMillis());
        assertEquals(date.getTimeInMillis(), requestByRequested.getDate().getTimeInMillis());

        Set<FriendRequest> requestes3 = connector.findFriendRequestsByRequester(requested);
        Set<FriendRequest> requestes4 = connector.findFriendRequestsByRequested(requester);

        assertTrue(requestes3.isEmpty());
        assertTrue(requestes4.isEmpty());
    }

    @Test
    public void deleteFriendRequest() {
        User requester = new User("requester", "password", Calendar.getInstance());
        User requested = new User("requested", "password", Calendar.getInstance());
        connector.addUser(requester);
        connector.addUser(requested);
        FriendRequest friendRequest = new FriendRequest(requester, requested, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        Set<FriendRequest> requestes = connector.findFriendRequestsByRequester(requester);
        assertEquals(1, requestes.size());
        connector.deleteFriendRequest(friendRequest);
        requestes = connector.findFriendRequestsByRequester(requester);
        assertEquals(0, requestes.size());
    }
}
