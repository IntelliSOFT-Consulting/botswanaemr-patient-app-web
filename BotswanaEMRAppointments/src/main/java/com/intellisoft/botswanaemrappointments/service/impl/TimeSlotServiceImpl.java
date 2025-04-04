package com.intellisoft.botswanaemrappointments.service.impl;

import com.intellisoft.botswanaemrappointments.*;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppointmentType;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.RequestAppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Type;
import com.intellisoft.botswanaemrappointments.service.service.TimeSlotService;
import com.intellisoft.botswanaemrappointments.service.models.request.AppointmentReq;
import com.intellisoft.botswanaemrappointments.service.models.response.CreateAppointmentRes;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.AppResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.Slot;
import com.intellisoft.botswanaemrappointments.service.models.response.slots.SlotsResponse;
import com.intellisoft.botswanaemrappointments.service.models.response.types.TypesResponse;
import com.intellisoft.botswanaemrappointments.utils.GenericWebClient;
import com.intellisoft.botswanaemrappointments.utils.exception.GenericBadRequestException;
import com.intellisoft.botswanaemrappointments.utils.exception.ServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.*;

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
                String reason = null;
                String status = null;
                String locationDisplay = null;
                String provider = null;
                String startDate = null;
                String endDate = null;

                String uuid = result.getUuid();

                if (result.getAppointmentType() != null){
                    appointmentType = result.getAppointmentType().getDisplay();
                    if (result.getAppointmentType().getLocation() != null){
                        locationDisplay = result.getAppointmentType().getLocation().getDisplay();
                    }
                }

                if (result.getTimeSlot() != null){
                    if (result.getTimeSlot().getAppointmentBlock() != null){
                        if (result.getTimeSlot().getAppointmentBlock().getProvider() != null){
                            if (result.getTimeSlot().getAppointmentBlock().getProvider().getPerson() != null){
                                provider = result.getTimeSlot().getAppointmentBlock().getProvider().getPerson().getDisplay();
                            }
                        }
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
                String notes = null;
                String status = null;
                String provider = null;
                String requestedBy = null;
                String requestedOn = null;

                String uuid = result.getUuid();

                if (result.getAppointmentType() != null){
                    appointmentType = result.getAppointmentType().getDisplay();
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

            DbCancelRequestAppointmentResponse cancelAppointmentResponse = GenericWebClient.postRequest(webClient, cancelUrl,
                    getBasicAuthenticationHeader(username, password), dbCancelAppointment, DbCancelRequestAppointmentResponse.class);

            if (cancelAppointmentResponse != null){

                String status = cancelAppointmentResponse.getStatus();
                String responseUuid = cancelAppointmentResponse.getUuid();
                if (status != null && responseUuid != null){

                    if (status.equals(dbCancelAppointment.getStatus()) && responseUuid.equals(uuid)){
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

            DbCancelScheduledAppointmentResponse cancelAppointmentResponse = GenericWebClient.postRequest(webClient, cancelUrl,
                    getBasicAuthenticationHeader(username, password), dbCancelAppointment, DbCancelScheduledAppointmentResponse.class);

            if (cancelAppointmentResponse != null){

                if (cancelAppointmentResponse.getStatus() != null){

                    String status = cancelAppointmentResponse.getStatus().getCode();
                    String responseUuid = cancelAppointmentResponse.getUuid();

                    if (status != null && responseUuid != null){

                        if (status.equals(dbCancelAppointment.getStatus()) && responseUuid.equals(uuid)){
                            return new Results(200, new DbResultsData("The appointment was canceled successfully"));
                        }else {
                            return new Results(400, "The appointment could not be cancelled. Please try again.");
                        }
                    }else {
                        return new Results(400, "There was an issue. Please try again");
                    }

                }else {
                    return new Results(400, "There was an issue in cancelling the request. Please try again");

                }




            }else {
                return new Results(400, "The appointment could not be cancelled");
            }

        }catch (Exception e){
            e.printStackTrace();
            return new Results(400, "There was an issue cancelling the request.");
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

}
