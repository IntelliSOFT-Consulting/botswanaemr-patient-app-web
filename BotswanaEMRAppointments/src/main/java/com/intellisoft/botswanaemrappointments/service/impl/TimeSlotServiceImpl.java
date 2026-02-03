package com.intellisoft.botswanaemrappointments.service.impl;

import com.intellisoft.botswanaemrappointments.*;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppointmentType;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.RequestAppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Type;
import com.intellisoft.botswanaemrappointments.service.service.TimeSlotService;
import com.intellisoft.botswanaemrappointments.service.models.request.AppointmentReq;
import com.intellisoft.botswanaemrappointments.service.models.response.CreateAppointmentRes;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppResult;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Slot;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.SlotsResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.types.TypesResponse;
import com.intellisoft.botswanaemrappointments.utils.GenericWebClient;
import com.intellisoft.botswanaemrappointments.utils.exception.GenericBadRequestException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServerException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServiceException;
import com.intellisoft.botswanaemrappointments.utils.exception.CustomTimeoutException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map;

@Log4j2
@Service
public class TimeSlotServiceImpl implements TimeSlotService {
    @Value("${openmrs.username}")
    private String username;
    @Value("${openmrs.password}")
    private String password;
    @Value("${openmrs.timeslots}")
    private String timeSlotsUrl;
    @Value("${openmrs.types}")
    private String typesUrl;
    @Value("${openmrs.book}")
    private String createAppUrl;
    @Value("${openmrs.apps}")
    private String getAppsUrl;

    @Value("${openmrs.appointment}")
    private String getAppsRequest;
    @Value("${openmrs.slot}")
    private String slotUrl;

    @Value("${openmrs.appointment}")
    private String appointmentsUrl;

    @Value("${openmrs.schedule}")
    private String scheduleUrl;

    @Value("${openmrs.visits}")
    private String visitsUrl;

    @Value("${openmrs.url}")
    private String openmrsBaseUrl;

    private FormatterClass formatterClass = new FormatterClass();

    @Autowired
    NetworkCall networkCall = new NetworkCall();
    @Autowired
    private WebClient webClient;

    public static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
    @Override
    public List<DbTimeSlotData> fetchTimeSlots(String limit, String patientId, String appointmentType) {
        if (limit!=null)
            timeSlotsUrl
                    +="&limit="+limit
                    +"&excludeTimeSlotsPatientAlreadyBookedFor="+patientId
                    +"&appointmentType="+appointmentType;

        SlotsResponse slotsResponse = GenericWebClient.getRequest(webClient,timeSlotsUrl,
                getBasicAuthenticationHeader(username,password),SlotsResponse.class);

        List<DbTimeSlotData> dbTimeSlotDataList = new ArrayList<>();

        List<Slot> timeSlotsResults = slotsResponse.getResults();
        for (Slot slot: timeSlotsResults) {

            String uuid = slot.getUuid();
            String startDateTime = String.valueOf(slot.getStartDate());
            String endDateTime = String.valueOf(slot.getEndDate());

            int countOfAppointments = slot.getCountOfAppointments();
            int unallocatedMinutes = slot.getUnallocatedMinutes();

            String appointmentBlockUuid = slot.getAppointmentBlock().getUuid();
            String appointmentStartDate = String.valueOf(slot.getAppointmentBlock().getStartDate());
            String appointmentEndDate = String.valueOf(slot.getAppointmentBlock().getEndDate());

            String providerUuid = slot.getAppointmentBlock().getProvider().getPerson().getUuid();
            String providerName = slot.getAppointmentBlock().getProvider().getPerson().getDisplay();

            String locationUuid = slot.getAppointmentBlock().getLocation().getUuid();
            String locationName = slot.getAppointmentBlock().getLocation().getDisplay();

            List<DbTypesData> dbTypesDataList = new ArrayList<>();
            List<Type> types = slot.getAppointmentBlock().getTypes();
            for(Type type: types){

                String typeUuid = type.getUuid();
                String typeName = type.getDisplay();

                DbTypesData dbTypesData = new DbTypesData(
                        typeUuid,
                        typeName
                );
                dbTypesDataList.add(dbTypesData);

            }
            DbPerson dbProviderResults = new DbPerson(
                    providerUuid,
                    providerName
            );
            DbLocation dbLocationResults = new DbLocation(
                    locationUuid,
                    locationName
            );

            DbAppointmentData dbAppointmentData = new DbAppointmentData(
                    appointmentBlockUuid,
                    appointmentStartDate,
                    appointmentEndDate,
                    dbProviderResults,
                    dbLocationResults,
                    dbTypesDataList
            );
            DbTimeSlotData dbTimeSlotData = new DbTimeSlotData(
                    uuid,
                    startDateTime,
                    endDateTime,
                    dbAppointmentData,
                    countOfAppointments,
                    unallocatedMinutes);

            dbTimeSlotDataList.add(dbTimeSlotData);

        }


        return dbTimeSlotDataList;
    }

    @Override
    public TypesResponse fetchAppointmentTypes(String limit) {
        if (limit!=null)
            typesUrl+="&limit="+limit;
        return GenericWebClient.getRequest(webClient,typesUrl, getBasicAuthenticationHeader(username,password),TypesResponse.class);    }

