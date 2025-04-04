package com.intellisoft.botswanaemrambulance.backoffice.service_impl.impl;

import com.intellisoft.botswanaemrambulance.*;
import com.intellisoft.botswanaemrambulance.backoffice.entity.BackofficeAmbulance;
import com.intellisoft.botswanaemrambulance.backoffice.entity.DispatchTeam;
import com.intellisoft.botswanaemrambulance.backoffice.entity.DriverDetails;
import com.intellisoft.botswanaemrambulance.backoffice.repository.DispatchTeamRepository;
import com.intellisoft.botswanaemrambulance.backoffice.service_impl.service.DispatchTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DispatchTeamServiceImpl implements DispatchTeamService {

    @Autowired
    private DriverServiceImpl driverService;
    
    @Autowired
    private DispatchTeamRepository dispatchTeamRepository;

    @Autowired
    private NetworkCall networkCall = new NetworkCall();

    @Autowired
    private BackofficeAmbulanceServiceImpl ambulanceService;


    @Override
    public Results getUsers(String emailAddress) {
        return driverService.getUsers(emailAddress);
    }

    @Override
    public Results getDispatchTeamsList(int pageNo, int itemNo, String sortField, String sortDirection) {
        return new Results(200, new DbResults(getPaginatedData(pageNo, itemNo, "", "")));
    }

    private List<DispatchTeam> getPaginatedData(int pageNo, int pageSize, String sortField, String sortDirection){

        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt";}else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC";}else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortPageField).ascending() :
                Sort.by(sortPageField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<DispatchTeam> page = dispatchTeamRepository.findAll(pageable);
        return page.getContent();

    }


    @Override
    public Results getDispatchTeam(String id) {
        DispatchTeam dispatchTeam = getDispatchTeamDetails(id);
        if (dispatchTeam != null) {
            return new Results(200, new DbResults(dispatchTeam));
        } else {
            return new Results(400, "The requested dispatcher could not be found.");
        }
    }

    private DispatchTeam getDispatchTeamDetails(String id){
        return dispatchTeamRepository.findById(id).orElse(null);
    }

    @Override
    public Results updateUserToDispatchTeam(String keycloakId) {
        /**
         * We're using keycloak id cause that's the provided endpoint from acl service
         * I'm saving the user postgres id in the dispatchTeam details table - not the keycloak id
         * The keycloak id is only used to get the user details from acl service and update dispatchTeam roles
         */

        PatientDetails userDetails = networkCall.getUserDetailsKeyCloak(keycloakId);
        if (userDetails != null) {

            String id = userDetails.getId();
            boolean exists = dispatchTeamRepository.existsByUserId(id);
            if (!exists){

                DispatchTeam dispatchTeamDetails = new DispatchTeam();
                dispatchTeamDetails.setUserId(userDetails.getId());
                dispatchTeamDetails.setPhoneNumber(userDetails.getPhoneNumber());
                dispatchTeamDetails.setEmailAddress(userDetails.getEmailAddress());
                dispatchTeamDetails.setUsername(userDetails.getUsername());
                dispatchTeamRepository.save(dispatchTeamDetails);

                DbUserRole dbUserRole = new DbUserRole(
                        keycloakId,"app-dispatch");

                networkCall.updateRole(dbUserRole);

                /**
                 * Make sure to update the user roles in keycloak
                 */

                //Create notification that user has not created a patient
                DbNotification dbNotification = new DbNotification(
                        "Client Role Update.",
                        "Your account has been updated to a dispatcher.",
                        userDetails.getId(),
                        NotificationDetails.SYSTEM.name(),
                        NotificationDetails.PATIENT_UPDATE.name());

                networkCall.createNotification("CREATE_NOTIFICATION", dbNotification);

                return new Results(200,
                        new DbResults("The user has been successfully updated in the dispatch team."));


            }else {
                return new Results(400, "The requested user is already in the dispatch team.");
            }


        } else {
            return new Results(400, "The requested user could not be found.");
        }
    }

    @Override
    public Results assignAmbulanceDriver(String ambulanceId, String driverId) {

        //Check if the driver has an ambulance assigned, also check if the ambulance is assigned to another driver
        DriverDetails driverDetails = driverService.getDriverDetails(driverId);
        if (driverDetails != null) {
            boolean hasAmbulance = driverDetails.isHasAmbulance();
            if (!hasAmbulance){

                //Check if ambulance is assigned to another driver
                BackofficeAmbulance ambulance = ambulanceService.getAmbulanceData(ambulanceId);
                if (ambulance != null){

                    String available = AmbulanceIncidentStatus.AVAILABLE.name();

                    //Assign the ambulance to the driver
                    ambulance.setHasDriver(true);
                    ambulance.setDriverId(driverId);
                    ambulance.setStatus(available);
                    BackofficeAmbulance updatedAmbulance = ambulanceService.updateAmbulance(ambulanceId, ambulance);

                    driverDetails.setHasAmbulance(true);

                    if (updatedAmbulance != null) {

                        DriverDetails updateDriverDetails = driverService.updateDriverDetails(driverDetails);
                        if (updateDriverDetails != null){

                            //Create notification that user has not created a patient
                            DbNotification dbNotification = new DbNotification(
                                    "Driver Update.",
                                    "You have been assigned ambulance reg no: " + ambulance.getRegNo(),
                                    driverId,
                                    NotificationDetails.SYSTEM.name(),
                                    NotificationDetails.DRIVER_UPDATE.name());

                            networkCall.createNotification("CREATE_NOTIFICATION", dbNotification);

                            return new Results(200, new DbResults("The ambulance has been assigned successfully to the driver."));
                        }else {

                            String offline = AmbulanceIncidentStatus.OFFLINE.name();
                            ambulance.setHasDriver(false);
                            ambulance.setDriverId(null);
                            ambulance.setStatus(offline);
                            ambulanceService.updateAmbulance(ambulanceId, ambulance);

                            return new Results(400, "The ambulance could not be assigned to the driver.");
                        }


                    } else {
                        return new Results(400, "The ambulance could not be assigned to the driver.");
                    }

                }else {
                    return new Results(400, "The ambulance does not exist.");
                }

            }else {
                return new Results(400, "The driver is already assigned to an ambulance.");
            }

        } else {
            return new Results(400, "The requested driver could not be found.");
        }

    }

    @Override
    public Results removeAmbulanceDriver(String driverId, String ambulanceId) {

        DriverDetails driverDetails = driverService.getDriverDetails(driverId);
        BackofficeAmbulance ambulance = ambulanceService.getAmbulanceData(ambulanceId);

        if (driverDetails != null){

            if (ambulance != null) {

                //Remove the ambulance from the driver
                ambulance.setHasDriver(false);
                ambulance.setDriverId(null);
                BackofficeAmbulance updatedAmbulance = ambulanceService.updateAmbulance(ambulanceId, ambulance);
                if (updatedAmbulance != null) {

                    //Create notification that user has not created a patient
                    DbNotification dbNotification = new DbNotification(
                            "Driver Update.",
                            "You have been removed from ambulance reg no: " + ambulance.getRegNo(),
                            driverId,
                            NotificationDetails.SYSTEM.name(),
                            NotificationDetails.DRIVER_UPDATE.name());

                    networkCall.createNotification("CREATE_NOTIFICATION", dbNotification);

                    return new Results(200, new DbResults("The ambulance has been removed successfully from the driver."));
                } else {
                    return new Results(400, "The ambulance could not be removed from the driver.");
                }

            }else {
                return new Results(400, "The ambulance does not exist.");
            }

        }else {
            return new Results(400, "The requested driver could not be found.");
        }

    }
}

