package com.intellisoft.botswanaemrappointments.controller;

import com.intellisoft.botswanaemrappointments.*;
import com.intellisoft.botswanaemrappointments.service.impl.NotificationServiceImpl;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppointmentType;
import com.intellisoft.botswanaemrappointments.service.service.RequestAppointmentService;
import com.intellisoft.botswanaemrappointments.service.service.TimeSlotService;
import com.intellisoft.botswanaemrappointments.service.models.request.AppointmentReq;
import com.intellisoft.botswanaemrappointments.service.models.request.RequestAppointment;
import com.intellisoft.botswanaemrappointments.service.models.response.CreateAppointmentRes;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Slot;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.SlotsResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.types.TypesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping(value = "/appointments/api/v1")
@RestController
@RequiredArgsConstructor
public class AppointmentsController {

    FormatterClass formatterClass = new FormatterClass();
    private final TimeSlotService timeSlotService;

    @Autowired
    NetworkCall networkCall = new NetworkCall();

    private final RequestAppointmentService requestAppointmentService;

    @Autowired
    NotificationServiceImpl notificationService = new NotificationServiceImpl();

    @Value("${authentication}")
    private String authUrl;
    @Value("${notification}")
    private String notificationUrl;

    @GetMapping(value = "/timeslots")
    public ResponseEntity<?> getTimeSlots(
            @RequestParam(value = "limit", required = false) String limit,
            @RequestParam(value = "appointmentType") String appointmentType

            ){

        Results results;
        Optional<String> currentUserLogin = formatterClass.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {

            UserDetails userDetails = getUserDetails(currentUserLogin.get());

            if (userDetails != null) {

                String patientId = userDetails.getOpenMrsId();
                List<DbTimeSlotData> slotsResponse = timeSlotService.fetchTimeSlots(limit, patientId, appointmentType);
                DbReturnDetails dbReturnDetails = new DbReturnDetails(slotsResponse.size(), slotsResponse);

                results = new Results(200, dbReturnDetails);


            }else{
                results = new Results(400, "User not found");
            }

        }else {
            results = new Results(403, "User not found");
        }

        return formatterClass.getResponse(results);
    }

    @GetMapping(value = "/types")
    public TypesResponse getAppointmentTypes(@RequestParam(value = "limit", required = false) String limit){
        return timeSlotService.fetchAppointmentTypes(limit);

    }

    @GetMapping(value = "/timeslots/{slotId}")
    public DbTimeSlotData getSlotDetails(@PathVariable("slotId") String slotId){
        return timeSlotService.getSlotDetails(slotId);

    }

    /**
     * Get patient appointments
     * @param limit
     * @return
     */
    @GetMapping(value = "/specific-appointments/")
    public ResponseEntity<?> getAppointments(
            @RequestParam(value = "appointmentType", required = true)  String appointmentType,

            @RequestParam(value = "status", required = false)  String status,
            @RequestParam(value = "fromDate", required = false)  String fromDate,
            @RequestParam(value = "limit", required = false) String limit
    ){

        Results results;
        Optional<String> currentUserLogin = formatterClass.getCurrentUserLogin();
        if (currentUserLogin.isPresent()){

            if (appointmentType.equals(AppointmentTypeData.REQUEST_APPOINTMENT.name())){
                if(status == null || status.isBlank() || status.isEmpty()){
                    results = new Results(400, "Status is required for " + AppointmentTypeData.REQUEST_APPOINTMENT.name());
                    return formatterClass.getResponse(results);
                }
            }

            String patient = currentUserLogin.get();

            UserDetails userDetails = getUserDetails(patient);

            if (userDetails != null) {

                String openMrsId = userDetails.getOpenMrsId();
                Map<String, String> params = new LinkedHashMap<>();

                if (limit != null ) params.put("limit", limit);
                if (fromDate != null ) params.put("fromDate", fromDate);

                if (appointmentType.equals(AppointmentTypeData.REQUEST_APPOINTMENT.name())){
                    params.put("status", status);
                }

                params.put("patient", openMrsId);

                DbMyAppointments appResponse = timeSlotService.fetchAppointments(params, appointmentType);
                results = new Results(200, appResponse);
                return formatterClass.getResponse(results);
            }else {
                results = new Results(400, "We could not find the user");

            }



        }else {
            results = new Results(400, "We could not find the user");
        }

        return formatterClass.getResponse(results);


    }

    //Get all appointments
    @GetMapping(value = "/my-appointments")
    public ResponseEntity<?> getMyAppointmensts(
            @RequestParam(value = "fromDate", required = false)  String fromDate,
            @RequestParam(value = "limit", required = false) String limit
    ){
        Results results;
        Optional<String> currentUserLogin = formatterClass.getCurrentUserLogin();
        if (currentUserLogin.isPresent()){

            String patient = currentUserLogin.get();
            //Get the openMrs Id
            UserDetails userDetails = getUserDetails(patient);

            if (userDetails != null) {

                String openMrsId = userDetails.getOpenMrsId();
                Map<String, String> params = new LinkedHashMap<>();

                if (limit != null ) params.put("limit", limit);
                if (fromDate != null ) params.put("fromDate", fromDate);

                params.put("patient", openMrsId);


                results = timeSlotService.allAppointments(params);
                return formatterClass.getResponse(results);
            }else {
                results = new Results(400, "Please try again after sometime.");

            }



        }else {
            results = new Results(400, "We could not find the user");
        }

        return formatterClass.getResponse(results);
    }



