package com.intellisoft.botswanaemrambulance.backoffice.repository;

import com.intellisoft.botswanaemrambulance.backoffice.entity.DriverDetails;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverDetailsRepository extends JpaRepository<DriverDetails, String> {

    boolean existsByUserId(String userId);
    List<DriverDetails> findAllByHasAmbulance(boolean hasAmbulance, Pageable pageable);
}