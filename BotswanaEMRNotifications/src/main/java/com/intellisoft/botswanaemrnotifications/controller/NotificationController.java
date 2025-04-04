package com.intellisoft.botswanaemrnotifications.controller;

import com.intellisoft.botswanaemrnotifications.DbNotification;
import com.intellisoft.botswanaemrnotifications.FormatterClass;
import com.intellisoft.botswanaemrnotifications.Results;
import com.intellisoft.botswanaemrnotifications.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(description = "System Notifications.", name = "Notifications module")
@RequestMapping(value = "/notification/api/v1")
@RestController
public class NotificationController {

    FormatterClass formatterClass = new FormatterClass();

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    //Add Notification
    @Operation(summary = "Create notifications", description = "Create a notification to the system.")
    @PostMapping(path = "/create")
    public ResponseEntity<?> createNotification(@RequestBody DbNotification dbNotification){
        Results results = notificationService.createNotification(dbNotification);
        return formatterClass.getResponse(results);

    }

    //Get Notifications
    @Operation(summary = "Get notifications", description = "View notifications.")
    @GetMapping(value = "/user-notification")
    public ResponseEntity<?> getUserNotifications(
            @Param("isRead") boolean isRead,
            @Param("pageSize") int pageSize,
            @Param("pageNumber") int pageNumber
    ){

        Optional<String> userId = formatterClass.getCurrentUserLogin();
        Results results;
        if (userId.isPresent()){
            results = notificationService.getMyNotification(userId.get(), isRead, pageNumber, pageSize);
        }else {
            results = new Results(400, "We could not find the user");
        }
        return formatterClass.getResponse(results);


    }


    //Get Notifications Details
    @Operation(summary = "Get notification details", description = "View notification details.")
    @GetMapping(value = "/notification-details")
    public ResponseEntity<?> getUserNotificationDetails(
            @Param("notificationId") String notificationId){

        Results results = notificationService.getNotificationDetails(notificationId);
        return formatterClass.getResponse(results);
    }

    //Mark Notification as Read
    @Operation(summary = "Mark notification as read", description = "Mark notification as read.")
    @PutMapping(value = "/notification-status")
    public ResponseEntity<?> updateNotification(
            @Param("notificationId") String notificationId,
            @Param("status") String status
            ){

        Results results = notificationService.updateNotification(notificationId, status);
        return formatterClass.getResponse(results);
    }


    



}
