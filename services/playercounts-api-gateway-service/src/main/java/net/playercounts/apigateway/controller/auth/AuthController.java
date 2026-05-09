package net.playercounts.apigateway.controller.auth;

import net.playercounts.apigateway.dto.request.LoginRequest;
import net.playercounts.apigateway.dto.response.LoginResponse;
import net.playercounts.apigateway.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
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