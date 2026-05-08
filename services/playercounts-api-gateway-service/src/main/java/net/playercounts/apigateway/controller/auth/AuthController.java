package net.playercounts.apigateway.controller.auth;

import net.playercounts.apigateway.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     * =========================================
     * Authentication Endpoints
     * =========================================
     */

    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok("login");
    }

    @PostMapping("/logout")
    public  ResponseEntity<?> logout() {
        return ResponseEntity.ok("logout");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok("status");
    }

    // Refresh JWT/session expiration
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        return ResponseEntity.ok("refresh");
    }

}