package com.intellisoft.botswanaemrnotifications.controller;

import com.intellisoft.botswanaemrnotifications.*;
import com.intellisoft.botswanaemrnotifications.service.NotificationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;

@RequestMapping(value = "/notificatication/service/notification/")
@RestController
public class MicroserviceController {

    FormatterClass formatterClass = new FormatterClass();

    @Autowired
    private NetworkCall networkCall = new NetworkCall();

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    public TemplateEngine templateEngine;

    private final NotificationServiceImpl notificationService;

    public MicroserviceController(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping(value = "create")
    public ResponseEntity<?> createService(@RequestBody DbNotification dbNotification){

        Results results = notificationService.createNotification(dbNotification);
        return formatterClass.getResponse(results);
    }
    @PostMapping(value = "send-email")
    public ResponseEntity<?> sendEmail(@RequestBody DbNotification dbNotification){

        networkCall.sendEmail(javaMailSender,dbNotification, templateEngine);
        Results results = new Results(200, new DbResults("Email will be processed."));
        return formatterClass.getResponse(results);
    }

    //Get my notifications
    @GetMapping(value = "user-notification")
    public ResponseEntity<?> getUserNotifications(@Param("userId") String userId){

        Results results = notificationService.getMyNotification(
                userId,false,1, 100);
        return formatterClass.getResponse(results);
    }

}
