package com.intellisoft.botswanaemrambulance.backoffice.service_impl.service;

import com.intellisoft.botswanaemrambulance.Results;

public interface DriverService {

    //Search user from acl using email address
    Results getUsers(String emailAddress);
    Results getDriversList(int pageNo, int itemNo, String sortField, boolean hasAmbulance);
    Results getDriver(String id);
    Results updateUserToDriver(String driverId);


}
