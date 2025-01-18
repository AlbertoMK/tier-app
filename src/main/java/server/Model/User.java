package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Calendar;
import java.util.List;

@AllArgsConstructor
public class User {

    @Getter
    private String username;

    @Getter
    private String password;

    @Getter
    private Calendar dateOfBirth;

    @Getter
    private List<Routine> routines;
}
