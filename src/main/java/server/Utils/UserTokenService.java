package server.Utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class UserTokenService {

    private static final String SECRET_KEY = PropertiesLoader.getProperty("usertokenservice.secretkey");
    private static final long EXPIRATION_TIME = 60 * 60 * 1000; // 1 hour
    private static final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public static String generateToken(String username) {
        return JWT.create()
                  .withSubject(username)
                  .withIssuedAt(new Date())
                  .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                  .sign(algorithm);
    }

    /**
     * This method to verify a session token
     *
     * @param token The session token given by the user
     * @return Username of the user if the token is valid. Throws exception otherwise.
     */
    public static String verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build(); // creates the object in charge of verifying the token
        DecodedJWT decodedJWT = verifier.verify(token); // decodes the token and gets parameters

        return decodedJWT.getSubject();
    }
}
