package com.incidents.service;

import com.incidents.dto.Dto.*;
import com.incidents.exception.ApiException;
import com.incidents.model.*;
import com.incidents.repository.*;
import com.incidents.security.JwtService;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final IncidentRepository incidents;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public UserService(UserRepository users, IncidentRepository incidents, RefreshTokenRepository refreshTokens,
                       PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.incidents = incidents; this.refreshTokens = refreshTokens;
        this.encoder = encoder; this.jwt = jwt;
    }

    public User current(String email) {
        return users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
    }

    public ProfileResponse profile(String email) { return ProfileResponse.of(current(email), null); }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileUpdateRequest req) {
        User u = current(email);
        String newEmail = req.email().trim().toLowerCase();
        boolean emailChanged = !newEmail.equals(u.getEmail());
        if (emailChanged && users.existsByEmail(newEmail)) throw ApiException.conflict("Email deja utilise");
        u.setFullName(req.fullName().trim());
        u.setEmail(newEmail);
        // Le JWT contient l'email : on emet toujours un nouveau token (indispensable si l'email change).
        String token = jwt.generateToken(newEmail, u.getRole().name());
        return ProfileResponse.of(users.save(u), token);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest req) {
        User u = current(email);
        if (!encoder.matches(req.oldPassword(), u.getPassword()))
            throw ApiException.badRequest("Ancien mot de passe incorrect");
        if (!req.newPassword().equals(req.confirmPassword()))
            throw ApiException.badRequest("La confirmation ne correspond pas au nouveau mot de passe");
        u.setPassword(encoder.encode(req.newPassword()));
        refreshTokens.deleteByUser(u);   // invalide les sessions existantes
    }

    // ---------- Administration ----------
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers(Role role) {
        List<User> list = role == null ? users.findAll() : users.findByRole(role);
        return list.stream().map(UserResponse::of).toList();
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled, String adminEmail) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        if (!enabled && u.getEmail().equals(adminEmail))
            throw ApiException.badRequest("Vous ne pouvez pas desactiver votre propre compte");
        u.setEnabled(enabled);
        if (!enabled) refreshTokens.deleteByUser(u);
        return UserResponse.of(users.save(u));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> statistics() {
        Map<String, Long> s = new LinkedHashMap<>();
        s.put("totalUsers", users.count());
        s.put("employees", users.countByRole(Role.EMPLOYEE));
        s.put("technicians", users.countByRole(Role.TECHNICIAN));
        s.put("admins", users.countByRole(Role.ADMIN));
        s.put("totalIncidents", incidents.count());
        s.put("open", incidents.countByStatus(Status.OPEN));
        s.put("assigned", incidents.countByStatus(Status.ASSIGNED));
        s.put("inProgress", incidents.countByStatus(Status.IN_PROGRESS));
        s.put("resolved", incidents.countByStatus(Status.RESOLVED));
        s.put("closed", incidents.countByStatus(Status.CLOSED));
        return s;
    }
}
