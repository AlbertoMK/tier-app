package server.Database;

import server.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySqlConnector implements UserRepository{
    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>();
    }
}
