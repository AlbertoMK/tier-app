package server.Utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    public static void httpResponse(HttpExchange httpExchange, int httpStatus, String response) throws IOException {
        httpExchange.sendResponseHeaders(httpStatus, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
