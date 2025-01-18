package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.Database.MySqlConnector;
import server.Model.Routine;
import server.Model.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppTest extends ServerEndpointsTest {

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
    public void userFindByUsernameEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.of(Alberto));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user?username=Alberto").body();
            assertEquals(new ObjectMapper().writeValueAsString(Alberto), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindByUsernameNotFoundEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findByUsername("Alberto")).thenReturn(Optional.empty());
        App.attachDatabaseManager(mySqlConnector);

        try {
            HttpResponse<String> response = makeHttpRequest("user?username=Alberto");
            assertEquals("User not found", response.body());
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindAllEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findAll()).thenReturn(List.of(Alberto, Alonso, Nico, Unai));
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user").body();
            assertEquals(new ObjectMapper().writeValueAsString(List.of(Alberto, Alonso, Nico, Unai)), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }

    @Test
    public void userFindAllEmptyEndpointTest() throws IOException {
        MySqlConnector mySqlConnector = mock(MySqlConnector.class);

        when(mySqlConnector.findAll()).thenReturn(new ArrayList<>());
        App.attachDatabaseManager(mySqlConnector);

        try {
            String response = makeHttpRequest("user").body();
            assertEquals(new ObjectMapper().writeValueAsString(new ArrayList<>()), response);
        } catch (IOException | InterruptedException ex) {
            fail("Unexpected exception happen: " + ex.getMessage());
        }
    }
}
