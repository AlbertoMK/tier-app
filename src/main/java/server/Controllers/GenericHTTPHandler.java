package server.Controllers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.Utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

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

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);
    }

    private void handlePutRequest(HttpExchange exchange) throws IOException {
        String response = "Method not allowed";
        Utils.httpResponse(exchange, HttpURLConnection.HTTP_BAD_METHOD, response);
    }

    private void handleDeleteRequest(HttpExchange exchange) throws IOException {
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
}
