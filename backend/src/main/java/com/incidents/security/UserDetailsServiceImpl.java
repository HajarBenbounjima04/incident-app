package com.incidents.security;

import com.incidents.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;
    public UserDetailsServiceImpl(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        var u = users.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().name())      // => ROLE_EMPLOYEE / ROLE_TECHNICIAN / ROLE_ADMIN
                .disabled(!u.isEnabled())       // un compte desactive ne peut plus se connecter
                .build();
    }
}
