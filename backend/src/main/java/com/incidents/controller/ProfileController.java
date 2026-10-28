package com.incidents.controller;

import com.incidents.dto.Dto.*;
import com.incidents.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserService users;
    public ProfileController(UserService users) { this.users = users; }

    @GetMapping
    public ProfileResponse get(Principal p) { return users.profile(p.getName()); }

    @PutMapping
    public ProfileResponse update(Principal p, @Valid @RequestBody ProfileUpdateRequest req) {
        return users.updateProfile(p.getName(), req);
    }

    @PutMapping("/password")
    public Map<String, String> password(Principal p, @Valid @RequestBody PasswordChangeRequest req) {
        users.changePassword(p.getName(), req);
        return Map.of("message", "Mot de passe modifie");
    }
}
