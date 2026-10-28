package com.incidents.controller;

import com.incidents.dto.Dto.*;
import com.incidents.service.IncidentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService service;
    public IncidentController(IncidentService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<IncidentResponse> create(Principal p, @Valid @RequestBody IncidentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(p.getName(), req));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<IncidentResponse> my(Principal p) { return service.mine(p.getName()); }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public List<IncidentResponse> assigned(Principal p) { return service.assigned(p.getName()); }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IncidentResponse> all() { return service.all(); }

    // Controle de propriete effectue dans le service (findViewable)
    @GetMapping("/{id}")
    public IncidentDetailResponse get(@PathVariable Long id, Principal p) { return service.get(id, p.getName()); }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public IncidentResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req, Principal p) {
        return service.changeStatus(id, req.status(), p.getName());
    }

    @PutMapping("/{id}/assign/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public IncidentResponse assign(@PathVariable Long id, @PathVariable Long technicianId, Principal p) {
        return service.assign(id, technicianId, p.getName());
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long id, @Valid @RequestBody CommentRequest req, Principal p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addComment(id, req.content(), p.getName()));
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> comments(@PathVariable Long id, Principal p) { return service.comments(id, p.getName()); }

    @GetMapping("/{id}/history")
    public List<HistoryResponse> history(@PathVariable Long id, Principal p) { return service.history(id, p.getName()); }
}
