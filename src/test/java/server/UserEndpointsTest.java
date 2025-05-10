package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Controllers.UserController;
import server.Database.MySqlConnector;
import server.Model.FriendRequest;
import server.Model.LazyReference;
import server.Model.User;
import server.Utils.FriendRequestService;
import server.Utils.UserTokenService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserEndpointsTest extends ServerEndpointsTest {

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

    @BeforeEach
    public void reset() {
        Alberto = new User("Alberto", "password", albertoBirthday);
        Alonso = new User("Alonso", "password", alonsoBirthday);
        Nico = new User("Nico1", "password", nicoBirthday);
        Unai = new User("Unai1", "password", unaiBirthday);
    }

    @BeforeAll
    public static void initialize() throws IOException {
        startServer();
    }

    @AfterAll
    public static void cleanUp() {
        stopServer();
    }

    @Test
    public void unexpectedEndpointTestInPOST() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);
        try {
            HttpResponse<String> response = makeHttpRequest("user/pochoclo", HttpMethod.POST, "");
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Unrecognized endpoint"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void userFindByUsernameEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user?username=" + Alberto.getUsername(), HttpMethod.GET, "").body();
            assertEquals(new ObjectMapper().writeValueAsString(Alberto), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindByUsernameNotFoundEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            HttpResponse<String> response = makeHttpRequest("user?username=" + Alberto.getUsername(), HttpMethod.GET, "");
            assertEquals("User not found", response.body());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindAllEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findAllUsers()).thenReturn(List.of(Alberto, Alonso, Nico, Unai));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user", HttpMethod.GET, "").body();
            assertEquals(new ObjectMapper().writeValueAsString(List.of(Alberto, Alonso, Nico, Unai)), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindAllEmptyEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findAllUsers()).thenReturn(new ArrayList<>());
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user", HttpMethod.GET, "").body();
            assertEquals(new ObjectMapper().writeValueAsString(new ArrayList<>()), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createValidUserEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Alberto);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector).addUser(any());
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertEquals("Successfully created a new user", response.body());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createRepeatedUsernameUserEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Alberto);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector, never()).addUser(any());
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertEquals("Username is already taken", response.body());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createShortUsernameUserEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(anyString())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        User user1 = new User("ABC", "password", Calendar.getInstance());
        try {
            String requestBody = new ObjectMapper().writeValueAsString(user1);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector, never()).addUser(any());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Username too short."));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createTooLongUsernameUserEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(anyString())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        User user1 = new User("ABCDQWERTIYOPASDFGHJKLÑZXCMVNAS", "password", Calendar.getInstance());
        try {
            String requestBody = new ObjectMapper().writeValueAsString(user1);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector, never()).addUser(any());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Username too long."));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createShortPasswordUserEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(anyString())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        User user1 = new User("username", "p1", Calendar.getInstance());
        try {
            String requestBody = new ObjectMapper().writeValueAsString(user1);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector, never()).addUser(any());
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Password too short."));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void validLoginEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        User user1 = new User("alberto", UserController.hashPassword("password"), Calendar.getInstance());
        when(mySqlConnector.findByUsername("alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "password"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("session_token"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void wrongPasswordLoginEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", Alberto.getUsername(), "password", "anotherPassword"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertTrue(response.body().contains("Incorrect password for given username"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userNotFoundLoginEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        when(mySqlConnector.findByUsername("alberto")).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "password"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Username not found"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void invalidRequestLoginEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("user", "alberto", "pass", "password"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("The request must include an username and a password."));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void addFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request sent"));
            verify(mySqlConnector).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void testAcceptFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest friendRequest = new FriendRequest(Unai, Alberto, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Unai)).thenReturn(Set.of(friendRequest));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requester", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request accepted"));
            verify(mySqlConnector).addFriend(any());
            verify(mySqlConnector).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void testDeclineFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest friendRequest = new FriendRequest(Unai, Alberto, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Unai)).thenReturn(Set.of(friendRequest));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requester", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend/reject", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request rejected"));
            verify(mySqlConnector).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void addMutualFriendRequestBetweenUsers(){
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alonso.getUsername())).thenReturn(Optional.of(Alonso));
        when(mySqlConnector.findByUsername(Nico.getUsername())).thenReturn(Optional.of(Nico));

        FriendRequest nicoToAlon = new FriendRequest(Nico,Alonso,Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Nico)).thenReturn(Set.of(nicoToAlon));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Nico.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Alonso.getUsername()));
            makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("This user has already sent or received a friend request"));
        }catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
    @Test
    public void addRepeatedFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of(new FriendRequest(Alberto, Unai, Calendar.getInstance())));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("This user has already sent or received a friend request"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenFriendRequestTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertTrue(response.body().contains("Token not valid or not present"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingRequestedFriendRequestTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Missing attribute: requested"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requesterNotFoundFriendRequestTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
    @Test
    public void requestedEqualToRequesterTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Nico.getUsername())).thenReturn(Optional.of(Nico));

        try {
            String token = UserTokenService.generateToken(Nico.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Nico.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("You can't send a friend request to yourself"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
    @Test
    public void requestedNotFoundFriendRequestTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void removeFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest request = new FriendRequest(Alberto, Unai, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of(request));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend_request", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request removed"));
            verify(mySqlConnector).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void removeUnexistentFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend_request", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("This user hasn't an existing friend request"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenRemoveFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest request = new FriendRequest(Alberto, Unai, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of(request));

        FriendRequestService.init(mySqlConnector);
        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertTrue(response.body().contains("Token not valid or not present"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingRequestedRemoveFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token));
            HttpResponse<String> response = makeHttpRequest("user/friend_request", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Missing attribute: requested"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requesterNotFoundRemoveFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend_request", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requestedNotFoundRemoveFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.empty());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend_request", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void unrecognizedEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", "value"));
            HttpResponse<String> response = makeHttpRequest("user/unknownEndpoint", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Unrecognized endpoint"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenAcceptFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of()); // Without token
            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertTrue(response.body().contains("Token not valid or not present"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void missingRequesterAcceptFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requested", Unai.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Missing attribute: requester"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void usersAlreadyFriendsTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        User user1 = new User("nombre1", "pass1", Calendar.getInstance());
        User user2 = new User("nombre2", "pass2", Calendar.getInstance());
        user1.setFriends(Set.of(new LazyReference<>(user2)));
        user2.setFriends(Set.of(new LazyReference<>(user1)));

        when(mySqlConnector.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(mySqlConnector.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        FriendRequest friendRequest = new FriendRequest(user2, user1, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(user2)).thenReturn(Set.of(friendRequest));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(user1.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", user2.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("You are already friends"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void noPendingFriendRequestTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", Alberto.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("No pending friend request found"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void requesterNotFoundAcceptFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", Alberto.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void requestedNotFoundAcceptFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", Alberto.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenRejectFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of()); // Without token
            HttpResponse<String> response = makeHttpRequest("user/friend/reject", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertTrue(response.body().contains("Token not valid or not present"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void missingRequesterRejectFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/reject", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Missing attribute: requester"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void requesterNotFoundRejectFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", Alberto.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/reject", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void requestedNotFoundRejectFriendRequest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));

        FriendRequestService.init(mySqlConnector);

        try {
            String token = UserTokenService.generateToken(Unai.getUsername());

            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "requester", Alberto.getUsername()
            ));

            HttpResponse<String> response = makeHttpRequest("user/friend/reject", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));

        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    // GET methods

    @Test
    public void unexpectedEndpointTestInGET() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);
        try {
            HttpResponse<String> response = makeHttpRequest("user/pochoclo", HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Unrecognized endpoint"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happened: " + ex.getMessage());
        }
    }

    @Test
    public void getIncomingRequests() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        Calendar date = Calendar.getInstance();
        FriendRequest request1 = new FriendRequest(Unai, Alberto, date);
        FriendRequest request2 = new FriendRequest(Alonso, Alberto, date);

        when(connector.findFriendRequestsByRequested(Alberto)).thenReturn(Set.of(request1, request2));
        when(connector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(connector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(connector.findByUsername(Alonso.getUsername())).thenReturn(Optional.of(Alonso));
        FriendRequestService.init(connector);

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/incoming?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            List<Map<String, Object>> list = new ObjectMapper().readValue(response.body(), List.class);
            assertEquals(2, list.size());
            assertEquals(1, list.stream().filter(request -> request.get("requester").equals(Unai.getUsername()) &&
                    request.get("date").equals(date.getTimeInMillis())).toList().size());
            assertEquals(1, list.stream().filter(request -> request.get("requester").equals(Alonso.getUsername()) &&
                    request.get("date").equals(date.getTimeInMillis())).toList().size());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getIncomingRequestWithoutToken() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);
        try {
            HttpResponse<String> response = makeHttpRequest("user/incoming", HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertEquals("Token not valid or not present", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getIncomingRequestUserNotFound() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(connector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/incoming?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertEquals("Username not found", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getOutgoingRequests() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        when(connector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(connector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(connector.findByUsername(Alonso.getUsername())).thenReturn(Optional.of(Alonso));

        Calendar dateTime = Calendar.getInstance();
        FriendRequest friendRequest1 = new FriendRequest(Alonso, Unai, dateTime);
        FriendRequest friendRequest2 = new FriendRequest(Alonso, Alberto, dateTime);
        when(connector.findFriendRequestsByRequester(Alonso)).thenReturn(Set.of(friendRequest1, friendRequest2));

        FriendRequestService.init(connector);

        try {
            String token = UserTokenService.generateToken(Alonso.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/outgoing?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

            List<Map<String, Object>> list = new ObjectMapper().readValue(response.body(), List.class); // List of JSONs from response
            assertEquals(2, list.size()); // The 2 requests

            assertEquals(1, list.stream().filter(request -> request.get("requested").equals(Unai.getUsername()) &&
                    request.get("date").equals(dateTime.getTimeInMillis())).toList().size());
            assertEquals(1, list.stream().filter(request -> request.get("requested").equals(Alberto.getUsername()) &&
                    request.get("date").equals(dateTime.getTimeInMillis())).toList().size());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getOutgoingRequestWithoutToken() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);
        try {
            HttpResponse<String> response = makeHttpRequest("user/outgoing", HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertEquals("Token not valid or not present", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getOutgoingRequestUserNotFound() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findByUsername(Alonso.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(connector);
        try {
            String token = UserTokenService.generateToken(Alonso.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/outgoing?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertEquals("Username not found", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getFriendsFromUser() {
        MySqlConnector connector = mock(MySqlConnector.class);

        User friend1 = new User("friend1", "pass1", Calendar.getInstance());
        User friend2 = new User("friend2", "pass2", Calendar.getInstance());
        User friend3 = new User("friend3", "pass3", Calendar.getInstance());

        friend1.setFriends(Set.of(new LazyReference<>(friend2), new LazyReference<>(friend3)));

        when(connector.findByUsername(friend1.getUsername())).thenReturn(Optional.of(friend1));

        App.attachDatabaseManager(connector);

        try {
            String token = UserTokenService.generateToken(friend1.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/friends?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

            List<Map<String, Object>> list = new ObjectMapper().readValue(response.body(), List.class); // List of JSONs from response
            assertEquals(2, list.size()); // The 2 requests

            assertEquals(1, list.stream().filter(request -> request.get("username").equals(friend2.getUsername())).toList().size());
            assertEquals(1, list.stream().filter(request -> request.get("username").equals(friend3.getUsername())).toList().size());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getFriendsFromUserButEmptyList() {
        MySqlConnector connector = mock(MySqlConnector.class);

        User friend1 = new User("friend1", "pass1", Calendar.getInstance());

        when(connector.findByUsername(friend1.getUsername())).thenReturn(Optional.of(friend1));

        App.attachDatabaseManager(connector);

        try {
            String token = UserTokenService.generateToken(friend1.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/friends?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());

            List<Map<String, Object>> list = new ObjectMapper().readValue(response.body(), List.class); // List of JSONs from response
            assertEquals(0, list.size());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getFriendsFromUserWithoutToken() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);
        try {
            HttpResponse<String> response = makeHttpRequest("user/friends", HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertEquals("Token not valid or not present", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void getFriendsFromUserNotFound() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(connector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            HttpResponse<String> response = makeHttpRequest(String.format("user/friends?session_token=%s", token),
                    HttpMethod.GET, null);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertEquals("Username not found", response.body());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void deleteFriendship() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        User user1 = new User("user1", "pass1", Calendar.getInstance());
        User user2 = new User("user2", "pass1", Calendar.getInstance());
        user1.setFriends(Set.of(new LazyReference<>(user2)));

        when(connector.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(connector.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));

        try {
            String token = UserTokenService.generateToken(user1.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "friend", user2.getUsername()
            ));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertEquals("Friendship has been removed successfully", response.body());
            verify(connector).deleteFriend(any());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void deleteFriendshipWithoutToken() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "friend", "whatever"
            ));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.statusCode());
            assertEquals("Token not valid or not present", response.body());
            verify(connector, never()).deleteFriend(any());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void deleteFriendshipWithoutFriendUsername() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        User user1 = new User("user1", "pass1", Calendar.getInstance());

        when(connector.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));

        try {
            String token = UserTokenService.generateToken(user1.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token
            ));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertEquals("Missing attribute: friend", response.body());
            verify(connector, never()).deleteFriend(any());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void deleteFriendshipUsernamesNotFound() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        User user1 = new User("user1", "pass1", Calendar.getInstance());
        User user2 = new User("user2", "pass1", Calendar.getInstance());

        when(connector.findByUsername(user1.getUsername())).thenReturn(Optional.empty());
        when(connector.findByUsername(user2.getUsername())).thenReturn(Optional.empty());

        try {
            String token = UserTokenService.generateToken(user1.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "friend", user2.getUsername()
            ));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertEquals("Usernames not found", response.body());
            verify(connector, never()).deleteFriend(any());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }

    @Test
    public void deleteUnexistentFriendship() {
        MySqlConnector connector = mock(MySqlConnector.class);
        App.attachDatabaseManager(connector);

        User user1 = new User("user1", "pass1", Calendar.getInstance());
        User user2 = new User("user2", "pass1", Calendar.getInstance());

        when(connector.findByUsername(user1.getUsername())).thenReturn(Optional.of(user1));
        when(connector.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));

        try {
            String token = UserTokenService.generateToken(user1.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of(
                    "session_token", token,
                    "friend", user2.getUsername()
            ));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertEquals("Unexistent friendship", response.body());
            verify(connector, never()).deleteFriend(any());
        } catch (Exception e) {
            fail("Unexpected exception happened: " + e.getMessage());
        }
    }
}
