package com.incidents.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Une ligne d'historique : creation, affectation, changement de statut... */
@Entity
public class IncidentHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Incident incident;

    @Column(nullable = false)
    private String message;

    private String author;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected IncidentHistory() {}
    public IncidentHistory(Incident incident, String message, String author) {
        this.incident = incident; this.message = message; this.author = author;
    }
    public Long getId() { return id; }
    public Incident getIncident() { return incident; }
    public String getMessage() { return message; }
    public String getAuthor() { return author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
