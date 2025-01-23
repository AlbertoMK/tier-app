package server.Utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class Utils {

    public static void httpResponse(HttpExchange httpExchange, int httpStatus, boolean isJson, String response) {
        try {
            if (isJson)
                httpExchange.getResponseHeaders().set("Content-Type", "application/json");
            else
                httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
            httpExchange.sendResponseHeaders(httpStatus, response.getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException ex) {
            try {
                httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
                String errorResponse = "Internal error retrieving infomation";
                LoggerService.logerror("Error while retrieving httpResponse... " + ex.getMessage());
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, errorResponse.getBytes().length);
                OutputStream os = httpExchange.getResponseBody();
                os.write(errorResponse.getBytes());
                os.close();
            } catch (IOException ex2) {
                LoggerService.logerror("Fatal error retrieving error message after error retrieving httpResponse");
            }
        }
    }
}
