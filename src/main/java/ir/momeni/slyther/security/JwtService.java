package ir.momeni.slyther.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import ir.momeni.slyther.config.AppProperties;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;


/**
 * Service responsible for issuing and validating JSON Web Tokens (JWTs).
 * <p>
 * Configuration is sourced from {@link AppProperties}:
 * - Secret: symmetric signing key (HS256). Use a strong, high-entropy secret (>= 256 bits).
 * - Issuer: identifies your token issuer (included in the "iss" claim).
 * - Access token expiration (minutes): controls short-lived access token lifetime.
 * <p>
 * Notes:
 * - This service generates short-lived access tokens; refresh tokens should be managed elsewhere.
 * - Clock skew handling is delegated to the jjwt defaults unless configured at the parser level.
 */
@Service
public class JwtService {
    private final Key key;              // HMAC signing key derived from configured secret
    private final String issuer;        // "iss" claim value
    private final long accessExpMillis; // access token TTL in milliseconds

    public JwtService(AppProperties props) {
        var cfg = props.getSecurity().getJwt();
        this.key = Keys.hmacShaKeyFor(cfg.getSecret().getBytes());         // Requires a sufficiently long secret
        this.issuer = cfg.getIssuer();
        this.accessExpMillis = cfg.getAccessExpMins() * 60_000L;           // minutes -> milliseconds
    }


    /**
     * Generates a signed access token.
     *
     * @param subject the token subject (typically a username or user ID)
     * @param claims  additional custom claims to embed (e.g., roles, tenant, etc.)
     * @return compact JWT string signed with HS256
     */
    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)                                        // custom claims payload
                .setSubject(subject)                                      // "sub"
                .setIssuer(issuer)                                        // "iss"
                .setIssuedAt(Date.from(now))                              // "iat"
                .setExpiration(new Date(now.toEpochMilli() + accessExpMillis)) // "exp"
                .signWith(key, SignatureAlgorithm.HS256)                  // HMAC-SHA256 signature
                .compact();
    }


    /**
     * Parses and validates a compact JWT string.
     * <p>
     * On success returns the parsed {@link Jws} with {@link Claims}.
     * On failure, jjwt will throw a subclass of {@link JwtException} (e.g., ExpiredJwtException).
     *
     * @param token the compact JWT
     * @return parsed JWS with claims if signature and structure are valid
     * @throws JwtException if token is invalid, expired, or signature verification fails
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
