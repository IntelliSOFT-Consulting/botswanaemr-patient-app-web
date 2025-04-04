package com.intellisoft.botswanaemrambulance.incident.repository;

import com.intellisoft.botswanaemrambulance.incident.entity.IncidentRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRequestRepository extends JpaRepository<IncidentRequest, String> {
    List<IncidentRequest> findAllByIncidentStatus(String status, Pageable pageable);
}