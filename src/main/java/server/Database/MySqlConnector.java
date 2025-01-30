package server.Database;

import server.Model.Exercise;
import server.Model.FriendRequest;
import server.Model.GymExercise;
import server.Model.User;
import server.Utils.LoggerService;
import server.Utils.PropertiesLoader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MySqlConnector implements UserRepository, ExerciseRepository {

    private Connection connection;
    private static final String USERS_TABLE_NAME = "users";
    private static final String EXERCISES_TABLE_NAME = "exercises";

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

    // EXERCISES

    @Override
    public Optional<Exercise> findExerciseByName(String exerciseName) {
        return Optional.of(findExerciseWithFilters(Map.of("exercise_name", exerciseName)).iterator().next());
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
                GymExercise.BodyRegion bodyRegion = GymExercise.BodyRegion.valueOf(rs.getString("body_region").toUpperCase().replace(" ", "_"));
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
