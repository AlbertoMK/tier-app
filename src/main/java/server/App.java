package server;

import com.sun.net.httpserver.HttpServer;
import server.Controllers.UserController;
import server.Database.MySqlConnector;

import java.io.IOException;
import java.net.InetSocketAddress;

import static server.Utils.LoggerService.log;

public class App {

    public static void main( String[] args ) throws IOException {
        startServer(new MySqlConnector());
    }

    // Use to make unit test mocking connector
    protected static HttpServer startServer(MySqlConnector connector) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/user", new UserController(connector));
        server.setExecutor(null);
        log("Server started at port 8080...");
        server.start();
        return server;
    }
}
