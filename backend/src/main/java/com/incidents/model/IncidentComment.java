package com.incidents.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Note de traitement ajoutee par un technicien. */
@Entity
public class IncidentComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected IncidentComment() {}
    public IncidentComment(Incident incident, User author, String content) {
        this.incident = incident; this.author = author; this.content = content;
    }
    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
