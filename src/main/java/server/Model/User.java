package server.Model;

import lombok.*;

import java.util.Calendar;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class User {

    @NonNull
    private String username;
    @NonNull
    private String password;
    @NonNull
    private Calendar dateOfBirth;
    private List<Routine> routines;
}
