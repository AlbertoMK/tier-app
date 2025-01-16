package server;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import server.Controllers.UserController;
import server.Database.MySqlConnector;

import java.io.IOException;
import java.net.InetSocketAddress;

import static server.Utils.LoggerService.log;

public class App {

    private static HttpServer server;
    private static HttpContext contextInUse;

    public static void main( String[] args ) throws IOException {
        startServer();
        attachDatabaseManager(new MySqlConnector());
    }

    // Use to make unit test mocking connector
    protected static HttpServer startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(null);
        log("Server started at port 8080...");
        server.start();
        return server;
    }

    protected static void attachDatabaseManager(MySqlConnector connector) {
        if(contextInUse != null) {
            server.removeContext(contextInUse);
        }
        contextInUse = server.createContext("/user", new UserController(connector));
    }
}
