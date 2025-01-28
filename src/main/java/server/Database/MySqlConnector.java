package server.Database;

import server.Model.FriendRequest;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MySqlConnector implements UserRepository {

    private Connection connection;
    private static final String USERS_TABLE_NAME = "users";

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
        return new ArrayList<>();
    }

    public void addUser(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES(?,?,?)", USERS_TABLE_NAME));
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, String.format("%d/%d/%d",
                      user.getDateOfBirth().get(Calendar.YEAR), user.getDateOfBirth().get(Calendar.MONTH) + 1, user.getDateOfBirth().get(Calendar.DAY_OF_MONTH)));
            statement.executeUpdate();
        } catch (SQLException ex) {
            LoggerService.logerror("Error while inserting user into database");
        }
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public void addFriendship(User user1, User user2) {

    }

    @Override
    public void addFriendRequest(User requester, User requested) {

    }

    @Override
    public void deleteFriendRequest(User requester, User requested) {

    }

    @Override
    public Set<FriendRequest> findFriendRequestsByRequester(User requester) {
        return null;
    }

    @Override
    public Set<FriendRequest> findFriendRequestsByRequested(User requested) {
        return null;
    }
}
