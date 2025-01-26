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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private Calendar dateOfBirth;
    private List<Routine> routines;
    private Set<User> friends;


    public User (String username, String password, Calendar dateOfBirth) {
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        routines = new ArrayList<>();
        friends = new HashSet<>();
    }

    public boolean equals(Object object) {
        if(object instanceof User) {
            return ((User) object).username.equals(username);
        }
        return false;
    }

    public boolean addFriend(User friend) {
        if (friends.contains(friend))
            return false;
        friends.add(friend);
        return true;
    }

    public boolean removeFriend(User friend) {
        if (friends.contains(friend)) {
            friends.remove(friend);
            return true;
        }
        return false;
    }
}
