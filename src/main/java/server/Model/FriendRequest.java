package server.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequest {

    private User requester;
    private User requested;
    private Calendar date;
}
