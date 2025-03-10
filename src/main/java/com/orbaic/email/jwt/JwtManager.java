package com.orbaic.email.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class JwtManager {

    @Value("${spring.jwt.secret}")
    String secret;

    public String generateToken(Map<String, Object> data, String issuer, long expiration) {
        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(expiration))
                .withPayload(data)
                .sign(Algorithm.HMAC256(secret));
    }
    public Map verify(String token) throws Exception {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
            String payload = new String(Base64.getUrlDecoder().decode(jwt.getPayload()));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(payload, Map.class);
        }catch (JWTVerificationException | JsonProcessingException e) {
            System.out.println("JWT verify: "+ e.getMessage());
            throw new Exception(e.getMessage());
        }
    }


    public boolean validateJwt(String token, String userDetails) {
        return token.trim().equals(userDetails);
    }
}
