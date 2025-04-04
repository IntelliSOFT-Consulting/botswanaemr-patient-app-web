package com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl;

import com.intellisoft.botswanaemrambulance.AmbulanceIncidentStatus;
import com.intellisoft.botswanaemrambulance.DbAmbulance;
import com.intellisoft.botswanaemrambulance.DbResults;
import com.intellisoft.botswanaemrambulance.Results;
import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import com.intellisoft.botswanaemrambulance.backoffice.repository.BackofficeAmbulanceRepository;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.service.BackofficeAmbulanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BackofficeAmbulanceServiceImpl implements BackofficeAmbulanceService {

    @Autowired
    private BackofficeAmbulanceRepository backofficeAmbulanceRepository;

    @Override
    public Results addAmbulance(DbAmbulance dbAmbulance) {
        
        String regNo = dbAmbulance.getRegistrationNumber();
        String name = dbAmbulance.getName();
        
        boolean isRegNo = backofficeAmbulanceRepository.existsByRegNo(regNo);

        if (!isRegNo){

            String offline = AmbulanceIncidentStatus.OFFLINE.name();

            BackofficeAmbulance backofficeAmbulance = new BackofficeAmbulance();
            backofficeAmbulance.setName(name);
            backofficeAmbulance.setRegNo(regNo);
            backofficeAmbulance.setStatus(offline);
            backofficeAmbulance.setHasDriver(false);
            backofficeAmbulanceRepository.save(backofficeAmbulance);
            
            return new Results(200, new DbResults("Ambulance added successfully"));

        }else {
            return new Results(400, "The provided registration number is already in the system.");
        }
    }

    @Override
    public Results getAmbulance(String id) {

        BackofficeAmbulance backofficeAmbulance = getAmbulanceData(id);
        if (backofficeAmbulance != null){
            return new Results(200, backofficeAmbulance);
        }else {
            return new Results(400, "The requested ambulance could not be found.");
        }
    }

    public BackofficeAmbulance getAmbulanceData(String id){

        /**
         * Return driver details if it has driver
         */

        Optional<BackofficeAmbulance> optionalBackOffice = backofficeAmbulanceRepository.findById(id);
        return optionalBackOffice.orElse(null);

    }

    @Override
    public Results getAmbulances(int pageNo, int itemNo, String ambulanceStatus) {

        List<BackofficeAmbulance> backofficeAmbulanceList2 = backofficeAmbulanceRepository.findAllByStatus(
                ambulanceStatus, PageRequest.of(pageNo-1, itemNo));

        return new Results(200, new DbResults(backofficeAmbulanceList2));

    }

    @Override
    public Results updateAmbulanceDetails(DbAmbulance dbAmbulance) {

        String regNo = dbAmbulance.getRegistrationNumber();

        BackofficeAmbulance backofficeAmbulance = findByRegNo(regNo);

        String id = backofficeAmbulance.getId();
        String status = dbAmbulance.getStatus();

        backofficeAmbulance.setStatus(status);

        BackofficeAmbulance updateBackofficeAmbulance = updateAmbulance(id, backofficeAmbulance);
        if (updateBackofficeAmbulance != null){
            return new Results(200, new DbResults("The ambulance has been updated successfully."));
        }else {
            return new Results(400, "The ambulance could not be updated.");
        }
        
    }

    public BackofficeAmbulance findByRegNo(String regNo){
        return backofficeAmbulanceRepository.findByRegNo(regNo);
    }

    public BackofficeAmbulance updateAmbulance(String id, BackofficeAmbulance dbAmbulance){

        return backofficeAmbulanceRepository.findById(id)
                .map(backofficeAmbulanceOld ->{

                    backofficeAmbulanceOld.setDriverId(dbAmbulance.getDriverId());
                    backofficeAmbulanceOld.setHasDriver(dbAmbulance.isHasDriver());

                    backofficeAmbulanceOld.setStatus(dbAmbulance.getStatus());
                    backofficeAmbulanceOld.setRegNo(dbAmbulance.getRegNo());
                    backofficeAmbulanceOld.setName(dbAmbulance.getName());
                    return backofficeAmbulanceRepository.save(backofficeAmbulanceOld);
                }).orElse(null);
    }

    @Override
    public Results addAmbulanceDrivers(String ambulanceId, String driverId) {
        return null;
    }

    @Override
    public Results addHospitalAmbulance(String ambulanceId, String hospitalId) {
        return null;
    }

    @Override
    public Results getDriverAmbulances(String driverId, int pageNo, int itemNo) {
        return null;
    }

    @Override
    public Results getHospitalAmbulances(String hospitalId, int pageNo, int itemNo) {
        return null;
    }



    private List<BackofficeAmbulance> getPaginatedData(int pageNo, int pageSize, String sortField, String sortDirection){

        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt";}else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC";}else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(
                Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() :
                Sort.by(sortPageField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<BackofficeAmbulance> page = backofficeAmbulanceRepository.findAll(pageable);
        return page.getContent();

    }

}
