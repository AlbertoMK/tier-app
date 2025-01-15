package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import server.Database.MySqlConnector;
import server.Model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppTest extends ServerEndpointsTest {

    @Test
    public void userFindByUsernameEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        User user1 = new User("Alberto");

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.of(user1));

        HttpServer server = App.startServer(mySqlConnector);

        try {
            String response = makeHttpRequest(String.format("%s/user?username=Alberto", base_url)).body();
            assertEquals(new ObjectMapper().writeValueAsString(user1), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void userFindByUsernameNotFoundEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.empty());

        HttpServer server = App.startServer(mySqlConnector);

        try {
            HttpResponse<String> response = makeHttpRequest(String.format("%s/user?username=Alberto", base_url));
            assertEquals("User not found", response.body());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void userFindAllEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        User user1 = new User("Alberto");
        User user2 = new User("Unai");
        User user3 = new User("Nico");
        User user4 = new User("Alonso");

        when(mySqlConnector.findAll()).thenReturn(List.of(user1, user2, user3, user4));

        HttpServer server = App.startServer(mySqlConnector);

        try {
            String response = makeHttpRequest(String.format("%s/user", base_url)).body();
            assertEquals(new ObjectMapper().writeValueAsString(List.of(user1, user2, user3, user4)), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void userFindAllEmptyEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findAll()).thenReturn(new ArrayList<>());

        HttpServer server = App.startServer(mySqlConnector);

        try {
            String response = makeHttpRequest(String.format("%s/user", base_url)).body();
            assertEquals(new ObjectMapper().writeValueAsString(new ArrayList<>()), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        } finally {
            server.stop(0);
        }
    }
}
