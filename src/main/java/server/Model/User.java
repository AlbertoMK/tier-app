package server.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;
    private String password;
    private Calendar dateOfBirth;
    private List<Routine> routines;
}
