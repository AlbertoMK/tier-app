package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Calendar;
import java.util.List;

@Getter
@AllArgsConstructor
public class User {

    private String username;

    private String password;

    private Calendar dateOfBirth;

    private List<Routine> routines;
}
