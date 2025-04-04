package com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl;

import com.intellisoft.botswanaemrambulance.*;
import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import com.intellisoft.botswanaemrambulance.backoffice.entity.DriverDetails;
import com.intellisoft.botswanaemrambulance.backoffice.repository.DriverDetailsRepository;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverServiceImpl implements DriverService {


    @Autowired
    private NetworkCall networkCall = new NetworkCall();

    @Autowired
    private DriverDetailsRepository driverDetailsRepository;

    @Override
    public Results getUsers(String emailAddress) {

        PatientDetails userDetails = networkCall.getUserDetailsEmailAddress(emailAddress);
        if (userDetails != null) {

            DbDriverData driverDetails = new DbDriverData(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmailAddress(),
                    null
            );

            return new Results(200, new DbResults(driverDetails));
        } else {
            return new Results(400, "The requested user could not be found.");
        }
    }

    @Override
    public Results updateUserToDriver(String keycloakId) {

        /**
         * We're using keycloak id cause that's the provided endpoint from acl service
         * I'm saving the user postgres id in the driver details table - not the keycloak id
         * The keycloak id is only used to get the user details from acl service and update driver roles
         */

        PatientDetails userDetails = networkCall.getUserDetailsKeyCloak(keycloakId);
        if (userDetails != null) {

            String id = userDetails.getId();
            boolean exists = driverDetailsRepository.existsByUserId(id);
            if (!exists){

                DriverDetails driverDetails = new DriverDetails();
                driverDetails.setUserId(userDetails.getId());
                driverDetails.setPhoneNumber(userDetails.getPhoneNumber());
                driverDetails.setEmailAddress(userDetails.getEmailAddress());
                driverDetails.setUsername(userDetails.getUsername());
                driverDetails.setHasAmbulance(false);
                driverDetailsRepository.save(driverDetails);

                DbUserRole dbUserRole = new DbUserRole(
                        keycloakId,"app-driver");
                networkCall.updateRole(dbUserRole);

                //Create notification that user has not created a patient
                DbNotification dbNotification = new DbNotification(
                        "Client Role Update.",
                        "Your account has been updated to a driver.",
                        userDetails.getId(),
                        NotificationDetails.SYSTEM.name(),
                        NotificationDetails.PATIENT_UPDATE.name());

                networkCall.createNotification("CREATE_NOTIFICATION", dbNotification);


                /**
                 * Make sure to update the user roles in keycloak
                 */

                return new Results(200, new DbResults("The user has been successfully updated to a driver."));


            }else {
                return new Results(400, "The requested user is already a driver.");
            }


        } else {
            return new Results(400, "The requested user could not be found.");
        }

    }

    @Override
    public Results getDriversList(int pageNo, int itemNo, String sortField, boolean sortDirection) {

        List<DriverDetails> driverDetailsList = driverDetailsRepository.findAllByHasAmbulance(
                sortDirection, PageRequest.of(pageNo-1, itemNo));

        return new Results(200, new DbResults(driverDetailsList));

    }

    private List<DriverDetails> getPaginatedData(int pageNo, int pageSize, String sortField, String sortDirection){

        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt";}else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC";}else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() :
                Sort.by(sortPageField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<DriverDetails> page = driverDetailsRepository.findAll(pageable);
        return page.getContent();

    }

    @Override
    public Results getDriver(String id) {

        DriverDetails driverDetails = getDriverDetails(id);
        if (driverDetails != null) {
            return new Results(200, new DbResults(driverDetails));
        } else {
            return new Results(400, "The requested driver could not be found.");
        }

    }


    public DriverDetails getDriverDetails(String id){
        return driverDetailsRepository.findById(id).orElse(null);
    }

    public DriverDetails updateDriverDetails(DriverDetails driverDetails){

        String id = driverDetails.getUserId();

        return driverDetailsRepository.findById(id)
                .map(driverDetailsOld ->{
                    driverDetailsOld.setHasAmbulance(driverDetails.isHasAmbulance());
                    return driverDetailsRepository.save(driverDetailsOld);
                }).orElse(null);

    }


}
