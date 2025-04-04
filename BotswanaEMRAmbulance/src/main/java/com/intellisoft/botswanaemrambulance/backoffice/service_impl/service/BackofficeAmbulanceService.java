package com.intellisoft.botswanaemrambulance.backoffice.service_impl.service;

import com.intellisoft.botswanaemrambulance.DbAmbulance;
import com.intellisoft.botswanaemrambulance.Results;

public interface BackofficeAmbulanceService {

    Results addAmbulance(DbAmbulance dbAmbulance);
    Results getAmbulance(String id);
    Results getAmbulances(int pageNo, int itemNo, String ambulanceStatus);
    Results updateAmbulanceDetails(DbAmbulance dbAmbulance);
    Results addAmbulanceDrivers(String ambulanceId, String driverId);
    Results addHospitalAmbulance(String ambulanceId, String hospitalId);
    Results getDriverAmbulances(String driverId,  int pageNo, int itemNo);
    Results getHospitalAmbulances(String hospitalId, int pageNo, int itemNo);

}
