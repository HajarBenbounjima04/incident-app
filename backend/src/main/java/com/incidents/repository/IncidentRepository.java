package com.incidents.repository;

import com.incidents.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
    List<Incident> findByAssignedToIdOrderByCreatedAtDesc(Long userId);
    List<Incident> findAllByOrderByCreatedAtDesc();
    long countByStatus(Status status);
}
