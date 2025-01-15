package server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerEndpointsTest {

    protected String base_url = "http://localhost:8080";

    protected HttpResponse<String> makeHttpRequest(String requestURL) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                  .uri(URI.create(requestURL))
                  .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
