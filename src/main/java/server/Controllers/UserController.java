package server.Controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.Database.UserRepository;
import server.Model.FriendRequest;
import server.Model.User;
import server.Utils.FriendRequestService;
import server.Utils.LoggerService;
import server.Utils.UserTokenService;
import server.Utils.Utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserController extends GenericHTTPHandler {

    private UserRepository userRepository;
    private static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final int MIN_USERNAME_LENGHT = 5;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final int MIN_PASSWORD_LENGHT = 8;
    private static final String BODY_TOKEN_KEY = "session-token";

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
        boolean isJson = (boolean) res[2];
        Utils.httpResponse(exchange, httpStatus, isJson, response);
    }

    // POST /user
    protected void handlePostRequest(HttpExchange exchange) {
        Optional<String> nextSegment = getNextSegment(exchange.getRequestURI(), 1);
        Object[] res;

        // /user -> Creates a new user
        if (nextSegment.isEmpty()) {
            res = createUserEndpoint(exchange);
        }

        // /user/login -> Validates credentials and generates token
        else if (nextSegment.isPresent() && nextSegment.get().equals("login")) {
            res = loginEndpoint(exchange);
        }

        // /user/friend -> Creates a friend request to requestedUsername
        else if (nextSegment.isPresent() && nextSegment.get().equals("friend")) {
            res = createFriendRequest(exchange);
        }

        else {
            res = new Object[]{"Unrecognized endpoint", HttpURLConnection.HTTP_BAD_REQUEST};
        }

        String response = (String) res[0];
        int httpStatus = (int) res[1];
        boolean isJson = (boolean) res[2];
        Utils.httpResponse(exchange, httpStatus, isJson, response);
    }

    protected void handleDeleteRequest(HttpExchange exchange) {
        Optional<String> nextSegment = getNextSegment(exchange.getRequestURI(), 1);
        Object[] res;

        // /user/friend -> Deletes an existent friend request to requestedUsername
        if (nextSegment.isPresent() && nextSegment.get().equals("friend")) {
            res = deleteFriendRequest(exchange);
        }

        else {
            res = new Object[]{"Unrecognized endpoint", HttpURLConnection.HTTP_BAD_REQUEST};
        }

        String response = (String) res[0];
        int httpStatus = (int) res[1];
        boolean isJson = (boolean) res[2];
        Utils.httpResponse(exchange, httpStatus, isJson, response);
    }

    private Object[] deleteFriendRequest(HttpExchange exchange) {
        String response;
        int httpStatus;
        boolean isJson;

        try {
            Map<String, String> body = extractJsonBody(exchange);
            Optional<String> requesterOptional = requiresToken(body);
            if (requesterOptional.isEmpty()) {
                response = "Token not valid or not present";
                httpStatus = HttpURLConnection.HTTP_UNAUTHORIZED;
                isJson = false;
            } else {
                String requester = requesterOptional.get();
                String requested = body.get("requested");
                if (requested == null) {
                    response = "Missing attribute: requested";
                    httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                    isJson = false;
                }
                else if (userRepository.findByUsername(requester).isPresent() && userRepository.findByUsername(requested).isPresent()) {
                    if (FriendRequestService.getInstance().removeRequest(requester, requested)) {
                        response = "Friend request removed";
                        httpStatus = HttpURLConnection.HTTP_OK;
                        isJson = false;
                    } else {
                            response = "This user hasn't an existing friend request";
                        httpStatus = HttpURLConnection.HTTP_CONFLICT;
                        isJson = false;
                    }
                } else {
                    response = "Usernames not found";
                    httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
                    isJson = false;
                }
            }
        } catch (IOException e) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            isJson = false;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        }
        return new Object[]{response, httpStatus, isJson};
    }

    private Object[] createFriendRequest(HttpExchange exchange) {
        String response;
        int httpStatus;
        boolean isJson;
        try {
            Map<String, String> body = extractJsonBody(exchange);
            Optional<String> requesterOptional = requiresToken(body);
            if (requesterOptional.isEmpty()) {
                response = "Token not valid or not present";
                httpStatus = HttpURLConnection.HTTP_UNAUTHORIZED;
                isJson = false;
            } else {
                String requester = requesterOptional.get();
                String requested = body.get("requested");
                if (requested == null) {
                    response = "Missing attribute: requested";
                    httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                    isJson = false;
                }
                else if (userRepository.findByUsername(requester).isPresent() && userRepository.findByUsername(requested).isPresent()) {
                    User requesterUser = userRepository.findByUsername(requester).get();
                    User requestedUser = userRepository.findByUsername(requested).get();
                    if (FriendRequestService.getInstance().addRequest(new FriendRequest(requesterUser, requestedUser, Calendar.getInstance()))) {
                        response = "Friend request sent";
                        httpStatus = HttpURLConnection.HTTP_OK;
                        isJson = false;
                    } else {
                        response = "This user has already sent a friend request";
                        httpStatus = HttpURLConnection.HTTP_CONFLICT;
                        isJson = false;
                    }
                } else {
                    response = "Usernames not found";
                    httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
                    isJson = false;
                }
            }
        } catch (IOException e) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            isJson = false;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        }
        return new Object[]{response, httpStatus, isJson};
    }

    private Object[] loginEndpoint(HttpExchange exchange) {
        String response;
        int httpStatus;
        boolean isJson;
        try {
            Map<String, String> body = extractJsonBody(exchange);
            if (!body.containsKey("username") || !body.containsKey("password")) { // request without username or password
                response = "The request must include an username and a password.";
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                isJson = false;
            } else {
                String username = body.get("username");
                String rawPassword = body.get("password");
                if (userRepository.findByUsername(username).isPresent()) { // request with existing username
                    if (compareCredentials(username, rawPassword)) { // valid request with matching passwords
                        String token = UserTokenService.generateToken(username);
                        response = new ObjectMapper().writeValueAsString(Map.of("session-token", token));
                        httpStatus = HttpURLConnection.HTTP_OK;
                        isJson = true;
                    } else { // wrong credentials
                        response = "Incorrect password for given username";
                        httpStatus = HttpURLConnection.HTTP_UNAUTHORIZED;
                        isJson = false;
                    }
                } else { // request with not existing username
                    response = "Username not found";
                    httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
                    isJson = false;
                }
            }
        } catch (IOException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            isJson = false;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        }
        return new Object[]{response, httpStatus, isJson};
    }

    private Object[] createUserEndpoint(HttpExchange exchange) throws IllegalArgumentException {
        String response;
        int httpStatus;
        boolean isJson;
        try {
            Map<String, String> body = extractJsonBody(exchange);
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.convertValue(body, User.class);
            if (user.getUsername().length() < MIN_USERNAME_LENGHT) {
                response = String.format("Username too short. Min %d characters", MIN_USERNAME_LENGHT);
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                isJson = false;
            } else if (user.getUsername().length() > MAX_USERNAME_LENGTH) {
                response = String.format("Username too long. Max %d characters", MAX_USERNAME_LENGTH);
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                isJson = false;
            } else if (user.getPassword().length() < MIN_PASSWORD_LENGHT) {
                response = String.format("Password too short. Min %d characters", MIN_PASSWORD_LENGHT);
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                isJson = false;
            } else if (user.getDateOfBirth() == null) {
                response = "Users need to indicate its date of birth.";
                httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
                isJson = false;
            } else {
                user.setPassword(hashPassword(user.getPassword()));
                if (createUser(user)) {
                    response = "Successfully created a new user";
                    httpStatus = HttpURLConnection.HTTP_OK;
                    isJson = false;
                } else {
                    response = "Username is already taken";
                    httpStatus = HttpURLConnection.HTTP_CONFLICT;
                    isJson = false;
                }
            }
        } catch (IOException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            isJson = false;
            LoggerService.logerror("Internal error while obtaining http body from request.");
        } catch (IllegalArgumentException argumentException) {
            response = "Invalid arguments for creating a new user";
            httpStatus = HttpURLConnection.HTTP_BAD_REQUEST;
            isJson = false;
        }
        return new Object[]{response, httpStatus, isJson};
    }

    private boolean createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) { // false if the username is already in use
            return false;
        } else {
            userRepository.addUser(user);
            return true;
        }
    }

    private Object[] getUserByUsernameEndpoint(Map<String, String> params) {
        String response;
        int httpStatus;
        boolean isJson;
        Optional<User> result = userRepository.findByUsername(params.get("username"));
        if (!result.isEmpty()) {
            User user = result.get();
            try {
                response = new ObjectMapper().writeValueAsString(user);
                httpStatus = HttpURLConnection.HTTP_OK;
                isJson = true;
            } catch (JsonProcessingException ex) {
                response = "Internal error";
                httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
                isJson = false;
                LoggerService.logerror("Internal error while parsing user object to json format.");
            }
        } else {
            response = "User not found";
            httpStatus = HttpURLConnection.HTTP_NOT_FOUND;
            isJson = false;
        }
        return new Object[]{response, httpStatus, isJson};
    }

    private Object[] getAllUsersEndpoint() {
        List<User> users = userRepository.findAll();
        String response;
        int httpStatus;
        boolean isJson;
        try {
            response = new ObjectMapper().writeValueAsString(users);
            httpStatus = HttpURLConnection.HTTP_OK;
            isJson = true;
        } catch (JsonProcessingException ex) {
            response = "Internal error";
            httpStatus = HttpURLConnection.HTTP_INTERNAL_ERROR;
            isJson = false;
            LoggerService.logerror("Internal error while parsing list of users to json format.");
        }
        return new Object[]{response, httpStatus, isJson};
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

    private Optional<String> requiresToken(Map<String, String> body) {
        if (body.containsKey(BODY_TOKEN_KEY)) {
            try {
                return Optional.of(UserTokenService.verifyToken(body.get(BODY_TOKEN_KEY)));
            } catch (JWTVerificationException e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
