package server.Database;

import server.Model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    public Optional<User> findByUsername(String username);
    public List<User> findAll();
}
