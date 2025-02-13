package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String ROLE = "role";
    private static final String AUTHORITIES = "authorities";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-access-token}")
    private String expirationTokenTime;

    @Override
    public AccessToken generateAccessToken(User user) {
        var expirationTokenSeconds = Long.parseLong(expirationTokenTime);
        Date expirationTime = DateUtils.currentTimePlusSeconds(expirationTokenSeconds);
        Long expiresIn = expirationTokenSeconds;
        String accessToken = generateToken(user, expirationTime);
        return new AccessToken(accessToken, expirationTime, expiresIn, user);
    }

    @Override
    public String extractUsername(String jwt) {
        return extractAllClaims(jwt).getSubject();
    }

    private Claims extractAllClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(generateKey()).build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    @Override
    public Long getValidationTokenTime(){
        return Long.parseLong(expirationTokenTime) * 1000;
    }

    private String generateToken(User user, Date expirationDate) {
        return Jwts.builder()
                .claims(generateExtraClaims(user))
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .header().type("JWT").and()
                .signWith(generateKey())
                .compact();
    }

    private Map<String, Object> generateExtraClaims(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(ROLE, user.getFlattenRoles());
        extraClaims.put(AUTHORITIES, user.getAuthorities());
        return extraClaims;
    }

    private SecretKey generateKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
