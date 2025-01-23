package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import server.Controllers.UserController;
import server.Database.MySqlConnector;
import server.Model.Routine;
import server.Model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    static User Alberto = new User("Alberto", "password", albertoBirthday, new ArrayList<Routine>());
    static User Alonso = new User("Alonso", "password", alonsoBirthday, new ArrayList<Routine>());
    static User Nico = new User("Nico", "password", nicoBirthday, new ArrayList<Routine>());
    static User Unai = new User("Unai", "password", unaiBirthday, new ArrayList<Routine>());

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

        when(mySqlConnector.findAll()).thenReturn(List.of(Alberto, Alonso, Nico, Unai));
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

        when(mySqlConnector.findAll()).thenReturn(new ArrayList<>());
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
        MySqlConnector mySqlConnector = spy(MySqlConnector.class);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Alberto);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector).createUser(any());
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertEquals("Successfully created a new user", response.body());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void createRepeatedUsernameUserEndpointTest() {
        MySqlConnector mySqlConnector = spy(MySqlConnector.class);

        when(mySqlConnector.findByUsername(Alberto.getUsername())).thenReturn(Optional.of(Alberto));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Alberto);
            HttpResponse<String> response = makeHttpRequest("user", HttpMethod.POST, requestBody);

            verify(mySqlConnector, never()).createUser(any());
            assertEquals(HttpURLConnection.HTTP_CONFLICT, response.statusCode());
            assertEquals("Username is already taken", response.body());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void validLoginEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        User user1 = new User("Alberto", UserController.hashPassword("p1"), Calendar.getInstance(), new ArrayList<>());
        when(mySqlConnector.findByUsername("alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "p1"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
            assertTrue(response.body().contains("session-token"));
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
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "p1"));
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
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("user", "alberto", "pass", "p1"));
            HttpResponse<String> response = makeHttpRequest("user/login", HttpMethod.POST, requestBody);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.statusCode());
            assertTrue(response.body().contains("The request must include an username and a password."));
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
}
