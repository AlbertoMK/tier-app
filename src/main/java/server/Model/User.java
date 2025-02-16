package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Setter
@NoArgsConstructor
public class User {

    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private Calendar dateOfBirth;
    private Set<LazyReference<User>> friends;


    public User (String username, String password, Calendar dateOfBirth) {
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        friends = new HashSet<>();
    }

    /**
     *
     * @param username
     * @param password
     * @param dateOfBirth
     * @param friendsMethods Set containing method to access friends
     */
    public User(String username, String password, Calendar dateOfBirth, Set<Supplier<User>> friendsMethods) {
        this(username, password, dateOfBirth);
        this.friends = friendsMethods.stream()
                .map(LazyReference::new)
                .collect(Collectors.toSet());
    }

    public boolean equals(Object object) {
        if(object instanceof User) {
            return ((User) object).username.equals(username);
        }
        return false;
    }

    public Set<User> getFriends() {
        return friends.stream().map(userLazyReference -> {
            return userLazyReference.get();
        }).collect(Collectors.toSet());
    }

    public void addFriend(User user) {
        friends.add(new LazyReference<>(user));
    }
}
