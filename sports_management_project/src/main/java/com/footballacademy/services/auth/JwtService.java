package com.footballacademy.services.auth;

import com.footballacademy.security.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days
    private long refreshExpiration;

    // =====================================================
    // === CLAIM EXTRACTION
    // =====================================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractUserRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // =====================================================
    // === TOKEN GENERATION
    // =====================================================

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        if (userDetails instanceof UserPrincipal principal) {
            claims.put("userId", principal.getId());
            claims.put("role", principal.getMainRole().name());
        }
        return buildToken(claims, userDetails, jwtExpiration);
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", principal.getId());
        claims.put("role", principal.getMainRole().name());
        return buildToken(claims, principal, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String buildToken(Map<String, Object> extraClaims, UserPrincipal principal, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(principal.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =====================================================
    // === TOKEN VALIDATION
    // =====================================================

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenAboutToExpire(String token, long thresholdMs) {
        try {
            return extractExpiration(token).getTime() - System.currentTimeMillis() <= thresholdMs;
        } catch (Exception e) {
            return true;
        }
    }

    // =====================================================
    // === PARSING
    // =====================================================

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // =====================================================
    // === SIGNING KEY
    // =====================================================

    private Key getSignInKey() {
        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(secretKey);
        } catch (IllegalArgumentException e) {
            keyBytes = hexStringToByteArray(secretKey);
        }

        if (keyBytes.length < 32) {
            throw new RuntimeException("JWT secret key must be at least 256 bits");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static byte[] hexStringToByteArray(String s) {
        s = s.trim();
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }

        byte[] data = new byte[s.length() / 2];
        for (int i = 0; i < s.length(); i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) {
                throw new IllegalArgumentException("Invalid hex character");
            }
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }

    // =====================================================
    // === TOKEN INFO / UTILITIES
    // =====================================================

    public long getTimeUntilExpiration(String token) {
        try {
            return extractExpiration(token).getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return -1;
        }
    }

    public Map<String, Object> getTokenInfo(String token) {
        Map<String, Object> info = new HashMap<>();
        try {
            Claims claims = extractAllClaims(token);
            info.put("username", claims.getSubject());
            info.put("userId", claims.get("userId"));
            info.put("role", claims.get("role"));
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("timeUntilExpiration", getTimeUntilExpiration(token));
        } catch (Exception e) {
            info.put("error", "Invalid token");
        }
        return info;
    }

    // =====================================================
    // === VALIDATION RESULT
    // =====================================================

    public TokenValidationResult validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return new TokenValidationResult(false, "Token is empty");
            }
            Claims claims = extractAllClaims(token);
            if (isTokenExpired(token)) {
                return new TokenValidationResult(false, "Token has expired");
            }
            return new TokenValidationResult(true, "Token is valid", claims);
        } catch (ExpiredJwtException e) {
            return new TokenValidationResult(false, "Token has expired");
        } catch (JwtException e) {
            return new TokenValidationResult(false, "Invalid token");
        }
    }

    public static class TokenValidationResult {

        private final boolean valid;
        private final String message;
        private final Claims claims;

        public TokenValidationResult(boolean valid, String message) {
            this(valid, message, null);
        }

        public TokenValidationResult(boolean valid, String message, Claims claims) {
            this.valid = valid;
            this.message = message;
            this.claims = claims;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public Claims getClaims() {
            return claims;
        }
    }

    // =====================================================
    // === USER INFO
    // =====================================================

    public UserInfo extractUserInfo(String token) {
        TokenValidationResult result = validateToken(token);
        if (!result.isValid()) {
            throw new RuntimeException(result.getMessage());
        }
        Claims c = result.getClaims();
        return new UserInfo(
                c.getSubject(),
                c.get("userId", Long.class),
                c.get("role", String.class),
                c.getIssuedAt(),
                c.getExpiration()
        );
    }

    public static class UserInfo {

        private final String username;
        private final Long userId;
        private final String role;
        private final Date issuedAt;
        private final Date expiration;

        public UserInfo(String username, Long userId, String role,
                        Date issuedAt, Date expiration) {
            this.username = username;
            this.userId = userId;
            this.role = role;
            this.issuedAt = issuedAt;
            this.expiration = expiration;
        }

        public boolean isExpired() {
            return expiration.before(new Date());
        }

        public long getTimeUntilExpiration() {
            return expiration.getTime() - System.currentTimeMillis();
        }
    }
}
