package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server.Controllers.UserController;
import server.Database.MySqlConnector;
import server.Database.UserRepository;
import server.Model.FriendRequest;
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
import static org.mockito.Mockito.*;

public class UserEndpointsTest extends ServerEndpointsTest {

    private UserController userController;
    private UserRepository userRepository;
    private MySqlConnector mySqlConnector;
    private FriendRequestService friendRequestService;
    private HttpExchange exchange;

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
    public void setUp() {
        userRepository = mock(UserRepository.class);
        friendRequestService = mock(FriendRequestService.class);
        mySqlConnector = mock(MySqlConnector.class);
        userController = new UserController(userRepository);
        exchange = mock(HttpExchange.class);
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
    public void userFindByUsernameEndpointTest() {
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
        User user1 = new User("alberto", UserController.hashPassword("password"), Calendar.getInstance());
        when(mySqlConnector.findByUsername("alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "password"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("session-token"));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void wrongPasswordLoginEndpointTest() {
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
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
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
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest friendRequest = new FriendRequest(Unai, Alberto, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Unai)).thenReturn(Set.of(friendRequest));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requester", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend/accept", HttpMethod.POST, requestBody);

            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request accepted"));
            verify(mySqlConnector).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }


    @Test
    public void addRepeatedFriendRequest() {
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of(new FriendRequest(Alberto, Unai, Calendar.getInstance())));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("This user has already sent a friend request"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenFriendRequestTest() {
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
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token));
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
        App.attachDatabaseManager(mySqlConnector);
        
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).addFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requestedNotFoundFriendRequestTest() {
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));

        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
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
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        FriendRequest request = new FriendRequest(Alberto, Unai, Calendar.getInstance());
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of(request));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("Friend request removed"));
            verify(mySqlConnector).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void removeUnexistentFriendRequest() {
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertTrue(response.body().contains("This user hasn't an existing friend request"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void missingTokenRemoveFriendRequest() {
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
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("Missing attribute: requested"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requesterNotFoundRemoveFriendRequest() {
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void requestedNotFoundRemoveFriendRequest() {
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.empty());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session-token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void testDeclineFriendRequest() throws IOException {

    }
}
