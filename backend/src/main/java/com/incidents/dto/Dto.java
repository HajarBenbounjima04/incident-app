package com.incidents.dto;

import com.incidents.model.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Tous les objets echanges avec le frontend (le mot de passe n'apparait jamais dans une reponse). */
public final class Dto {
    private Dto() {}

    // ---------- Requetes ----------
    public record RegisterRequest(
            @NotBlank(message = "Le nom est obligatoire") String fullName,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caracteres") String password) {}

    public record LoginRequest(
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank(message = "Le mot de passe est obligatoire") String password) {}

    public record RefreshRequest(@NotBlank(message = "refreshToken obligatoire") String refreshToken) {}

    public record IncidentRequest(
            @NotBlank(message = "Le titre est obligatoire") @Size(max = 150, message = "Titre trop long (150 max)") String title,
            @NotBlank(message = "La description est obligatoire") @Size(max = 2000, message = "Description trop longue (2000 max)") String description,
            Category category,
            @NotNull(message = "La priorite est obligatoire") Priority priority) {}

    public record StatusRequest(@NotNull(message = "Le statut est obligatoire") Status status) {}

    public record CommentRequest(
            @NotBlank(message = "La note est obligatoire") @Size(max = 1000, message = "Note trop longue (1000 max)") String content) {}

    public record ProfileUpdateRequest(
            @NotBlank(message = "Le nom est obligatoire") String fullName,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email) {}

    public record PasswordChangeRequest(
            @NotBlank(message = "Ancien mot de passe obligatoire") String oldPassword,
            @NotBlank(message = "Nouveau mot de passe obligatoire")
            @Size(min = 8, max = 100, message = "Le nouveau mot de passe doit contenir au moins 8 caracteres") String newPassword,
            @NotBlank(message = "Confirmation obligatoire") String confirmPassword) {}

    // ---------- Reponses ----------
    public record AuthResponse(String token, String refreshToken, String role, String fullName, String email) {}

    /** token : renseigne uniquement pour PUT /api/profile (nouveau JWT car l'email est le sujet du token). */
    public record ProfileResponse(Long id, String fullName, String email, Role role, boolean enabled,
                                  LocalDateTime createdAt, String token) {
        public static ProfileResponse of(User u, String token) {
            return new ProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isEnabled(), u.getCreatedAt(), token);
        }
    }

    public record UserResponse(Long id, String fullName, String email, Role role, boolean enabled, LocalDateTime createdAt) {
        public static UserResponse of(User u) {
            return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isEnabled(), u.getCreatedAt());
        }
    }

    public record UserSummary(Long id, String fullName) {
        public static UserSummary of(User u) { return u == null ? null : new UserSummary(u.getId(), u.getFullName()); }
    }

    public record IncidentResponse(Long id, String title, String description, Category category, Priority priority,
                                   Status status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                   UserSummary createdBy, UserSummary assignedTo) {
        public static IncidentResponse of(Incident i) {
            return new IncidentResponse(i.getId(), i.getTitle(), i.getDescription(), i.getCategory(), i.getPriority(),
                    i.getStatus(), i.getCreatedAt(), i.getUpdatedAt(),
                    UserSummary.of(i.getCreatedBy()), UserSummary.of(i.getAssignedTo()));
        }
    }

    public record CommentResponse(Long id, String author, String content, LocalDateTime createdAt) {}
    public record HistoryResponse(Long id, String message, String performedBy, LocalDateTime createdAt) {}
    public record IncidentDetailResponse(IncidentResponse incident, List<HistoryResponse> history, List<CommentResponse> comments) {}

    public record ErrorResponse(int status, String message, LocalDateTime timestamp, Map<String, String> errors) {}

}
