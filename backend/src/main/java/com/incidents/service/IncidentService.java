package com.incidents.service;

import com.incidents.dto.Dto.*;
import com.incidents.exception.ApiException;
import com.incidents.model.*;
import com.incidents.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class IncidentService {
    private final IncidentRepository incidents;
    private final IncidentHistoryRepository histories;
    private final IncidentCommentRepository comments;
    private final UserRepository users;

    public IncidentService(IncidentRepository incidents, IncidentHistoryRepository histories,
                           IncidentCommentRepository comments, UserRepository users) {
        this.incidents = incidents; this.histories = histories; this.comments = comments; this.users = users;
    }

    private User current(String email) {
        return users.findByEmail(email).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
    }

    private Incident find(Long id) {
        return incidents.findById(id).orElseThrow(() -> ApiException.notFound("Incident introuvable"));
    }

    private boolean isOwner(Incident i, User u)    { return i.getCreatedBy().getId().equals(u.getId()); }
    private boolean isAssignee(Incident i, User u) { return i.getAssignedTo() != null && i.getAssignedTo().getId().equals(u.getId()); }

    /** Regle centrale : ADMIN voit tout, le proprietaire voit le sien, le technicien voit ceux qui lui sont affectes. */
    private Incident findViewable(Long id, User u) {
        Incident i = find(id);
        if (u.getRole() == Role.ADMIN || isOwner(i, u) || isAssignee(i, u)) return i;
        throw ApiException.forbidden("Acces interdit a cet incident");
    }

    private void log(Incident i, String msg, User author) { histories.save(new IncidentHistory(i, msg, author.getFullName())); }

    // ---------- Employe ----------
    public IncidentResponse create(String email, IncidentRequest req) {
        User u = current(email);
        Incident i = new Incident();
        i.setTitle(req.title().trim());
        i.setDescription(req.description().trim());
        i.setCategory(req.category() == null ? Category.OTHER : req.category());
        i.setPriority(req.priority());
        i.setStatus(Status.OPEN);
        i.setCreatedBy(u);
        incidents.save(i);
        log(i, "Incident cree", u);
        return IncidentResponse.of(i);
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> mine(String email) {
        return incidents.findByCreatedByIdOrderByCreatedAtDesc(current(email).getId()).stream().map(IncidentResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public IncidentDetailResponse get(Long id, String email) {
        Incident i = findViewable(id, current(email));
        List<HistoryResponse> h = histories.findByIncidentIdOrderByCreatedAtAsc(i.getId()).stream()
                .map(x -> new HistoryResponse(x.getId(), x.getMessage(), x.getAuthor(), x.getCreatedAt())).toList();
        List<CommentResponse> c = comments.findByIncidentIdOrderByCreatedAtAsc(i.getId()).stream()
                .map(x -> new CommentResponse(x.getId(), x.getAuthor().getFullName(), x.getContent(), x.getCreatedAt())).toList();
        return new IncidentDetailResponse(IncidentResponse.of(i), h, c);
    }

    // ---------- Technicien ----------
    @Transactional(readOnly = true)
    public List<IncidentResponse> assigned(String email) {
        return incidents.findByAssignedToIdOrderByCreatedAtDesc(current(email).getId()).stream().map(IncidentResponse::of).toList();
    }

    public IncidentResponse changeStatus(Long id, Status newStatus, String email) {
        User u = current(email);
        Incident i = find(id);
        if (u.getRole() == Role.TECHNICIAN && !isAssignee(i, u))
            throw ApiException.forbidden("Cet incident ne vous est pas affecte");
        if (i.getStatus() == Status.CLOSED && u.getRole() != Role.ADMIN)
            throw ApiException.forbidden("Incident CLOSED : seul un administrateur peut le modifier");
        Status old = i.getStatus();
        if (old == newStatus) throw ApiException.badRequest("L'incident a deja le statut " + newStatus);
        i.setStatus(newStatus);
        log(i, old + " -> " + newStatus, u);
        return IncidentResponse.of(i);
    }

    public CommentResponse addComment(Long id, String content, String email) {
        User u = current(email);
        Incident i = find(id);
        if (u.getRole() == Role.TECHNICIAN && !isAssignee(i, u))
            throw ApiException.forbidden("Cet incident ne vous est pas affecte");
        if (u.getRole() == Role.EMPLOYEE) throw ApiException.forbidden("Acces interdit");
        if (i.getStatus() == Status.CLOSED) throw ApiException.forbidden("Incident CLOSED : ajout de note impossible");
        IncidentComment c = comments.save(new IncidentComment(i, u, content.trim()));
        log(i, "Note de traitement ajoutee", u);
        return new CommentResponse(c.getId(), u.getFullName(), c.getContent(), c.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> comments(Long id, String email) {
        Incident i = findViewable(id, current(email));
        return comments.findByIncidentIdOrderByCreatedAtAsc(i.getId()).stream()
                .map(c -> new CommentResponse(c.getId(), c.getAuthor().getFullName(), c.getContent(), c.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(Long id, String email) {
        Incident i = findViewable(id, current(email));
        return histories.findByIncidentIdOrderByCreatedAtAsc(i.getId()).stream()
                .map(h -> new HistoryResponse(h.getId(), h.getMessage(), h.getAuthor(), h.getCreatedAt())).toList();
    }

    // ---------- Admin ----------
    @Transactional(readOnly = true)
    public List<IncidentResponse> all() {
        return incidents.findAllByOrderByCreatedAtDesc().stream().map(IncidentResponse::of).toList();
    }

    public IncidentResponse assign(Long incidentId, Long technicianId, String adminEmail) {
        User admin = current(adminEmail);
        Incident i = find(incidentId);
        User tech = users.findById(technicianId).orElseThrow(() -> ApiException.notFound("Utilisateur introuvable"));
        if (tech.getRole() != Role.TECHNICIAN) throw ApiException.badRequest("L'utilisateur choisi n'est pas un technicien");
        if (!tech.isEnabled()) throw ApiException.badRequest("Le technicien est desactive");
        if (i.getStatus() == Status.CLOSED || i.getStatus() == Status.RESOLVED)
            throw ApiException.badRequest("Impossible d'affecter un incident " + i.getStatus());
        i.setAssignedTo(tech);
        if (i.getStatus() == Status.OPEN) i.setStatus(Status.ASSIGNED);
        log(i, "Affecte a " + tech.getFullName(), admin);
        return IncidentResponse.of(i);
    }

    // ---------- Dashboard ----------
    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(String email) {
        User u = current(email);
        Map<String, Object> m = new LinkedHashMap<>();
        List<Incident> l;
        if (u.getRole() == Role.EMPLOYEE) l = incidents.findByCreatedByIdOrderByCreatedAtDesc(u.getId());
        else if (u.getRole() == Role.TECHNICIAN) l = incidents.findByAssignedToIdOrderByCreatedAtDesc(u.getId());
        else l = incidents.findAllByOrderByCreatedAtDesc();
        m.put("total", (long) l.size());
        m.put("open", count(l, Status.OPEN) + count(l, Status.ASSIGNED));
        m.put("inProgress", count(l, Status.IN_PROGRESS));
        m.put("resolvedOrClosed", count(l, Status.RESOLVED) + count(l, Status.CLOSED));
        m.put("critical", l.stream().filter(i -> i.getPriority() == Priority.CRITICAL).count());
        m.put("latest", l.stream().limit(5).map(IncidentResponse::of).toList());
        return m;
    }

    private long count(List<Incident> l, Status s) { return l.stream().filter(i -> i.getStatus() == s).count(); }
}
