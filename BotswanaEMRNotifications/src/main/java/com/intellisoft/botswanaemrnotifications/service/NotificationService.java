package com.intellisoft.botswanaemrnotifications.service;

import com.intellisoft.botswanaemrnotifications.DbNotification;
import com.intellisoft.botswanaemrnotifications.Results;
import com.intellisoft.botswanaemrnotifications.entity.Notification;

public interface NotificationService {

    Results createNotification(DbNotification notification);
    Results getMyNotification(String userId, boolean isRead, int page, int size);
    Results getNotificationDetails(String notificationId);
    Results updateNotification(String notificationId,  String status);

}
