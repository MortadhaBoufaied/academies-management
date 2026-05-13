package com.footballacademy.controllers_rest.auth;

import com.footballacademy.model.User;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.auth.JwtService;
import com.footballacademy.util.PasswordValidator;
import com.footballacademy.util.PasswordValidator.ValidationResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public
class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.
    class);
    private final AuthService authService;
    @Autowired final JwtService jwtService;
    public static
    record LoginRequest(String email, String password) {
    }
    public static
    record SignupRequest(String nom, String email, String mdp, String mainRole, Long academyId) {
    }
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(
    @Valid
    @RequestBody LoginRequest body, HttpServletRequest request) {
        try {
            logger.info("Login attempt for email: {}", body.email());
            if (body.email() == null || body.email() .isBlank()) {
                logger.warn("Login failed: Email is required");
                return ResponseEntity.badRequest() .body(Map.of("error", "Email is required"));
            }
            if (body.password() == null || body.password() .isBlank()) {
                logger.warn("Login failed: Password is required");
                return ResponseEntity.badRequest() .body(Map.of("error", "Password is required"));
            } var response = authService.authenticate(body.email(), body.password());
            logger.info("Login successful for email: {}", body.email());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Login failed for email: {} - {}", body.email(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected login error for email: {}", body.email(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Login error: " + e.getMessage()));
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
    @Valid
    @RequestBody SignupRequest body, HttpServletRequest request) {
        try {
            if (body.nom() == null || body.nom() .isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Name is required"));
            }
            if (body.email() == null || body.email() .isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Email is required"));
            }
            if (body.mdp() == null || body.mdp() .isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Password is required"));
            }
            if (body.mainRole() == null || body.mainRole() .isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "User role is required"));
            }
            // Validate password strength             PasswordValidator.
            ValidationResult passwordValidation = PasswordValidator.validate(body.mdp());
            if (!passwordValidation.isValid()) {
                logger.warn("Password validation failed for email: {}", body.email());
                return ResponseEntity.badRequest() .body(Map.of("error", passwordValidation.getMessage(), "requirements", PasswordValidator.generatePasswordRequirements()));
            }
            // Validate email format
            if (!isValidEmail(body.email())) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid email format"));
            } User user = new User();
            user.setNom(body.nom());
            user.setEmail(body.email());
            user.setMdp(body.mdp());
            try {
                user.setMainRole(User.UserRole.valueOf(body.mainRole() .toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid user role: " + body.mainRole() + ". Valid roles: SUPER_ADMIN, ADMIN, PLAYER, PARENT, TRAINER, SCOUTER"));
            } User created = authService.register(user, body.academyId());
            logger.info("User registered successfully: {}", created.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED) .body(created);
        } catch (RuntimeException e) {
            logger.error("Registration failed for email: {}", body.email(), e);
            return ResponseEntity.badRequest() .body(Map.of("error", "Registration failed: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during signup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Signup error: " + e.getMessage()));
        }
    }
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        } String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
    @RequestParam("refreshToken") String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Refresh token is required"));
            } var tokens = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Invalid refresh token: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Token refresh error: " + e.getMessage()));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
    @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Authorization token is required"));
            } String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            authService.logout(jwt);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Logout failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Logout error: " + e.getMessage()));
        }
    }
    /**  * Refresh access token using httpOnly refreshToken cookie (admin web flow).  * Returns new accessToken and sets a new accessToken cookie.  */
    @PostMapping("/refresh-cookie")
    public ResponseEntity<?> refreshCookie(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = null;
            if (request.getCookies() != null) {
                for (Cookie c : request.getCookies()) {
                    if ("refreshToken" .equals(c.getName())) {
                        refreshToken = c.getValue();
                        break;
                    }
                }
            }
            if (refreshToken == null || refreshToken.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Refresh token cookie is missing"));
            } var tokens = authService.refreshToken(refreshToken);
            String newAccess = tokens.get("accessToken");
            Cookie accessCookie = new Cookie("accessToken", newAccess);
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(accessCookie);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Invalid refresh token: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Refresh error: " + e.getMessage()));
        }
    }
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
    @RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("valid", false, "reason", "missing_or_invalid_authorization_header"));
        } String token = auth.substring(7);
        boolean valid = jwtService.isTokenValid(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
