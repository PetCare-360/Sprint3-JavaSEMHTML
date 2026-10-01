package br.com.fiap.petcare360_java.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String ISSUER = "petcare360-api";
    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final long expirationSeconds;

    public JwtService(@Value("${PETCARE360_JWT_SECRET}") String secret,
            @Value("${PETCARE360_JWT_EXPIRATION_MINUTES:120}") long expirationMinutes) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32 || secret.isBlank()) {
            throw new IllegalArgumentException("PETCARE360_JWT_SECRET deve conter pelo menos 32 bytes");
        }
        if (expirationMinutes < 1 || expirationMinutes > 1440) {
            throw new IllegalArgumentException("PETCARE360_JWT_EXPIRATION_MINUTES deve estar entre 1 e 1440");
        }
        var key = new SecretKeySpec(keyBytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
        expirationSeconds = expirationMinutes * 60;
    }

    public String issue(Authentication authentication) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder()
                .issuer(ISSUER).subject(authentication.getName())
                .id(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plusSeconds(expirationSeconds))
                .claim("roles", authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority()).toList())
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    public Jwt decode(String token) {
        Jwt jwt = decoder.decode(token);
        if (jwt.getSubject() == null || jwt.getSubject().isBlank() || jwt.getExpiresAt() == null
                || !jwt.getExpiresAt().isAfter(Instant.now())) {
            throw new BadJwtException("Token sem identificacao ou expirado");
        }
        return jwt;
    }

    public long expiresIn() {
        return expirationSeconds;
    }
}
