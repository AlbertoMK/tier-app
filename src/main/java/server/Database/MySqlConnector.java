package server.Database;

import server.Model.Exercise;
import server.Model.FriendRequest;
import server.Model.GymExercise;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class MySqlConnector implements UserRepository, ExerciseRepository {

    private Connection connection;
    private static final String USERS_TABLE_NAME = "users";
    private static final String FRIEND_REQUEST_TABLE_NAME = "friend_request";
    private static final String EXERCISES_TABLE_NAME = "exercises";
    private static final String FRIENDS_TABLE_NAME = "friends";

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

    // USERS

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
    public List<User> findAllUsers() {
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
            PreparedStatement statement = connection.prepareStatement(String.format("DELETE FROM %s WHERE requester=? AND requested=?", FRIEND_REQUEST_TABLE_NAME));
            statement.setString(1, friendRequest.getRequester().getUsername());
            statement.setString(2, friendRequest.getRequested().getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logerror("Error while deleting friend request");
        }
    }

    @Override
    public void addFriend(FriendRequest friendRequest) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES(?,?)",
                    FRIENDS_TABLE_NAME, friendRequest.getRequester().getUsername(), friendRequest.getRequested().getUsername()));
            statement.setString(1, friendRequest.getRequester().getUsername());
            statement.setString(2, friendRequest.getRequested().getUsername());
            statement.executeUpdate();
        } catch (SQLException e) {
            LoggerService.logerror("Error while accepting friend request");
        }
    }

    @Override
    public void deleteFriend(FriendRequest friendRequest) {
        try {
            PreparedStatement statement = connection.prepareStatement(String.format("DELETE FROM %s WHERE username1=? AND username2=?", FRIENDS_TABLE_NAME));
            statement.setString(1, friendRequest.getRequester().getUsername());
            statement.setString(2, friendRequest.getRequested().getUsername());
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

    @Override
    public FriendRequest findFriendRequestsByBothUsers(User requester, User requested) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s WHERE requester='%s' AND requested='%s'",
                    FRIEND_REQUEST_TABLE_NAME, requester.getUsername(), requested.getUsername()));
            while (rs.next()) {
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setRequester(requester);
                friendRequest.setRequested(requested);
                Calendar date = Calendar.getInstance();
                date.setTime(rs.getTimestamp("date"));
                friendRequest.setDate(date);
                return friendRequest;
            }
        } catch (Exception e) {
            LoggerService.logerror("Error finding friend request");
        }
        return null;
    }

    public User findFriendFromFriend(User friend) {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT * FROM %s WHERE username1='%s'",
                    FRIENDS_TABLE_NAME, friend.getUsername()));
            if (rs.next()) {
                Optional<User> friendRequested = findByUsername(rs.getString("username2"));
                return friendRequested.get();
            }
        } catch (Exception e) {
            LoggerService.logerror("Error finding friend request");
        }
        return null;
    }

    public Set<User> findFriendsGroupFromFriend(User friend) {
        Set<User> friends = new HashSet<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(String.format("SELECT DISTINCT * FROM %s WHERE username1='%s'",
                    FRIENDS_TABLE_NAME, friend.getUsername()));
            while (rs.next()) {
                Optional<User> friendRequested = findByUsername(rs.getString("username2"));
                friends.add(friendRequested.get());
            }
        } catch (Exception e) {
            LoggerService.logerror("Error finding friend request");
        }
        return friends;
    }

    // EXERCISES

    @Override
    public Optional<Exercise> findExerciseByName(String exerciseName) {
        try {
            return Optional.of(findExerciseWithFilters(Map.of("exercise_name", exerciseName)).iterator().next());
        } catch (NoSuchElementException ex) {
            return Optional.empty();
        }
    }

    @Override
    public Set<Exercise> findAllExercises() {
        return findExerciseWithFilters(Map.of());
    }

    @Override
    public Set<Exercise> findExerciseWithFilters(Map<String, String> filters) {
        try {
            Set<Exercise> set = new HashSet<>();
            String query = String.format("SELECT * FROM %s WHERE 1=1", EXERCISES_TABLE_NAME);
            List<String> filterList = new ArrayList<>();
            for (String filter : filters.keySet()) {
                query = query + String.format(" AND %s = ?", filter);
                filterList.add(filters.get(filter));
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            for (int i = 0; i < filterList.size(); i++) {
                preparedStatement.setString(i + 1, filterList.get(i));
            }
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String exerciseName = rs.getString("exercise_name");
                Exercise.SetsType setsType = Exercise.SetsType.valueOf(rs.getString("sets_type").toUpperCase());
                GymExercise.DifficultyLevel difficultyLevel = GymExercise.DifficultyLevel.valueOf(rs.getString("difficulty").toUpperCase().replace(" ", "_"));
                GymExercise.MuscleGroup muscleGroup = GymExercise.MuscleGroup.valueOf(rs.getString("muscle_group").toUpperCase().replace(" ", "_"));
                GymExercise.Equipment equipment = GymExercise.Equipment.valueOf(rs.getString("equipment").toUpperCase().replace(" ", "_"));
                GymExercise.SingleArm singleArm = GymExercise.SingleArm.valueOf(rs.getString("single_double_arm").toUpperCase().replace(" ", "_"));
                GymExercise.Grip grip = GymExercise.Grip.valueOf(rs.getString("grip").toUpperCase().replace(" ", "_"));
                GymExercise.BodyRegion bodyRegion = GymExercise.BodyRegion.valueOf(rs.getString("body_region").toUpperCase()
                        .replace(" ", "_").replace("*", ""));
                GymExercise exercise = new GymExercise(exerciseName, setsType, difficultyLevel, muscleGroup, equipment, singleArm, grip, bodyRegion);
                set.add(exercise);
            }
            return set;
        } catch (SQLException ex) {
            LoggerService.logerror("Error while retrieving exercises with filters");
            return Set.of();
        }
    }
}
