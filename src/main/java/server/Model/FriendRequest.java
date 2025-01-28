package server.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Calendar;

@Data
@AllArgsConstructor
public class FriendRequest {

    private User requester;
    private User requested;
    private Calendar date;
}
