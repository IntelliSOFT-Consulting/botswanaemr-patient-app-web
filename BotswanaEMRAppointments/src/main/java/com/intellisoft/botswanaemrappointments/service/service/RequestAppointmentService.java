package com.intellisoft.botswanaemrappointments.service.service;

import com.intellisoft.botswanaemrappointments.Results;
import com.intellisoft.botswanaemrappointments.service.models.request.RequestAppointment;

import java.text.ParseException;

public interface RequestAppointmentService {

    Results requestAppointment(RequestAppointment requestAppointment) throws ParseException;

}
