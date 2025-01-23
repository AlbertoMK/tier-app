package server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.junit.jupiter.api.Test;
import server.Utils.PropertiesLoader;
import server.Utils.UserTokenService;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class TokenTest {

    @Test
    public void generateAndValidateToken() {
        String username = "username1";
        String token = UserTokenService.generateToken(username);
        assertEquals(username, UserTokenService.verifyToken(token));
    }

    @Test
    public void testExpiredToken() {
        String expiredToken = JWT.create()
                  .withSubject("testuser")
                  .withIssuedAt(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000))
                  .withExpiresAt(new Date(System.currentTimeMillis() - 60 * 60 * 1000))
                  .sign(Algorithm.HMAC256(PropertiesLoader.getProperty("usertokenservice.secretkey")));

        Exception exception = assertThrows(Exception.class, () -> {
            UserTokenService.verifyToken(expiredToken);
        });

        assertTrue(exception.getMessage().contains("The Token has expired"));
    }
}
