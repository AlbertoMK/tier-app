package server.Database;

import server.Model.FriendRequest;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MySqlConnector implements UserRepository {

    private Connection connection;
    private static final String USERS_TABLE_NAME = "users";
    private static final String FRIEND_REQUEST_TABLE_NAME = "friend_request";

    public void connectDatabase() throws SQLException {
        LoggerService.log("Starting connection with mysql database...");
        String url = PropertiesLoader.getProperty("database.sql.url");
        String user = PropertiesLoader.getProperty("database.sql.username");
        String password = PropertiesLoader.getProperty("database.sql.password");
        try {
            connection = DriverManager.getConnection(url, user, password);
            LoggerService.log("MySQL connection successful");
        } catch (SQLException e) {
            LoggerService.logerror("Error connecting to mysql database");
        }
    }

    public void closeDatabse() {
        if (connection != null) {
            try {
                connection.close();
                LoggerService.log("MySQL connection closed successfully");
            } catch (SQLException e) {
                LoggerService.logerror("Error closing connection");
            }
        }
    }

    public void truncateUserTables() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("TRUNCATE TABLE " + USERS_TABLE_NAME);
            statement = connection.createStatement();
            statement.executeUpdate("TRUNCATE TABLE " + FRIEND_REQUEST_TABLE_NAME);
        } catch (SQLException ex) {
            LoggerService.logerror("Error while truncating users tables");
        }
    }

    @Override
    public Optional<User> findByUsername(String username) { // not finished
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s WHERE username='%s'", USERS_TABLE_NAME, username));
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                Calendar dateOfBirth = Calendar.getInstance();
                dateOfBirth.setTime(rs.getDate("birth_date"));
                user.setDateOfBirth(dateOfBirth);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            LoggerService.logerror("Error while finding user by username");
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s", USERS_TABLE_NAME));
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                Calendar dateOfBirth = Calendar.getInstance();
                dateOfBirth.setTime(rs.getDate("birth_date"));
                user.setDateOfBirth(dateOfBirth);
                users.add(user);
            }
        } catch (SQLException e) {
            LoggerService.logerror("Error while finding all users");
        }
        return users;
    }

    public void addUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES(?,?,?)", USERS_TABLE_NAME));
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            Calendar dateOfBirth = user.getDateOfBirth();
            statement.setString(3, String.format("%d/%d/%d",
                    dateOfBirth.get(Calendar.YEAR), dateOfBirth.get(Calendar.MONTH) + 1, dateOfBirth.get(Calendar.DAY_OF_MONTH)));
            statement.executeUpdate();
        } catch (SQLException ex) {
            LoggerService.logerror("Error while inserting user into database");
        }
    }

    @Override
    public void addFriendship(User user1, User user2) {

    }

    @Override
    public void addFriendRequest(FriendRequest friendRequest) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES(?,?,?)", FRIEND_REQUEST_TABLE_NAME));
            statement.setString(1, friendRequest.getRequester().getUsername());
            statement.setString(2, friendRequest.getRequested().getUsername());
            Calendar date = friendRequest.getDate();
            statement.setTimestamp(3, new Timestamp(date.getTimeInMillis()));
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logerror("Error while adding friendship");
        }
    }

    @Override
    public void deleteFriendRequest(FriendRequest friendRequest) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("DELETE FROM %s WHERE requester='%s' AND requested='%s'",
                      FRIEND_REQUEST_TABLE_NAME, friendRequest.getRequester().getUsername(), friendRequest.getRequested().getUsername()));
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logerror("Error while deleting friend request");
        }
    }

    @Override
    public Set<FriendRequest> findFriendRequestsByRequester(User requester) {
        Set<FriendRequest> friendRequestSet = new HashSet<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s WHERE requester='%s'", FRIEND_REQUEST_TABLE_NAME, requester.getUsername()));
            while (rs.next()) {
                FriendRequest friendRequest = new FriendRequest();
                Optional<User> requestedUser = findByUsername(rs.getString("requested"));
                friendRequest.setRequester(requester);
                friendRequest.setRequested(requestedUser.get());
                Calendar date = Calendar.getInstance();
                date.setTime(rs.getTimestamp("date"));
                friendRequest.setDate(date);
                friendRequestSet.add(friendRequest);
            }
        } catch (Exception e) {
            LoggerService.logerror("Error finding friend request");
            System.err.println(e.getMessage());
        }
        return friendRequestSet;
    }

    @Override
    public Set<FriendRequest> findFriendRequestsByRequested(User requested) {
        Set<FriendRequest> friendRequestSet = new HashSet<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s WHERE requested='%s'", FRIEND_REQUEST_TABLE_NAME, requested.getUsername()));
            while (rs.next()) {
                FriendRequest friendRequest = new FriendRequest();
                Optional<User> requesterUser = findByUsername(rs.getString("requester"));
                friendRequest.setRequester(requesterUser.get());
                friendRequest.setRequested(requested);
                Calendar date = Calendar.getInstance();
                date.setTime(rs.getTimestamp("date"));
                friendRequest.setDate(date);
                friendRequestSet.add(friendRequest);
            }
        } catch (Exception e) {
            LoggerService.logerror("Error finding friend request");
        }
        return friendRequestSet;
    }
}
