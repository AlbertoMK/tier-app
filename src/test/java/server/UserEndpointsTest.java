package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Controllers.UserController;
import server.Database.MySqlConnector;
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
            assertTrue(response.body().contains("This user has already sent a friend request"));
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
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));
        when(mySqlConnector.findFriendRequestsByRequester(Alberto)).thenReturn(Set.of());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
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
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.of(Unai));

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
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
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);
        App.attachDatabaseManager(mySqlConnector);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        when(mySqlConnector.findByUsername(Unai.getUsername())).thenReturn(Optional.empty());

        FriendRequestService.init(mySqlConnector);
        try {
            String token = UserTokenService.generateToken(Alberto.getUsername());
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("session_token", token, "requested", Unai.getUsername()));
            HttpResponse<String> response = makeHttpRequest("user/friend", HttpMethod.DELETE, requestBody);
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
            assertTrue(response.body().contains("Usernames not found"));
            verify(mySqlConnector, never()).deleteFriendRequest(any());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
}
