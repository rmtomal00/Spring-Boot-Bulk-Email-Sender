package com.orbaic.email.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHelperManager {

    @Value("${spring.jwt.secret2}")
    String secret2;

    public String generateToken(Map<String, Object> data, String issuer, long expiration) {
        return JWT.create()
                .withPayload(data)
                .withIssuer(issuer)
                .withExpiresAt(Date.from(Instant.now().plusSeconds(expiration)))
                .sign(Algorithm.HMAC256(secret2.getBytes(StandardCharsets.UTF_8)));
    }
}
