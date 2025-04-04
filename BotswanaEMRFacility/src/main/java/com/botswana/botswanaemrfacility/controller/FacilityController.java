package com.botswana.botswanaemrfacility.controller;

import com.botswana.botswanaemrfacility.service.FacilityServiceImpl;
import com.botswana.botswanaemrfacility.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/facility/api/v1")
@RestController
public class FacilityController {

    @Autowired
    private FacilityServiceImpl facilityService;

    //Get all facilities
    @GetMapping(value = "/all")
    public ResponseEntity<?> getFacility(){

        Results results = facilityService.getAllFacilities();
        return getResponse(results);

    }

    @GetMapping(value = "/")
    public ResponseEntity<?> getPagedFacility(
            @Param("pageNo") String pageNo,
            @Param("itemNo") String itemNo
    ){

        Results results = facilityService.getPagedFacilities(pageNo,itemNo);
        return getResponse(results);

    }
    @GetMapping(value = "/{facilityId}")
    public ResponseEntity<?> getFacilityDetails(@PathVariable("facilityId") String facilityId){

        Results results = facilityService.getFacilityDetails(facilityId);
        return getResponse(results);

    }

    @GetMapping(value = "/constituency")
    public ResponseEntity<?> getAllConstituencies(){

        Results results = facilityService.getAllConstituencies();
        return getResponse(results);

    }
    @GetMapping(value = "/district")
    public ResponseEntity<?> getAllDistricts(){

        Results results = facilityService.getAllDistricts();
        return getResponse(results);

    }
    @GetMapping(value = "/service")
    public ResponseEntity<?> getServices(){

        Results results = facilityService.getServices();
        return getResponse(results);

    }
    @GetMapping(value = "/type")
    public ResponseEntity<?> getFacilityTypes(){

        Results results = facilityService.getFacilityTypes();
        return getResponse(results);

    }
    @GetMapping(value = "/dhmt")
    public ResponseEntity<?> getAllDhmt(){

        Results results = facilityService.getAllDhmt();
        return getResponse(results);

    }
    @GetMapping(value = "/district-dhmt")
    public ResponseEntity<?> getDistrictDhmt(){

        Results results = facilityService.getDistrictDhmt();
        return getResponse(results);

    }
    @GetMapping(value = "/infrastructure")
    public ResponseEntity<?> getAllInfrastructure(){

        Results results = facilityService.getAllInfrastructure();
        return getResponse(results);

    }
    @GetMapping(value = "/ownership")
    public ResponseEntity<?> getFacilityOwnership(){

        Results results = facilityService.getFacilityOwnership();
        return getResponse(results);

    }
    @GetMapping(value = "/facility")
    public ResponseEntity<?> getFaciltiyDetailsByNewCode(
            @Param("facilityCode") String facilityCode,
            @Param("codeType") String codeType
    ){

        if (facilityCode != null && codeType != null){

            if (codeType.equals("new" ) || codeType.equals("old")){

                Results results = facilityService.getFacilityDetails(facilityCode, codeType);
                return getResponse(results);

            }else {
                return getResponse(new Results(400, "The code type provided is invalid"));
            }

        }else {
            return getResponse(new Results(400, "We could not validate the facility code or the code type"));
        }



    }

    private ResponseEntity<?> getResponse(Results results){

        int code = results.getCode();
        if (code == 200){

            return ResponseEntity
                    .status(code)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(results.getDetails());

        }else if (code == 500){
            return ResponseEntity.internalServerError().body(results);
        }else {
            return ResponseEntity.badRequest().body(results);
        }

    }



}
