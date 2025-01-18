package server.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.Database.UserRepository;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.UserTokenService;
import server.Utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserController extends GenericHTTPHandler {

    private UserRepository userRepository;
    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /user
    protected void handleGetRequest(HttpExchange exchange) {
        Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
        Object[] res;

        //1. /user?username=[] -> Retrieves a single user by the username
        if (params.containsKey("username")) {
            res = getUserByUsernameEndpoint(params);
        }

        //2. /user -> Retrieves list of all users
        else {
            res = getAllUsersEndpoint();
        }

        String response = (String) res[0];
        int httpStatus = (int) res[1];
        Utils.httpResponse(exchange, httpStatus, response);
    }

    // POST /user
    protected void handlePostRequest(HttpExchange exchange) {
        Optional<String> nextSegment = getNextSegment(exchange.getRequestURI(), 1);
        Object[] res;

        //1. /user/login -> Validates credentials and generates token
        if(nextSegment.isPresent() && nextSegment.get().equals("login")) {
            res = loginEndpoint(exchange);
        }

        //2. /user -> Creates a new user
        else if (nextSegment.isEmpty()) {
            res = createUserEndpoint(exchange);
        }

        else {
            res = new Object[]{"Unrecognized endpoint", HttpURLConnection.HTTP_BAD_REQUEST};
        }

        String response = (String) res[0];
        int httpStatus = (int) res[1];
        Utils.httpResponse(exchange, httpStatus, response);
    }

    private Object[] loginEndpoint(HttpExchange exchange) {
        String response;
        int httpStatus;
        try {
            Map<String, String> body = extractJsonBody(exchange);
            if (!body.containsKey("username") || !body.containsKey("password")) { // request without username or password
                response = "The request must include an username and a password.";
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
            } else {
                String username = body.get("username");
                String rawPassword = body.get("password");
                if (userRepository.findByUsername(username).isPresent()) { // request with existing username
                    if (compareCredentials(username, rawPassword)) { // valid request with matching passwords
                        String token = UserTokenService.generateToken(username);
                        response = new ObjectMapper().writeValueAsString(Map.of("session-token", token));
                        httpStatus = HttpURLConnection.HTTP_OK;
                    } else { // wrong credentials
                        response = "Incorrect password for given username";
                        httpStatus = HttpURLConnection.HTTP_UNAUTHORIZED;
                    }
                } else { // request with not existing username
                    response = "Username not found";
                    httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
                }
            }
        } catch (IOException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        }
        return new Object[]{response, httpStatus};
    }

    private Object[] createUserEndpoint(HttpExchange exchange) throws IllegalArgumentException {
        String response;
        int httpStatus;
        try {
            Map<String, String> body = extractJsonBody(exchange);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true); // fails if there is a missing attribute.
            User user = objectMapper.convertValue(body, User.class);
            user.setPassword(hashPassword(user.getPassword()));
            if (createUser(user)) {
                response = "Successfully created a new user";
                httpStatus = HttpURLConnection.HTTP_OK;
            } else {
                response = "Username is already taken";
                httpStatus = HttpURLConnection.HTTP_CONFLICT;
            }
        } catch (IOException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        } catch (IllegalArgumentException argumentException) {
            response = "Invalid arguments for creating a new user";
            httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
        }
        return new Object[]{response, httpStatus};
    }

    private boolean createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) { // false if the username is already in use
            return false;
        } else {
            userRepository.createUser(user);
            return true;
        }
    }

    private Object[] getUserByUsernameEndpoint(Map<String, String> params) {
        String response;
        int httpStatus;
        Optional<User> result = userRepository.findByUsername(params.get("username"));
        if (!result.isEmpty()) {
            User user = result.get();
            try {
                response = new ObjectMapper().writeValueAsString(user);
                httpStatus = HttpURLConnection.HTTP_OK;
            } catch (JsonProcessingException ex) {
                response = "Internal error";
                httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
                LoggerService.logerror("Internal error while parsing user object to json format.");
            }
        } else {
            response = "User not found";
            httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
        }
        return new Object[]{response, httpStatus};
    }

    private Object[] getAllUsersEndpoint() {
        List<User> users = userRepository.findAll();
        String response;
        int httpStatus;
        try {
            response = new ObjectMapper().writeValueAsString(users);
            httpStatus = HttpURLConnection.HTTP_OK;
        } catch (JsonProcessingException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            LoggerService.logerror("Internal error while parsing list of users to json format.");
        }
        return new Object[]{response, httpStatus};
    }

    private boolean compareCredentials(String username, String rawPassword) {
        String expectedHashedPassword = userRepository.findByUsername(username).orElseThrow().getPassword();
        return encoder.matches(rawPassword, expectedHashedPassword);
    }

    /**
     * This method to hash a password with BCrypt
     *
     * @param rawPassword the password without being hashed (raw).
     * @return hashed password
     */
    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
