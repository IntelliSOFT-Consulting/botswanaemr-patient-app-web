package com.botswana.botswanaemrfacility.service;

import com.botswana.botswanaemrfacility.*;
import com.botswana.botswanaemrfacility.database.FacilityInfo;
import com.botswana.botswanaemrfacility.database.FacilityInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FacilityServiceImpl implements FacilityService{

    @Autowired
    private FacilityInfoRepository facilityInfoRepository;

    FormatterClass formatterClass = new FormatterClass();

    @Autowired
    NetworkCall networkCalls = new NetworkCall();
   
    private Results getCacheFacilityData(String facilityDetails){

        FacilityInfo facilityInfo = facilityInfoRepository.findByFieldName(facilityDetails);
        Results results;
        if (facilityInfo != null){
            results = new Results(200, facilityInfo.getDbJson());
        }else {
            results = new Results(400, "The required field could not be found");
        }

        return results;


    }

    private Results getLiveFacilityData(DbQueryParam dbQueryParam, String type){

        formatterClass.addFacility(this, facilityInfoRepository);

        return networkCalls.getFacilityData(dbQueryParam,type);
    }





    @Override
    public Results getAllFacilities() {
        
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"ALL");
        }
        

    }


    @Override
    public Results getAllConstituencies() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            System.out.println("----1");
            return results;
        }else {
            System.out.println("----2");
            return getLiveFacilityData(null,"CONSTITUENCY");
        }

    }

    @Override
    public Results getAllDistricts() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"DISTRICT");
        }

    }

    @Override
    public Results getServices() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"SERVICE");
        }
    }

    @Override
    public Results getFacilityTypes() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"TYPE");
        }
    }

    @Override
    public Results getAllDhmt() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"DHMT");
        }
    }

    @Override
    public Results getDistrictDhmt() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"DHMT-DISTRICT");
        }
    }

    @Override
    public Results getAllInfrastructure() {
        Results results = getCacheFacilityData(FacilityField.FACILITY_DETAILS.name());
        if (results.getCode() == 200){
            return results;
        }else {
            return getLiveFacilityData(null,"INFRASTRUCTURE");
        }
    }

    @Override
    public Results getFacilityOwnership() {
        return getLiveFacilityData(null,"OWNERSHIP");
    }

    @Override
    public Results getFacilityDetails(String facilityCode, String codeType) {

        DbQueryParam dbQueryParam = new DbQueryParam(facilityCode, codeType);
        return getLiveFacilityData(dbQueryParam,"FACILITY_DETAILS_BY_CODE");

    }

    @Override
    public Results getPagedFacilities(String pageNo, String itemNo) {

        DbQueryParam dbQueryParam = new DbQueryParam(pageNo, itemNo);
        return getLiveFacilityData(dbQueryParam,"FACILITY_PAGED");

    }

    @Override
    public Results getFacilityDetails(String facilityId) {
        DbQueryParam dbQueryParam = new DbQueryParam(facilityId, facilityId);
        return getLiveFacilityData(dbQueryParam,"FACILITY_DETAILS");

    }


}
