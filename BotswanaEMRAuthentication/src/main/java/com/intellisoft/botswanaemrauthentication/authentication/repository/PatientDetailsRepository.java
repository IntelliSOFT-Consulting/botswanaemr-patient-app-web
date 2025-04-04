package com.intellisoft.botswanaemrauthentication.authentication.repository;

import com.intellisoft.botswanaemrauthentication.authentication.entity.PatientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientDetailsRepository extends JpaRepository<PatientDetails, String> {

    Boolean existsByEmailAddress(String emailAddress);
    PatientDetails findByEmailAddress(String emailAddress);
    Boolean existsByUserKeycloakId(String keycloakId);
    PatientDetails findByUserKeycloakId(String keycloakId);

    Boolean existsByPatientIdentificationNo(String pin);

}
