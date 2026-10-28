package com.incidents.service;

import com.incidents.dto.Dto.*;
import com.incidents.exception.ApiException;
import com.incidents.model.*;
import com.incidents.repository.*;
import com.incidents.security.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final long refreshMs;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt,
                       @Value("${app.jwt.refresh-expiration-ms}") long refreshMs) {
        this.users = users; this.refreshTokens = refreshTokens; this.encoder = encoder;
        this.authManager = authManager; this.jwt = jwt; this.refreshMs = refreshMs;
    }

    @Transactional
    public ProfileResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw ApiException.conflict("Email deja utilise");
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.EMPLOYEE);          // role impose cote serveur, jamais choisi par le client
        return ProfileResponse.of(users.save(u), null);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        // Leve BadCredentialsException (401) ou DisabledException (403, compte desactive)
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User u = users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        return issue(u);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken rt = refreshTokens.findByToken(refreshToken)
                .orElseThrow(() -> ApiException.unauthorized("Refresh token invalide"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshTokens.deleteByToken(refreshToken);
            throw ApiException.unauthorized("Refresh token expire");
        }
        User u = rt.getUser();
        if (!u.isEnabled()) throw ApiException.forbidden("Compte desactive");
        return new AuthResponse(jwt.generateToken(u.getEmail(), u.getRole().name()), rt.getToken(),
                u.getRole().name(), u.getFullName(), u.getEmail());
    }

    @Transactional
    public void logout(String refreshToken) { refreshTokens.deleteByToken(refreshToken); }

    private AuthResponse issue(User u) {
        String access = jwt.generateToken(u.getEmail(), u.getRole().name());
        String refresh = UUID.randomUUID().toString() + UUID.randomUUID();
        refreshTokens.save(new RefreshToken(refresh, u, Instant.now().plusMillis(refreshMs)));
        return new AuthResponse(access, refresh, u.getRole().name(), u.getFullName(), u.getEmail());
    }
}
