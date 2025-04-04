package com.botswana.botswanaemrfacility.database;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityInfoRepository extends JpaRepository<FacilityInfo, Integer> {

    Boolean existsByFieldName(String fieldName);
    FacilityInfo findByFieldName(String fieldName);
}