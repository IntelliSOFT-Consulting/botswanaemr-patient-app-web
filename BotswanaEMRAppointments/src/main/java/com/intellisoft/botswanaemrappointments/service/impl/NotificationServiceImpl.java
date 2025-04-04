package com.intellisoft.botswanaemrappointments.service.impl;

import com.intellisoft.botswanaemrappointments.DbNotification;
import com.intellisoft.botswanaemrappointments.NetworkCall;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceImpl {

    NetworkCall networkCall = new NetworkCall();

    public void createNotification(DbNotification dbNotification, String url){
        networkCall.createNotification("CREATE_NOTIFICATION", dbNotification, url);
    }

}
