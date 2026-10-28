package com.incidents.controller;

import java.util.Map;
import com.incidents.service.IncidentService;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final IncidentService service;
    public DashboardController(IncidentService service) { this.service = service; }

    /** Le contenu depend du role de l'utilisateur authentifie. */
    @GetMapping
    public Map<String, Object> dashboard(Principal p) { return service.dashboard(p.getName()); }
}
