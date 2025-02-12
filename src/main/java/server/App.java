package server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import server.Controllers.UserController;
import server.Database.MySqlConnector;
import server.Database.UserRepository;
import server.Utils.FriendRequestService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

import static server.Utils.LoggerService.log;

public class App {

    private static HttpServer server;
    private static HttpContext contextInUse;

    public static void main( String[] args ) throws IOException, SQLException {
        startServer();
        MySqlConnector userConnector = new MySqlConnector();
        FriendRequestService.init(userConnector);
        userConnector.connectDatabase();
        attachDatabaseManager(userConnector);
    }

    // Use to make unit test mocking connector
    protected static HttpServer startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(null);
        log("Server started at port 8080...");
        server.start();
        return server;
    }

    protected static void attachDatabaseManager(UserRepository userConnector) {
        if(contextInUse != null) {
            server.removeContext(contextInUse);
        }
        contextInUse = server.createContext("/user", new UserController(userConnector));
    }
}