    @Override
    public CreateAppointmentRes makeAppointment(AppointmentReq appointmentReq) {
        try {
            log.info("BODY: {}",appointmentReq);

            return GenericWebClient.postRequest(webClient,createAppUrl, getBasicAuthenticationHeader(username,password),appointmentReq,CreateAppointmentRes.class);

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof WebClientRequestException)
            {
                throw new ServerException("Unable to connect to downstream server, error: "+e.getMessage());
            }
            throw new GenericBadRequestException("Unable to create new appointment. Please check the submitted details: error: "+e.getMessage());
        }
    }



    @Override
    public DbMyAppointments fetchAppointments(Map<String, String> params, String appointmentType) {

        /**
         * Check the appointment type
         */

        String url = formatterClass.getBaseUrl(params, appointmentType, getAppsUrl, getAppsRequest);
        log.info("URL: {}",url);
        System.out.println("------" + url);

        if (appointmentType.equals(AppointmentTypeData.SCHEDULE_APPOINTMENT.name())){
            DbScheduleAppointmentSuccess appResponse = GenericWebClient.getRequest(webClient,url,
                    getBasicAuthenticationHeader(username,password),DbScheduleAppointmentSuccess.class);
            return new DbMyAppointments(null, appResponse);
        }else {
            DbRequestAppointmentSuccess appResponse = GenericWebClient.getRequest(webClient,url,
                    getBasicAuthenticationHeader(username,password),DbRequestAppointmentSuccess.class);
            return new DbMyAppointments(appResponse, null);
        }
    }

    @Override
    public Results allAppointments(Map<String, String> params) {

        //Schedule Appointments
        String scheduleAppointmentUrl = formatterClass.getBaseUrl(
                params, AppointmentTypeData.SCHEDULE_APPOINTMENT.name(), getAppsUrl, getAppsRequest);

        params.put("status", "PENDING");
        //Request Appointments
        String requestAppointmentUrl = formatterClass.getBaseUrl(
                params, AppointmentTypeData.REQUEST_APPOINTMENT.name(), getAppsUrl, getAppsRequest);


        //Get scheduled appointments
        DbScheduleAppointmentSuccess scheduleAppointmentSuccess = GenericWebClient.getRequest(webClient,scheduleAppointmentUrl,
                getBasicAuthenticationHeader(username,password),DbScheduleAppointmentSuccess.class);

        //Get request appointments
        DbRequestAppointmentSuccess requestAppointmentSuccess = GenericWebClient.getRequest(webClient,requestAppointmentUrl,
                getBasicAuthenticationHeader(username,password),DbRequestAppointmentSuccess.class);

        final List<Detail> appointmentDetailsList = new ArrayList<>();

        if (scheduleAppointmentSuccess != null){

            scheduleAppointmentSuccess.getResults().forEach(result -> {

                String appointmentType = null;
                String serviceUuid = null;
                String serviceName = null;
                String reason = null;
                String status = null;
                DbLocation locationDisplay = null;
                String provider = null;
                String startDate = null;
                String endDate = null;

                String uuid = result.getUuid();

                if (result.getAppointmentType() != null){
                    System.out.println("Appointment Result: " + result);
                    appointmentType = result.getAppointmentType().getDisplay();
                    serviceUuid = result.getAppointmentType().getUuid();
                    serviceName = result.getAppointmentType().getDisplay();
                }

                if (result.getTimeSlot() != null){
                    if (result.getTimeSlot().getAppointmentBlock() != null){
                        if (result.getTimeSlot().getAppointmentBlock().getProvider() != null){
                            if (result.getTimeSlot().getAppointmentBlock().getProvider().getPerson() != null){
                                provider = result.getTimeSlot().getAppointmentBlock().getProvider().getPerson().getDisplay();
                            }
                        }
                        // Extract location from appointmentBlock (primary source)
                        if (result.getTimeSlot().getAppointmentBlock().getLocation() != null){
                            com.intellisoft.botswanaemrappointments.DbLocation location = 
                                    result.getTimeSlot().getAppointmentBlock().getLocation();
                            if (location.getUuid() != null && location.getDisplay() != null){
                                locationDisplay = location;
                            }
                        }
                    }
                }

                // Fallback to appointmentType.location if appointmentBlock location is not available
                if (locationDisplay == null && result.getAppointmentType() != null){
                    if (result.getAppointmentType().getLocation() != null){
                        locationDisplay = result.getAppointmentType().getLocation();
                    }
                }

                if (result.getReason() != null){
                    reason = result.getReason();
                }

                if (result.getTimeSlot() != null){
                    startDate = result.getTimeSlot().getStartDate();
                    endDate = result.getTimeSlot().getEndDate();
                }

                if (result.getStatus() != null){
                    if (result.getStatus().getName() != null){
                        status = result.getStatus().getName();
                    }
                }

                ScheduledAppointmentDetails scheduledAppointmentDetails =
                        new ScheduledAppointmentDetails(
                        locationDisplay,
                        provider,
                        startDate,
                        endDate);

                Detail detail = new Detail(
                        uuid,
                        "SCHEDULED_APPOINTMENT",
                        appointmentType,
                        serviceUuid,
                        serviceName,
                        reason,
                        status,
                        null,
                        scheduledAppointmentDetails);

                appointmentDetailsList.add(detail);

            });


        }

        if (requestAppointmentSuccess != null){

            requestAppointmentSuccess.getResults().forEach(result -> {

                String appointmentType = null;
                String serviceUuid = null;
                String serviceName = null;
                String notes = null;
                String status = null;
                String provider = null;
                String requestedBy = null;
                String requestedOn = null;

                String uuid = result.getUuid();

                if (result.getAppointmentType() != null){
                    appointmentType = result.getAppointmentType().getDisplay();
                    serviceUuid = result.getAppointmentType().getUuid();
                    serviceName = result.getAppointmentType().getDisplay();
                }
                if (result.getNotes() != null){
                    notes = result.getNotes();
                }
                if (result.getStatus() != null){
                    status = result.getStatus();
                }
                if (result.getProvider() != null){
                    if (result.getProvider().getPerson() != null){
                        provider = result.getProvider().getPerson().getDisplay();
                    }
                }
                if (result.getRequestedBy() != null){
                    if (result.getRequestedBy().getPerson() != null){
                        requestedBy = result.getRequestedBy().getPerson().getDisplay();
                    }
                }
                if (result.getRequestedOn() != null){
                    requestedOn = result.getRequestedOn();
                }

                RequestedAppointmentDetails requestedAppointmentDetails =
                        new RequestedAppointmentDetails(
                        provider,
                        requestedBy, requestedOn);

                Detail detail = new Detail(
                        uuid,
                        "REQUESTED_APPOINTMENT",
                        appointmentType,
                        serviceUuid,
                        serviceName,
                        notes,
                        status,
                        requestedAppointmentDetails,
                        null);

                appointmentDetailsList.add(detail);


            });


        }


        //Sort the list by start date or requested on date
        appointmentDetailsList.sort(
                Comparator.comparing(
                        detail -> {
                            if (Objects.equals(detail.getAppointmentIdentifier(), "SCHEDULED_APPOINTMENT")){
                                if (detail.getScheduledAppointmentDetails() != null){
                                    return detail.getScheduledAppointmentDetails().getStartDate();
                                }else {
                                    return null;
                                }
                            }else {
                                if (detail.getRequestedAppointmentDetails() != null) {
                                    return detail.getRequestedAppointmentDetails().getRequestedOn();
                                } else
                                    return null;
                            }

                        }
                )
        );



        List<Detail> trimmedAppointmentList;
        //Check if params has limit and get it
        int limit = 0;
        if (params.containsKey("limit")){
            limit = Integer.parseInt(params.get("limit"));
        }
        //Trim the list to the limit

        System.out.println(appointmentDetailsList);

//        if (limit > 0){
//            trimmedAppointmentList = appointmentDetailsList.subList(0, limit);
//        }else {
//            trimmedAppointmentList = appointmentDetailsList;
//        }


        trimmedAppointmentList = appointmentDetailsList;
        int count = trimmedAppointmentList.size();

        Appointment appointment = new Appointment(count, trimmedAppointmentList);

        return new Results(200, appointment);
    }

    @Override
    public DbTimeSlotData getSlotDetails(String slotId) {
        try {
            Slot slot = GenericWebClient.getRequest(webClient,slotUrl+slotId, getBasicAuthenticationHeader(username,password),Slot.class);

            String uuid = slot.getUuid();
            String startDateTime = String.valueOf(slot.getStartDate());
            String endDateTime = String.valueOf(slot.getEndDate());

            int countOfAppointments = slot.getCountOfAppointments();
            int unallocatedMinutes = slot.getUnallocatedMinutes();

            String appointmentBlockUuid = slot.getAppointmentBlock().getUuid();
            String appointmentStartDate = String.valueOf(slot.getAppointmentBlock().getStartDate());
            String appointmentEndDate = String.valueOf(slot.getAppointmentBlock().getEndDate());

            String providerUuid = slot.getAppointmentBlock().getProvider().getPerson().getUuid();
            String providerName = slot.getAppointmentBlock().getProvider().getPerson().getDisplay();

            String locationUuid = slot.getAppointmentBlock().getLocation().getUuid();
            String locationName = slot.getAppointmentBlock().getLocation().getDisplay();

            List<DbTypesData> dbTypesDataList = new ArrayList<>();
            List<Type> types = slot.getAppointmentBlock().getTypes();
            for(Type type: types){

                String typeUuid = type.getUuid();
                String typeName = type.getDisplay();

                DbTypesData dbTypesData = new DbTypesData(
                        typeUuid,
                        typeName
                );
                dbTypesDataList.add(dbTypesData);

            }
            DbPerson dbProviderResults = new DbPerson(
                    providerUuid,
                    providerName
            );
            DbLocation dbLocationResults = new DbLocation(
                    locationUuid,
                    locationName
            );

            DbAppointmentData dbAppointmentData = new DbAppointmentData(
                    appointmentBlockUuid,
                    appointmentStartDate,
                    appointmentEndDate,
                    dbProviderResults,
                    dbLocationResults,
                    dbTypesDataList
            );

            return new DbTimeSlotData(
                    uuid,
                    startDateTime,
                    endDateTime,
                    dbAppointmentData,
                    countOfAppointments,
                    unallocatedMinutes);

        }catch (Exception exception) {
            throw new GenericBadRequestException("Unable to find slot: "+slotId);
        }
    }

    @Override
    public Results cancelRequestAppointment(DbCancelAppointment dbCancelAppointment) {

        String uuid = dbCancelAppointment.getUuid();
        String cancelUrl = appointmentsUrl + "/"+ uuid;
        
        try{
            // Update appointment status using PUT to avoid voiding the appointment
            // Using PUT follows the same pattern as modifyRequestAppointment which successfully updates appointments
            Map<String, String> requestPayload = new HashMap<>();
            String statusCode = dbCancelAppointment.getStatus() != null ? dbCancelAppointment.getStatus().getCode() : "CANCELLED";
            requestPayload.put("status", statusCode);

            log.info("Cancelling request appointment {} with payload: {}", uuid, requestPayload);

            DbCancelRequestAppointmentResponse cancelAppointmentResponse = GenericWebClient.putRequest(webClient, cancelUrl,
                    getBasicAuthenticationHeader(username, password), requestPayload, DbCancelRequestAppointmentResponse.class);

            if (cancelAppointmentResponse != null){

                String status = cancelAppointmentResponse.getStatus();
                String responseUuid = cancelAppointmentResponse.getUuid();
                if (status != null && responseUuid != null){
                    String expectedStatus = dbCancelAppointment.getStatus() != null ? dbCancelAppointment.getStatus().getCode() : null;
                    if (status.equals(expectedStatus) && responseUuid.equals(uuid)){
                        return new Results(200, new DbResultsData("The appointment was canceled successfully"));
                    }else {
                        return new Results(400, "The appointment could not be cancelled. Please try again.");
                    }
                }else {
                    return new Results(400,  "There was an issue. Please try again");
                }


            }else {
                return new Results(400, "The appointment could not be cancelled");
            }

        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "There was an issue cancelling the request.");
        }
    }

    @Override
    public Results cancelScheduledAppointment(DbCancelAppointment dbCancelAppointment) {

        String uuid = dbCancelAppointment.getUuid();
        String cancelUrl = scheduleUrl + "/"+ uuid;

        try{
            // Check current appointment status first to avoid unnecessary API calls for already-cancelled appointments
            // This also helps identify appointments that might have data integrity issues
            try {
                String getUrl = scheduleUrl + "/" + uuid;
                // Use DbCancelScheduledAppointmentResponse which properly handles status as object
                DbCancelScheduledAppointmentResponse currentAppointment = GenericWebClient.getRequest(webClient, getUrl,
                        getBasicAuthenticationHeader(username, password), DbCancelScheduledAppointmentResponse.class);
                
                if (currentAppointment != null && currentAppointment.getStatus() != null && currentAppointment.getStatus().getCode() != null) {
                    String currentStatus = currentAppointment.getStatus().getCode();
                    if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
                        log.info("Appointment {} is already cancelled, returning success", uuid);
                        // Get full appointment details for response
                        DbCancelAppointmentDetails appointmentDetails = getAppointmentDetailsForResponse(uuid, currentStatus);
                        DbCancelAppointmentResponse response = new DbCancelAppointmentResponse(
                                "The appointment was already cancelled.",
                                appointmentDetails
                        );
                        return new Results(200, response);
                    }
                }
            } catch (Exception e) {
                // If we can't get the current status, proceed with cancellation attempt
                log.debug("Could not check current appointment status for {}, proceeding with cancellation: {}", uuid, e.getMessage());
            }
            
            // Update appointment status using PUT to avoid voiding the appointment
            // Using PUT follows the same pattern as modifyScheduledAppointment which successfully updates appointments
            Map<String, String> requestPayload = new HashMap<>();
            String statusCode = dbCancelAppointment.getStatus() != null ? dbCancelAppointment.getStatus().getCode() : "CANCELLED";
            requestPayload.put("status", statusCode);

            log.info("Cancelling scheduled appointment {} with payload: {}", uuid, requestPayload);

            DbCancelScheduledAppointmentResponse cancelAppointmentResponse = GenericWebClient.putRequest(webClient, cancelUrl,
                    getBasicAuthenticationHeader(username, password), requestPayload, DbCancelScheduledAppointmentResponse.class);

            if (cancelAppointmentResponse != null){

                if (cancelAppointmentResponse.getStatus() != null){

                    String status = cancelAppointmentResponse.getStatus().getCode();
                    String responseUuid = cancelAppointmentResponse.getUuid();

                    log.info("Cancellation response - Status code: {}, Response UUID: {}, Expected status: {}, Expected UUID: {}", 
                            status, responseUuid, statusCode, uuid);

                    if (status != null && responseUuid != null){
                        // Compare status codes (response has code field, our request has string)
                        // Trim and compare case-insensitively to handle any whitespace or case issues
                        String responseStatusCode = status != null ? status.trim() : null;
                        String expectedStatusCode = statusCode != null ? statusCode.trim() : null;
                        
                        boolean statusMatches = responseStatusCode != null && expectedStatusCode != null 
                                && responseStatusCode.equalsIgnoreCase(expectedStatusCode);
                        boolean uuidMatches = responseUuid != null && uuid != null && responseUuid.equals(uuid);
                        
                        if (statusMatches && uuidMatches){
                            // Get full appointment details for response
                            DbCancelAppointmentDetails appointmentDetails = getAppointmentDetailsForResponse(uuid, responseStatusCode);
                            DbCancelAppointmentResponse response = new DbCancelAppointmentResponse(
                                    "The appointment was canceled successfully",
                                    appointmentDetails
                            );
                            return new Results(200, response);
                        }else {
                            log.warn("Status or UUID mismatch - Response status: '{}', Expected: '{}', Status match: {}, Response UUID: '{}', Expected UUID: '{}', UUID match: {}", 
                                    responseStatusCode, expectedStatusCode, statusMatches, responseUuid, uuid, uuidMatches);
                            return new Results(400, "The appointment could not be cancelled. Please try again.");
                        }
                    }else {
                        log.warn("Null values in response - Status: {}, Response UUID: {}", status, responseUuid);
                        return new Results(400, "There was an issue. Please try again");
                    }

                }else {
                    log.warn("Response status is null for appointment: {}", uuid);
                    return new Results(400, "There was an issue in cancelling the request. Please try again");

                }




            }else {
                return new Results(400, "The appointment could not be cancelled");
            }

        }catch (ServerException e){
            log.error("Server error while cancelling appointment: {} - OpenMRS error. Error: {}", uuid, e.getMessage(), e);
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                String normalized = errorMsg.toLowerCase();
                if (
                        (normalized.contains("nullpointerexception") && normalized.contains("appointmentresource1_9"))
                                || normalized.contains("associated visits")
                                || normalized.contains("system limitation")
                ) {
                    return new Results(503, "Issue 1: Unable to cancel this appointment due to a limitation in the appointment system (visit-linked appointment). Please contact support or try canceling from the OpenMRS interface directly.");
                }
                if (
                        (normalized.contains("propertyvalueexception") && normalized.contains("patientappointment.status"))
                                || normalized.contains("missing status history")
                                || normalized.contains("data integrity issue")
                ) {
                    return new Results(503, "Issue 2: Unable to cancel this appointment due to missing status history in the database. This appointment may have been created incorrectly. Please contact support.");
                }
            }
            return new Results(503, "Unable to cancel this appointment right now. Please try again or contact support.");
        }catch (ServiceException e){
            log.error("Service error while cancelling appointment: {}, Status: {}", uuid, e.getStatusCode(), e);
            return new Results(e.getStatusCode(), "Unable to cancel appointment. " + (e.getMessage() != null ? e.getMessage() : "Please try again."));
        }catch (CustomTimeoutException e){
            log.error("Timeout error while cancelling appointment: {}", uuid, e);
            return new Results(504, "The request timed out. Please try again.");
        }catch (Exception e){
            log.error("Unexpected error while cancelling appointment: {}", uuid, e);
            return new Results(500, "There was an issue cancelling the request. Please try again.");
        }
    }

    @Override
    public Results modifyScheduledAppointment(String appointmentId, DbModifyAppointment dbModifyAppointment) {
        String modifyUrl = scheduleUrl + "/" + appointmentId;

        try {
            // Build request payload with only provided fields
            Map<String, Object> requestPayload = new HashMap<>();
            if (dbModifyAppointment.getTimeSlot() != null && !dbModifyAppointment.getTimeSlot().isEmpty()) {
                requestPayload.put("timeSlot", dbModifyAppointment.getTimeSlot());
            }
            if (dbModifyAppointment.getAppointmentType() != null && !dbModifyAppointment.getAppointmentType().isEmpty()) {
                requestPayload.put("appointmentType", dbModifyAppointment.getAppointmentType());
            }
            if (dbModifyAppointment.getReason() != null && !dbModifyAppointment.getReason().isEmpty()) {
                requestPayload.put("reason", dbModifyAppointment.getReason());
            }

            if (requestPayload.isEmpty()) {
                return new Results(400, "At least one field must be provided for modification");
            }

            log.info("Modifying scheduled appointment {} with payload: {}", appointmentId, requestPayload);

            DbModifyScheduledAppointmentResponse modifyResponse = GenericWebClient.putRequest(webClient, modifyUrl,
                    getBasicAuthenticationHeader(username, password), requestPayload, DbModifyScheduledAppointmentResponse.class);

            if (modifyResponse != null && modifyResponse.getUuid() != null) {
                if (modifyResponse.getUuid().equals(appointmentId)) {
                    return new Results(200, new DbResultsData("The appointment was modified successfully"));
                } else {
                    return new Results(400, "The appointment could not be modified. Please try again.");
                }
            } else {
                return new Results(400, "The appointment could not be modified");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof WebClientRequestException) {
                return new Results(400, "Unable to connect to downstream server. Please try again.");
            }
            return new Results(400, "There was an issue modifying the appointment. Please try again.");
        }
    }

    @Override
    public Results modifyRequestAppointment(String appointmentId, DbModifyAppointment dbModifyAppointment) {
        String modifyUrl = appointmentsUrl + "/" + appointmentId;

        try {
            // Build request payload with only provided fields
            Map<String, Object> requestPayload = new HashMap<>();
            if (dbModifyAppointment.getAppointmentType() != null && !dbModifyAppointment.getAppointmentType().isEmpty()) {
                requestPayload.put("appointmentType", dbModifyAppointment.getAppointmentType());
            }
            if (dbModifyAppointment.getReason() != null && !dbModifyAppointment.getReason().isEmpty()) {
                requestPayload.put("notes", dbModifyAppointment.getReason());
            }
            // Note: timeSlot is not applicable for request appointments

            if (requestPayload.isEmpty()) {
                return new Results(400, "At least one field must be provided for modification");
            }

            log.info("Modifying request appointment {} with payload: {}", appointmentId, requestPayload);

            DbModifyRequestAppointmentResponse modifyResponse = GenericWebClient.putRequest(webClient, modifyUrl,
                    getBasicAuthenticationHeader(username, password), requestPayload, DbModifyRequestAppointmentResponse.class);

            if (modifyResponse != null && modifyResponse.getUuid() != null) {
                if (modifyResponse.getUuid().equals(appointmentId)) {
                    return new Results(200, new DbResultsData("The appointment was modified successfully"));
                } else {
                    return new Results(400, "The appointment could not be modified. Please try again.");
                }
            } else {
                return new Results(400, "The appointment could not be modified");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof WebClientRequestException) {
                return new Results(400, "Unable to connect to downstream server. Please try again.");
            }
            return new Results(400, "There was an issue modifying the appointment. Please try again.");
        }
    }

    /**
     * TODO: This method is not yet implemented. Currently, it is just a placeholder
     */

    @Override
    public Results getFacilityProviders(String queryName) {

//        GenericWebClient.getRequest(webClient,slotUrl+slotId, getBasicAuthenticationHeader(username,password),Slot.class);

        return null;
    }

    @Override
    public Results getFacilityServices() {

//        GenericWebClient.getRequest(webClient,slotUrl+slotId, getBasicAuthenticationHeader(username,password),Slot.class);
        return null;
    }

    @Override
    public Results getFacilityLocations() {

//        GenericWebClient.getRequest(webClient,slotUrl+slotId, getBasicAuthenticationHeader(username,password),Slot.class);

        return null;
    }

    @Override
    public Results getVisitHistory(Map<String, String> params) {
        try {
            String patientId = params.get("patient");
            if (patientId == null || patientId.isEmpty()) {
                return new Results(400, "Patient ID is required");
            }

            // Ensure we have the patient UUID (not identifier)
            String patientUuid = getPatientUuid(patientId);
            if (patientUuid == null || patientUuid.isEmpty()) {
                return new Results(400, "Could not find patient UUID. Please ensure the patient ID is valid.");
            }

            // Build visits URL with query parameters
            // visitsUrl already contains ?v=full, so we use & for additional parameters
            StringBuilder visitsUrlBuilder = new StringBuilder(visitsUrl);
            visitsUrlBuilder.append("&patient=").append(patientUuid);
            if (params.containsKey("fromDate")) {
                visitsUrlBuilder.append("&fromStartDate=").append(params.get("fromDate"));
            }
            if (params.containsKey("toDate")) {
                visitsUrlBuilder.append("&toStartDate=").append(params.get("toDate"));
            }
            String visitsUrlWithParams = visitsUrlBuilder.toString();

            // Fetch visits for the patient
            DbVisitResponse visitResponse = GenericWebClient.getRequest(webClient, visitsUrlWithParams,
                    getBasicAuthenticationHeader(username, password), DbVisitResponse.class);

            if (visitResponse == null || visitResponse.getResults() == null || visitResponse.getResults().isEmpty()) {
                return new Results(200, new DbVisitHistoryResponse(0, new ArrayList<>()));
            }

            // Get all completed appointments for the patient
            Map<String, String> appointmentParams = new LinkedHashMap<>();
            appointmentParams.put("patient", patientUuid);
            appointmentParams.put("status", "COMPLETED");
            if (params.containsKey("fromDate")) {
                appointmentParams.put("fromDate", params.get("fromDate"));
            }
            if (params.containsKey("toDate")) {
                appointmentParams.put("toDate", params.get("toDate"));
            }

            StringBuilder completedAppointmentsUrlBuilder = new StringBuilder(getAppsUrl);
            appointmentParams.forEach((key, value) -> {
                if (value != null) {
                    completedAppointmentsUrlBuilder.append("&").append(key).append("=").append(value);
                }
            });
            String completedAppointmentsUrl = completedAppointmentsUrlBuilder.toString();

            AppResponse completedAppointmentsResponse = null;
            try {
                completedAppointmentsResponse = GenericWebClient.getRequest(webClient, completedAppointmentsUrl,
                        getBasicAuthenticationHeader(username, password), AppResponse.class);
            } catch (Exception e) {
                log.warn("Could not fetch completed appointments: {}", e.getMessage());
            }

            // Create a map of visit UUIDs to appointments for quick lookup
            Map<String, AppResult> visitToAppointmentMap = new HashMap<>();
            if (completedAppointmentsResponse != null && completedAppointmentsResponse.getResults() != null) {
                for (AppResult appointment : completedAppointmentsResponse.getResults()) {
                    if (appointment.getVisit() != null) {
                        // Try to extract visit UUID from appointment's visit field
                        // The visit field might be a Map or a String UUID
                        String visitUuid = extractVisitUuid(appointment.getVisit());
                        if (visitUuid != null) {
                            visitToAppointmentMap.put(visitUuid, appointment);
                        }
                    }
                }
            }

            // Build visit history details
            List<DbVisitHistoryDetail> visitHistoryDetails = new ArrayList<>();
            for (DbVisit visit : visitResponse.getResults()) {
                if (visit.getUuid() == null) {
                    continue;
                }

                // Check if this visit has a linked completed appointment
                AppResult linkedAppointment = visitToAppointmentMap.get(visit.getUuid());

                // If no direct match, try to match by date/time proximity
                if (linkedAppointment == null && visit.getStartDatetime() != null) {
                    linkedAppointment = findAppointmentByDate(completedAppointmentsResponse, visit.getStartDatetime());
                }

                // Only include visits with completed appointments
                if (linkedAppointment != null) {
                    DbVisitHistoryDetail detail = buildVisitHistoryDetail(visit, linkedAppointment);
                    visitHistoryDetails.add(detail);
                }
            }

            // Apply limit if specified
            if (params.containsKey("limit")) {
                try {
                    int limit = Integer.parseInt(params.get("limit"));
                    if (limit > 0 && visitHistoryDetails.size() > limit) {
                        visitHistoryDetails = visitHistoryDetails.subList(0, limit);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid limit parameter: {}", params.get("limit"));
                }
            }

            // Sort by visit start date (most recent first)
            visitHistoryDetails.sort((a, b) -> {
                if (a.getVisitStartDate() == null && b.getVisitStartDate() == null) return 0;
                if (a.getVisitStartDate() == null) return 1;
                if (b.getVisitStartDate() == null) return -1;
                return b.getVisitStartDate().compareTo(a.getVisitStartDate());
            });

            DbVisitHistoryResponse response = new DbVisitHistoryResponse(visitHistoryDetails.size(), visitHistoryDetails);
            return new Results(200, response);

        } catch (Exception e) {
            log.error("Error fetching visit history: {}", e.getMessage(), e);
            if (e instanceof WebClientRequestException) {
                throw new ServerException("Unable to connect to downstream server, error: " + e.getMessage());
            }
            return new Results(500, "Error fetching visit history: " + e.getMessage());
        }
    }

    private String extractVisitUuid(Object visit) {
        if (visit == null) {
            return null;
        }
        if (visit instanceof String) {
            return (String) visit;
        }
        if (visit instanceof Map) {
            Map<?, ?> visitMap = (Map<?, ?>) visit;
            Object uuid = visitMap.get("uuid");
            if (uuid != null) {
                return uuid.toString();
            }
        }
        return null;
    }

    private AppResult findAppointmentByDate(AppResponse appointmentsResponse, String visitStartDate) {
        if (appointmentsResponse == null || appointmentsResponse.getResults() == null || visitStartDate == null) {
            return null;
        }

        // Try to find appointment with matching date/time
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        for (AppResult appointment : appointmentsResponse.getResults()) {
            if (appointment.getTimeSlot() != null && appointment.getTimeSlot().getStartDate() != null) {
                Date appointmentStartDate = appointment.getTimeSlot().getStartDate();
                String appointmentDate = dateFormat.format(appointmentStartDate);
                // Simple date matching (can be enhanced)
                if (appointmentDate != null && appointmentDate.startsWith(visitStartDate.substring(0, Math.min(10, visitStartDate.length())))) {
                    return appointment;
                }
            }
        }
        return null;
    }

    private DbVisitHistoryDetail buildVisitHistoryDetail(DbVisit visit, AppResult appointment) {
        // Extract visit information
        String visitUuid = visit.getUuid();
        String visitDisplay = visit.getDisplay();
        String visitStartDate = visit.getStartDatetime();
        String visitStopDate = visit.getStopDatetime();
        String visitType = visit.getVisitType() != null ? visit.getVisitType().getDisplay() : null;

        // Extract location
        DbLocation location = null;
        if (visit.getLocation() != null) {
            location = new DbLocation(
                    visit.getLocation().getUuid(),
                    visit.getLocation().getDisplay()
            );
        }

        // Extract provider from encounters
        String provider = null;
        if (visit.getEncounters() != null && !visit.getEncounters().isEmpty()) {
            DbEncounter firstEncounter = visit.getEncounters().get(0);
            if (firstEncounter.getProvider() != null && firstEncounter.getProvider().getPerson() != null) {
                provider = firstEncounter.getProvider().getPerson().getDisplay();
            }
        }

        // Extract appointment information
        String appointmentUuid = appointment.getUuid();
        String appointmentType = appointment.getAppointmentType() != null ? appointment.getAppointmentType().getDisplay() : null;
        String appointmentStatus = appointment.getStatus();
        String appointmentReason = appointment.getReason();

        // Build encounter information
        List<DbEncounterInfo> encounterInfos = new ArrayList<>();
        if (visit.getEncounters() != null) {
            for (DbEncounter encounter : visit.getEncounters()) {
                String encounterProvider = null;
                if (encounter.getProvider() != null && encounter.getProvider().getPerson() != null) {
                    encounterProvider = encounter.getProvider().getPerson().getDisplay();
                }
                String encounterType = encounter.getEncounterType() != null ? encounter.getEncounterType().getDisplay() : null;

                DbEncounterInfo encounterInfo = new DbEncounterInfo(
                        encounter.getUuid(),
                        encounter.getDisplay(),
                        encounter.getEncounterDatetime(),
                        encounterType,
                        encounterProvider
                );
                encounterInfos.add(encounterInfo);
            }
        }

        return new DbVisitHistoryDetail(
                visitUuid,
                visitDisplay,
                visitStartDate,
                visitStopDate,
                visitType,
                location,
                provider,
                appointmentUuid,
                appointmentType,
                appointmentStatus,
                appointmentReason,
                encounterInfos
        );
    }

    /**
     * Get appointment details for cancellation response by fetching full appointment data
     */
    private DbCancelAppointmentDetails getAppointmentDetailsForResponse(String uuid, String status) {
        try {
            // Fetch full appointment details using a Map to avoid deserialization issues with status object
            String getUrl = scheduleUrl + "/" + uuid;
            Map<String, Object> appointmentMap = GenericWebClient.getRequest(webClient, getUrl,
                    getBasicAuthenticationHeader(username, password), Map.class);
            
            if (appointmentMap != null) {
                String display = (String) appointmentMap.get("display");
                String appointmentType = null;
                String date = null;
                String time = null;
                String provider = null;
                String location = null;
                
                // Extract appointment type
                Map<String, Object> appointmentTypeMap = (Map<String, Object>) appointmentMap.get("appointmentType");
                if (appointmentTypeMap != null) {
                    appointmentType = (String) appointmentTypeMap.get("display");
                }
                
                // Extract date, time, provider, and location from timeSlot
                Map<String, Object> timeSlotMap = (Map<String, Object>) appointmentMap.get("timeSlot");
                if (timeSlotMap != null) {
                    // Extract date and time
                    String startDateStr = (String) timeSlotMap.get("startDate");
                    if (startDateStr != null) {
                        try {
                            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            Date startDate = inputFormat.parse(startDateStr);
                            date = dateFormat.format(startDate);
                            time = timeFormat.format(startDate);
                        } catch (Exception e) {
                            log.debug("Error parsing date: {}", e.getMessage());
                        }
                    }
                    
                    // Extract provider and location from appointmentBlock
                    Map<String, Object> appointmentBlockMap = (Map<String, Object>) timeSlotMap.get("appointmentBlock");
                    if (appointmentBlockMap != null) {
                        // Extract provider
                        Map<String, Object> providerMap = (Map<String, Object>) appointmentBlockMap.get("provider");
                        if (providerMap != null) {
                            Map<String, Object> personMap = (Map<String, Object>) providerMap.get("person");
                            if (personMap != null) {
                                provider = (String) personMap.get("display");
                            }
                        }
                        
                        // Extract location
                        Map<String, Object> locationMap = (Map<String, Object>) appointmentBlockMap.get("location");
                        if (locationMap != null) {
                            location = (String) locationMap.get("display");
                        }
                    }
                }
                
                return new DbCancelAppointmentDetails(
                        uuid,
                        status,
                        display,
                        appointmentType,
                        date,
                        time,
                        provider,
                        location
                );
            }
        } catch (Exception e) {
            log.warn("Could not fetch full appointment details for {}, returning basic info: {}", uuid, e.getMessage());
        }
        
        // Fallback to basic information
        return new DbCancelAppointmentDetails(
                uuid,
                status,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Get patient UUID from OpenMRS.
     * If the provided patientId is already a UUID format, return it.
     * Otherwise, search for the patient by identifier and return the UUID.
     *
     * @param patientId Patient identifier or UUID
     * @return Patient UUID or null if not found
     */
    private String getPatientUuid(String patientId) {
        // Check if it's already a UUID format (8-4-4-4-12 hexadecimal format)
        if (isUuidFormat(patientId)) {
            return patientId;
        }

        // If not a UUID, search for patient by identifier
        try {
            String patientSearchUrl = openmrsBaseUrl + "v1/patient?q=" + patientId + "&v=default";
            DbPatient patientResponse = GenericWebClient.getRequest(webClient, patientSearchUrl,
                    getBasicAuthenticationHeader(username, password), DbPatient.class);

            if (patientResponse != null && patientResponse.getResults() != null && !patientResponse.getResults().isEmpty()) {
                // Return the UUID of the first matching patient
                return patientResponse.getResults().get(0).getUuid();
            }
        } catch (Exception e) {
            log.warn("Could not fetch patient UUID for identifier {}: {}", patientId, e.getMessage());
        }

        return null;
    }

    /**
     * Check if a string is in UUID format (8-4-4-4-12 hexadecimal format)
     *
     * @param str String to check
     * @return true if the string is a UUID format
     */
    private boolean isUuidFormat(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        // UUID format: 8-4-4-4-12 hexadecimal characters
        // Example: e1512452-1eeb-42a9-9c64-37aa77240ab3
        String uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return str.matches(uuidPattern);
    }

}
