package ir.momeni.slyther.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import ir.momeni.slyther.config.AppProperties;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final String issuer;
    private final long accessExpMillis;

    public JwtService(AppProperties props) {
        var cfg = props.getSecurity().getJwt();
        this.key = Keys.hmacShaKeyFor(cfg.getSecret().getBytes());
        this.issuer = cfg.getIssuer();
        this.accessExpMillis = cfg.getAccessExpMins() * 60_000L;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(new Date(now.toEpochMilli() + accessExpMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
