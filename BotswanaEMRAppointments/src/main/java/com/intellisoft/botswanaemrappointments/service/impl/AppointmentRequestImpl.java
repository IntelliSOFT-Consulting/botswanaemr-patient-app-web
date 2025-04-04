package com.intellisoft.botswanaemrappointments.service.impl;

import com.intellisoft.botswanaemrappointments.DbNotification;
import com.intellisoft.botswanaemrappointments.NotificationDetails;
import com.intellisoft.botswanaemrappointments.Results;
import com.intellisoft.botswanaemrappointments.service.service.RequestAppointmentService;
import com.intellisoft.botswanaemrappointments.service.models.request.RequestAppointment;
import com.intellisoft.botswanaemrappointments.service.models.response.appointments.RequestAppResponse;
import com.intellisoft.botswanaemrappointments.utils.GenericWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static com.intellisoft.botswanaemrappointments.service.impl.TimeSlotServiceImpl.getBasicAuthenticationHeader;


@Service
public class AppointmentRequestImpl implements RequestAppointmentService {

    @Value("${openmrs.appointment}")
    private String appointmentsUrl;
    @Autowired
    private WebClient webClient;
    @Value("${openmrs.username}")
    private String username;
    @Value("${openmrs.password}")
    private String password;



    @Override
    public Results requestAppointment(RequestAppointment requestAppointment) throws ParseException {

        //Get the current date time
        Date date = new Date();

        //Get the current date and time
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = isoFormat.format(date);

        requestAppointment.setRequestedOn(currentDateTime);

        RequestAppResponse requestAppointmentResponse = GenericWebClient.postRequest(webClient, appointmentsUrl,
                getBasicAuthenticationHeader(username, password), requestAppointment, RequestAppResponse.class);
        if (requestAppointmentResponse != null){

            return new Results(200, requestAppointmentResponse);
        }else {
            return new Results(400, "Appointment not created");
        }


    }


}
