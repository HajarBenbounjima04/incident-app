package com.incidents.controller;

import com.incidents.dto.Dto.UserResponse;
import com.incidents.model.Role;
import com.incidents.service.UserService;
import java.security.Principal;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** /api/admin/** est deja restreint a ADMIN dans SecurityConfig ; @PreAuthorize ajoute une 2e barriere. */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserService users;
    public AdminController(UserService users) { this.users = users; }

    @GetMapping("/users")
    public List<UserResponse> list(@RequestParam(required = false) Role role) { return users.listUsers(role); }

    @PutMapping("/users/{id}/enable")
    public UserResponse enable(@PathVariable Long id, Principal p) { return users.setEnabled(id, true, p.getName()); }

    @PutMapping("/users/{id}/disable")
    public UserResponse disable(@PathVariable Long id, Principal p) { return users.setEnabled(id, false, p.getName()); }

    @GetMapping("/statistics")
    public Map<String, Long> statistics() { return users.statistics(); }
}
