package server.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import server.Database.UserRepository;
import server.Model.User;
import server.Utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserController extends GenericHTTPHandler {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /user
    protected void handleGetRequest(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
        Object[] res;

        //1. /user?username=[] -> Retrieves a single user by the username
        if (params.containsKey("username")) { res = getUserByUsernameEndpoint(params); }

        //2. /user -> Retrieves list of all users
        else { res = getAllUsersEndpoint(); }

        String response = (String) res[0];
        int httpStatus = (int) res[1];
        Utils.httpResponse(exchange, httpStatus, response);
    }

    private Object[] getUserByUsernameEndpoint(Map<String, String> params) throws JsonProcessingException {
        String response;
        int httpStatus;
        Optional<User> result = userRepository.findByUsername(params.get("username"));
        if (!result.isEmpty()) {
            User user = result.get();
            response = new ObjectMapper().writeValueAsString(user);
            httpStatus = HttpURLConnection.HTTP_OK;
        } else {
            response = "User not found";
            httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
        }
        return new Object[]{response, httpStatus};
    }

    private Object[] getAllUsersEndpoint() throws JsonProcessingException {
        List<User> users = userRepository.findAll();
        String response = new ObjectMapper().writeValueAsString(users);
        int httpStatus = HttpURLConnection.HTTP_OK;
        return new Object[]{response, httpStatus};
    }
}
