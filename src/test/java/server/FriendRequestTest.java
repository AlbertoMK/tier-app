package server;

import org.junit.jupiter.api.Test;
import server.Database.MySqlConnector;
import server.Model.FriendRequest;
import server.Model.User;
import server.Utils.FriendRequestService;

import java.util.Calendar;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FriendRequestTest {

    User requester = new User("requester", "", Calendar.getInstance());
    User requested = new User("requested", "", Calendar.getInstance());
    FriendRequest request = new FriendRequest(requester, requested);

    @Test
    public void sendFriendRequestCorrectly() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findFriendRequestsByRequester(requester)).thenReturn(Set.of());
        when(connector.findByUsername(requester.getUsername())).thenReturn(Optional.of(requester));
        when(connector.findByUsername(requested.getUsername())).thenReturn(Optional.of(requested));

        FriendRequestService.init(connector);
        assertTrue(FriendRequestService.getInstance().addRequest("requester", "requested"));
        verify(connector).addFriendRequest(requester, requested);
    }

    @Test
    public void sendRepeatedFriendRequest() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findFriendRequestsByRequester(requester)).thenReturn(Set.of(request));
        when(connector.findByUsername(requester.getUsername())).thenReturn(Optional.of(requester));

        FriendRequestService.init(connector);
        assertFalse(FriendRequestService.getInstance().addRequest("requester", "requested"));
        verify(connector, never()).addFriendRequest(requester, requested);
    }

    @Test
    public void removeFriendRequestCorrectly() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findByUsername(requester.getUsername())).thenReturn(Optional.of(requester));
        when(connector.findByUsername(requested.getUsername())).thenReturn(Optional.of(requested));
        when(connector.findFriendRequestsByRequester(requester)).thenReturn(Set.of(request));

        FriendRequestService.init(connector);
        assertTrue(FriendRequestService.getInstance().removeRequest("requester", "requested"));
        verify(connector).deleteFriendRequest(requester, requested);
    }

    @Test
    public void removeFriendRequestIncorrectly() {
        MySqlConnector connector = mock(MySqlConnector.class);
        when(connector.findByUsername(requester.getUsername())).thenReturn(Optional.of(requester));
        when(connector.findFriendRequestsByRequester(requester)).thenReturn(Set.of());

        FriendRequestService.init(connector);
        assertFalse(FriendRequestService.getInstance().removeRequest("requester", "requested"));
        verify(connector, never()).deleteFriendRequest(requester, requested);
    }
}
