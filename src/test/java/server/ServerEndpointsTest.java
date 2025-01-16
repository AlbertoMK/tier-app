package server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerEndpointsTest {

    private String base_url = "http://localhost:8080";
    private static HttpServer server;

    /**
     *
     * @param requestURL the endpoint that goes after the base_url.
     * @return An object containing the response and interestant metadata.
     * @throws IOException
     * @throws InterruptedException
     */
    protected HttpResponse<String> makeHttpRequest(String requestURL) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(String.format("%s/%s", base_url, requestURL)))
                  .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    protected static void startServer() throws IOException {
        server = App.startServer();
    }

    protected static void stopServer() {
        server.stop(0);
    }
}
