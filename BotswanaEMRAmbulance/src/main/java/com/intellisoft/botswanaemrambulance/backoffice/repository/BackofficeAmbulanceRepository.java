package com.intellisoft.botswanaemrambulance.backoffice.repository;

import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackofficeAmbulanceRepository extends JpaRepository<BackofficeAmbulance, String> {

    Boolean existsByRegNo(String regNo);
    BackofficeAmbulance findByRegNo(String regNo);
    List<BackofficeAmbulance> findAllByStatus(String status);

    List<BackofficeAmbulance> findAllByStatus(String status, Pageable pageable);
}