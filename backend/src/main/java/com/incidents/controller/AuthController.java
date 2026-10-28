package com.incidents.controller;

import com.incidents.dto.Dto.*;
import com.incidents.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(req));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) { return auth.login(req); }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) { return auth.refresh(req.refreshToken()); }

    @PostMapping("/logout")
    public Map<String, String> logout(@Valid @RequestBody RefreshRequest req) {
        auth.logout(req.refreshToken());
        return Map.of("message", "Deconnecte");
    }
}
