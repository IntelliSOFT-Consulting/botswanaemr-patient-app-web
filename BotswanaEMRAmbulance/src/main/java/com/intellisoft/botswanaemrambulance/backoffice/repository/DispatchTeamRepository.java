package com.intellisoft.botswanaemrambulance.backoffice.repository;

import com.intellisoft.botswanaemrambulance.backoffice.entity.DispatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatchTeamRepository extends JpaRepository<DispatchTeam, String> {
    boolean existsByUserId(String userId);
}