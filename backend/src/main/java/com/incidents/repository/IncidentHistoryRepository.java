package com.incidents.repository;

import com.incidents.model.IncidentHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {
    List<IncidentHistory> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
