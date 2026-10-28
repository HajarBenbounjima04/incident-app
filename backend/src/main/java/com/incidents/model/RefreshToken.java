package com.incidents.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    protected RefreshToken() {}
    public RefreshToken(String token, User user, Instant expiresAt) {
        this.token = token; this.user = user; this.expiresAt = expiresAt;
    }
    public String getToken() { return token; }
    public User getUser() { return user; }
    public Instant getExpiresAt() { return expiresAt; }
}
