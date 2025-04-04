package com.intellisoft.botswanaemrappointments.service.service;

import com.intellisoft.botswanaemrappointments.DbCancelAppointment;
import com.intellisoft.botswanaemrappointments.DbMyAppointments;
import com.intellisoft.botswanaemrappointments.DbTimeSlotData;
import com.intellisoft.botswanaemrappointments.Results;
import com.intellisoft.botswanaemrappointments.service.models.request.AppointmentReq;
import com.intellisoft.botswanaemrappointments.service.models.response.CreateAppointmentRes;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Slot;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.SlotsResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.types.TypesResponse;

import java.util.List;
import java.util.Map;

public interface TimeSlotService {
    /**
     *
     * @return all available slots
     */
    List<DbTimeSlotData> fetchTimeSlots(String limit, String patientId, String appointmentType);

    /**
     *
     * @return all available appintment types/services
     */
    TypesResponse fetchAppointmentTypes(String limit);

    /**
     *
     * @param appointmentReq
     * @return status of post call
     */
    CreateAppointmentRes makeAppointment(AppointmentReq appointmentReq);

    /**
     *
     * @param params
     * @return appointments as per specified params
     */
    DbMyAppointments fetchAppointments(
            Map<String, String> params,
            String appointmentType);

    Results allAppointments(Map<String, String> params);

    /**
     *
     * @param slotId
     * @return details for a particular timeslot
     */
    DbTimeSlotData getSlotDetails(String slotId);

    /**
     * Cancel Appointment
     */
    Results cancelRequestAppointment(DbCancelAppointment dbCancelAppointment);
    Results cancelScheduledAppointment(DbCancelAppointment dbCancelAppointment);

    Results getFacilityProviders(String queryName);
    Results getFacilityServices();
    Results getFacilityLocations();

}
