package server.Database;

import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlConnector implements UserRepository {

    private Connection connection;
    public void connectDatabase() throws SQLException {
        LoggerService.log("Starting connection with database...");
        String url = PropertiesLoader.getProperty("database.url");
        String user = PropertiesLoader.getProperty("database.username");
        String password = PropertiesLoader.getProperty("database.password");
        try {
            connection = DriverManager.getConnection(url, user, password);
            LoggerService.log("Connection successful");
        } catch (SQLException e) {
            LoggerService.logerror("Error connecting to database");
        }
    }

    public void closeDatabse() {
        if (connection != null) {
            try {
                connection.close();
                LoggerService.log("Connection closed successfully");
            } catch (SQLException e) {
                LoggerService.logerror("Error closing connection");
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>();
    }

    @Override
    public void createUser(User user) {

    }
}
