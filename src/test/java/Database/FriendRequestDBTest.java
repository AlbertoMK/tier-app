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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        connector.truncateUserTables();
    }


    // FriendRequests

    @Test
    public void createAndRetrieveFriendRequestByBothUsers() {
//        Alonso -> Alberto
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByBothUsers(Alonso, Alberto);
        assertEquals(friendRequest.getRequester(), friendRequestRetrieved.getRequester());
        assertEquals(friendRequest.getRequested(), friendRequestRetrieved.getRequested());
    }

    // Sin haber una friendRequest entre Unai y Nico
    @Test
    public void retrieveUnexistentFriendRequest() {
        assertEquals(null, connector.findFriendRequestsByBothUsers(Unai, Nico));
    }

    @Test
    public void unexistentUserWhileRetrievingFriendRequest() {
        assertEquals(null, connector.findFriendRequestsByBothUsers(null, Nico));
    }

    @Test
    public void retrieveFriendRequestByRequester() {
        //       Alonso -> Alberto
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addFriendRequest(friendRequest);

        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByRequester(Alonso).iterator().next(); // Falla la fecha :mirar:

        assertEquals(1, connector.findFriendRequestsByRequester(Alonso).size());
        assertEquals(friendRequest.getRequested(), friendRequestRetrieved.getRequested());
        assertEquals(friendRequest.getRequester(), friendRequestRetrieved.getRequester());
    }

    @Test
    public void unexistentUserWhileRetrievingFriendRequestByRequester() {
        assertEquals(0, connector.findFriendRequestsByRequester(null).size());
    }

    @Test
    public void unexistentUserWhileRetrievingFriendRequestByRequested() {
        assertEquals(0, connector.findFriendRequestsByRequested(null).size());
    }


    @Test
    public void retrieveFriendRequestByRequested() {
        //       Alberto -> Alonso
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addFriendRequest(friendRequest);

        FriendRequest friendRequestRetrieved = connector.findFriendRequestsByRequested(Alberto).iterator().next(); // Falla la fecha :mirar:

        assertEquals(1, connector.findFriendRequestsByRequested(Alberto).size());
        assertEquals(friendRequest.getRequested(), friendRequestRetrieved.getRequested());
        assertEquals(friendRequest.getRequester(), friendRequestRetrieved.getRequester());
    }

    @Test
    public void deleteFriendRequest() {
        //       Alberto -> Alonso
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        connector.addFriendRequest(friendRequest);
        assertEquals(1, connector.findFriendRequestsByRequested(Alberto).size());
        connector.deleteFriendRequest(friendRequest);
        assertEquals(0, connector.findFriendRequestsByRequested(Alberto).size());
    }


    // Friend

    @Test
    public void retrieveFriend() {
        // Alonso -> Alberto
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);

        assertEquals(Alberto, connector.findFriendFromFriend(Alonso));
    }

    @Test
    public void errorWhileRetrievingFriend() {
        assertEquals(null, connector.findFriendFromFriend(null));
    }


    @Test
    public void deleteFriend() {
        // Alonso -> Alberto
        connector.addUser(Alonso);
        connector.addUser(Alberto);
        FriendRequest friendRequest = new FriendRequest(Alonso, Alberto, Calendar.getInstance());
        connector.addFriendRequest(friendRequest);
        connector.addFriend(friendRequest);
        assertEquals(Alberto, connector.findFriendFromFriend(Alonso));
        connector.deleteFriend(friendRequest);
        assertEquals(null, connector.findFriendFromFriend(Alonso));
    }

    @Test
    public void retrieveUnexistentFriend() {
        assertEquals(null, connector.findFriendFromFriend(new User("Juanlu", "patata", Calendar.getInstance())));
    }

}
