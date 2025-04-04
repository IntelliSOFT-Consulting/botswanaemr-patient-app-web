package com.intellisoft.botswanaemrambulance.backoffice.service_impl.service;

import com.intellisoft.botswanaemrambulance.Results;

public interface DispatchTeamService {

    //Search user from acl using email address
    Results getUsers(String emailAddress);
    Results getDispatchTeamsList(int pageNo, int itemNo, String sortField, String sortDirection);
    Results getDispatchTeam(String id);
    Results updateUserToDispatchTeam(String DispatchTeamId);

    Results assignAmbulanceDriver(String driverId, String AmbulanceDriverId);
    Results removeAmbulanceDriver(String driverId, String AmbulanceDriverId);


}
