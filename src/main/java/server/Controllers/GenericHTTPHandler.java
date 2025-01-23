package server.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.Utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class GenericHTTPHandler implements HttpHandler {

    public void handle (HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET": handleGetRequest(exchange); break;
            case "POST": handlePostRequest(exchange); break;
            case "PUT": handlePutRequest(exchange); break;
            case "DELETE": handleDeleteRequest(exchange); break;
            default:
                String response = "Method not implemented";
                Utils.httpResponse(exchange, HttpURLConnection.HTTP_NOT_IMPLEMENTED, response);
        }
    }

    // by default, any mathod is allowed. Override these methods to implement the logic.
    protected void handleGetRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);
    }

    protected void handlePostRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);
    }

    protected void handlePutRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);
    }

    protected void handleDeleteRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);;
    }

    /**
     * Use this method to extract the parameters of a URL request
     * @param query The part of the URL that goes after '?'.
     * @return Map containing the keys and values of each parameter of the request.
     */
    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        return params;
    }

    /**
     * This method to extract as a Map of Strings the json body of a request
     * @param exchange representing the http request
     * @return Map of Strings representing the json body
     * @throws IOException
     */
    protected Map<String, String> extractJsonBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }

        reader.close();
        inputStream.close();

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(body.toString(), Map.class);
    }

    /**
     * Returns the next segment from a http path. eg: (user/manage/create/training, 1) -> "manage"
     * @param uri
     * @param index
     * @return
     */
    protected static Optional<String> getNextSegment(URI uri, int index) {
        String path = uri.getPath();
        String[] segments = path.split("/");

        if (index + 1 < segments.length) {
            return Optional.of(segments[index + 1]);
        }
        return Optional.empty();
    }
}
