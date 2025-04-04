package com.botswana.botswanaemrfacility.service;

import com.botswana.botswanaemrfacility.Results;

public interface FacilityService {

    Results getAllFacilities();
    Results getAllConstituencies();
    Results getAllDistricts();
    Results getServices();
    Results getFacilityTypes();
    Results getAllDhmt();
    Results getDistrictDhmt();
    Results getAllInfrastructure();
    Results getFacilityOwnership();
    Results getFacilityDetails(String facilityCode, String codeType);
    Results getPagedFacilities(String pageNo, String itemNo);
    Results getFacilityDetails(String facilityId);
}
