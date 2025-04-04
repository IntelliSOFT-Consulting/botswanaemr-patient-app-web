package com.intellisoft.botswanaemrambulance.incident.repository;

import com.intellisoft.botswanaemrambulance.incident.entity.IncidentRejections;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRejectionsRepository extends JpaRepository<IncidentRejections, String> {
}