package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import server.Controllers.UserController;
import server.Database.MySqlConnector;
import server.Model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserEndpointsTest extends ServerEndpointsTest {

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

        User user1 = new User("Alberto", "p1");
        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user?username=Alberto", HttpMethod.GET, "").body();
            assertEquals(new ObjectMapper().writeValueAsString(user1), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindByUsernameNotFoundEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            HttpResponse<String> response = makeHttpRequest("user?username=Alberto", HttpMethod.GET, "");
            assertEquals("User not found", response.body());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindAllEndpointTest() {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        User user1 = new User("Alberto", "p1");
        User user2 = new User("Unai", "p1");
        User user3 = new User("Nico", "p1");
        User user4 = new User("Alonso", "p1");

        when(mySqlConnector.findAll()).thenReturn(List.of(user1, user2, user3, user4));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user", HttpMethod.GET, "").body();
            assertEquals(new ObjectMapper().writeValueAsString(List.of(user1, user2, user3, user4)), response);
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

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            User user1 = new User("Alberto", "p1");
            String requestBody = new ObjectMapper().writeValueAsString(user1);
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

        User user1 = new User("Alberto", "p1");
        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(user1);
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

        User user1 = new User("alberto", UserController.hashPassword("p1"));
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

        User user1 = new User("alberto", "actual password");
        when(mySqlConnector.findByUsername("alberto")).thenReturn(Optional.of(user1));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String requestBody = new ObjectMapper().writeValueAsString(Map.of("username", "alberto", "password", "p1"));
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
