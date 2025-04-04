package com.intellisoft.botswanaemrauthentication.consent.repository;

import com.intellisoft.botswanaemrauthentication.consent.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsentRepository extends JpaRepository<Consent, String> {
    List<Consent> findByUserId(String userId);
}