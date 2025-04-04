package com.intellisoft.botswanaemrappointments.service.slots.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellisoft.botswanaemrappointments.service.impl.TimeSlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableConfigurationProperties
class TimeSlotServiceImplTest {

    @Autowired
    TimeSlotServiceImpl timeSlotService;
    @BeforeEach
    void setUp() {
    }

    @Test
    void fetchTimeSlots() {

//        System.out.println(timeSlotService.fetchTimeSlots("5"));
    }

    @Test
    void fetchAppointmentTypes() {
//        System.out.println(timeSlotService.fetchAppointmentTypes("5"));
    }

    @Test
    void makeAppointment() throws JsonProcessingException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String req ="{\"appointmentType\":\"7dd9ac8e-c436-11e4-a470-82b0ea87e2d8\",\"status\":\"SCHEDULED\",\"timeSlot\":\"e1587701-74fc-41e5-927d-214711bffc21\",\"reason\":\"ioou\",\"patient\":\"90f7f0b4-06a8-4a97-9678-e7a977f4b518\"}";
//        var reqObj = objectMapper.readValue(req, AppointmentReq.class);
//        System.out.println(timeSlotService.makeAppointment(reqObj));
    }

    @Test
    void fetchAppointments() {
//        Map<String, String> params = new HashMap<>();
//        params.put("limit", "5");
//        params.put("patient", "90f7f0b4-06a8-4a97-9678-e7a977f4b518");
//        params.put("fromDate", "2022-10-28");
//        System.out.println(timeSlotService.fetchAppointments(params));
    }
}