    //Schedule an appointment
    @PostMapping("schedule-appointment")
    public ResponseEntity<?> scheduleAppointment(@Valid @RequestBody DbScheduleNewAppointment dbScheduleNewAppointment) {

        Results results;
        Optional<String> currentUserLogin = formatterClass.getCurrentUserLogin();
        if (currentUserLogin.isPresent()){

            UserDetails userDetails = getUserDetails(currentUserLogin.get());

            if (userDetails != null){

                String openMrsId = userDetails.getOpenMrsId();
                AppointmentReq appointmentReq = new AppointmentReq(
                        dbScheduleNewAppointment.getAppointmentType(),
                        openMrsId,
                        dbScheduleNewAppointment.getReason(),
                        "SCHEDULED",
                        dbScheduleNewAppointment.getTimeSlot());
                CreateAppointmentRes createAppointmentRes = timeSlotService.makeAppointment(appointmentReq);
                if (createAppointmentRes != null){

                    String message = "Your appointment request was scheduled successfully.";
                    DbNotification dbNotification = new DbNotification(
                            "Appointment Created.",
                            message,
                            userDetails.getId(),
                            NotificationDetails.SYSTEM.name(),
                            NotificationDetails.PATIENT_APPOINTMENT.name());

                    notificationService.createNotification(dbNotification, notificationUrl);

                    results = new Results(201, new DbResultsData("The appointment was successfully scheduled"));
                }else {
                    results = new Results(400, "The appointment could not be scheduled. Please try again.");
                }

            }else {
                results = new Results(400, "Make sure the user is a patient");
            }

        }else {
            results = new Results(403, "Please use a valid token");
        }

        return formatterClass.getResponse(results);
    }
    private UserDetails getUserDetails(String keycloakId){
        return networkCall.getUserDetails(keycloakId, authUrl);
    }


    //Request appointment
    @PostMapping(value = "/request-appointment")
    public ResponseEntity<?> createApp(@RequestBody DbRequestAppointment dbRequestAppointment) throws ParseException {

        Results results;
        Optional<String> currentUserLogin = formatterClass.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {

            UserDetails userDetails = getUserDetails(currentUserLogin.get());

            if (userDetails != null) {

                String openMrsId = userDetails.getOpenMrsId();


                String appointmentType = dbRequestAppointment.getAppointmentType();
                String notes = dbRequestAppointment.getNotes();
                String provider = dbRequestAppointment.getProvider();

                RequestAppointment requestAppointment = new RequestAppointment();

                if (provider != null) {
                    requestAppointment.setProvider(provider);
                }else {
                    requestAppointment.setProvider(null);
                }

                requestAppointment.setNotes(notes);
                requestAppointment.setPatient(openMrsId);
                requestAppointment.setAppointmentType(appointmentType);

                /**
                 * TODO: Check on the 'requestedBy' field. Should it be the patient or the provider?
                 */
                requestAppointment.setRequestedBy(openMrsId);

                results =  requestAppointmentService.requestAppointment(requestAppointment);

                int statusCode = results.getCode();
                if (statusCode == 201 || statusCode == 200) {

                    String message = "Your appointment request has been received. You will be notified when it is approved.";
                    DbNotification dbNotification = new DbNotification(
                            "Appointment Created.",
                            message,
                            userDetails.getId(),
                            NotificationDetails.SYSTEM.name(),
                            NotificationDetails.PATIENT_APPOINTMENT.name());

                    notificationService.createNotification(dbNotification, notificationUrl);
                }


            }else {
                results = new Results(400, "Make sure the user is a patient");
            }

        }else {
            results = new Results(403, "Please use a valid token");
        }

        return formatterClass.getResponse(results);
    }


    //Cancel an appointment
    @PostMapping("/cancel-request-appointment/")
    public ResponseEntity<?> cancelRequestAppointment(
            @RequestParam(value = "appointmentId", required = true) String appointmentId
    ){

        DbCancelAppointment dbCancelAppointment = new DbCancelAppointment(appointmentId, "CANCELLED");

        Results results = timeSlotService.cancelRequestAppointment(dbCancelAppointment);
        return formatterClass.getResponse(results);

    }

    @PostMapping("/cancel-scheduled-appointment/")
    public ResponseEntity<?> cancelScheduledAppointment(
            @RequestParam(value = "appointmentId", required = true) String appointmentId
    ){

        DbCancelAppointment dbCancelAppointment = new DbCancelAppointment(appointmentId, "CANCELLED");

        Results results = timeSlotService.cancelScheduledAppointment(dbCancelAppointment);
        return formatterClass.getResponse(results);

    }

    /**
     * TODO: The following are not being used in making an appointment. Check on them after sometime
     *
     */

//    @GetMapping(value = "/providers")
//    public ResponseEntity<?> getAllProviders(
//            @RequestParam(value = "queryName", required = false) String queryName){
//
//        Results results = timeSlotService.getFacilityProviders(queryName);
//        return formatterClass.getResponse(results);
//    }

//    @GetMapping(value = "/locations")
//    public ResponseEntity<?> getLocations(){
//
//        Results results = timeSlotService.getFacilityLocations();
//        return formatterClass.getResponse(results);
//
//    }
//
//    @GetMapping(value = "/services")
//    public ResponseEntity<?> getFacilityServices(){
//
//        Results results = timeSlotService.getFacilityServices();
//        return formatterClass.getResponse(results);
//
//    }


}
