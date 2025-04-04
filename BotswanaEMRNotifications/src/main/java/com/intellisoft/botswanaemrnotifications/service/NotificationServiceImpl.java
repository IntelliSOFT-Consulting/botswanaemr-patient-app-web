package com.intellisoft.botswanaemrnotifications.service;

import com.intellisoft.botswanaemrnotifications.*;
import com.intellisoft.botswanaemrnotifications.entity.Notification;
import com.intellisoft.botswanaemrnotifications.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService{

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NetworkCall networkCall = new NetworkCall();

    @Value("${notification}")
    private String notificationUrl;

    @Override
    public Results createNotification(DbNotification notification) {

        Notification notificationSaved = saveNotification(notification);
        return new Results(201, notificationSaved);
    }

    @Override
    public Results getMyNotification(
            String keycloakId,
            boolean isRead,
            int page,
            int size
            ) {

        String nextUrl = null;
        String previousUrl = null;

        String commonUrl = "/api/v1/user-notification?";

        List<Notification> notificationList = getAllUserNotifications(
                keycloakId,
                page,
                size,
                isRead);

        //Check if page is more than 1
        if(page > 1){
            previousUrl = notificationUrl + commonUrl + "isRead="+ isRead +"&pageSize="+size + "&pageNumber=" + (page-1);
        }else{
            nextUrl = notificationUrl + commonUrl + "isRead="+ isRead +"&pageSize="+size + "&pageNumber=" + (page + 1);
        }

        DbDynamicResponse dbDynamicResponse = new DbDynamicResponse(
                size,
                nextUrl,
                previousUrl,
                notificationList);


        return new Results(200, dbDynamicResponse);

    }

    @Override
    public Results getNotificationDetails(String notificationId) {

        Notification notification = getNotificationInfo(notificationId);
        if (notification != null){
            return new Results(200, notification);
        }else {
            return new Results(204, "The notification cannot be found");
        }
    }

    @Override
    public Results updateNotification(String notificationId, String status) {

        Notification notification = getNotificationInfo(notificationId);

        if (notification != null){

            Notification notificationUpdate = updateNotificationStatus(notification, status);

            if (notificationUpdate != null){
                return new Results(200, new DbResults("The notifications has been updated successfully."));
            }else {
                return new Results(400, new DbResults("The notification cannot be found"));
            }

        }else {
            return new Results(400, new DbResults("The notification cannot be found"));
        }



    }

    private Notification saveNotification(DbNotification dbNotification){

        Notification notification = new Notification(
                dbNotification.getTitle(),
                dbNotification.getMessage(),
                dbNotification.getUserId(),
                dbNotification.getNotificationType(),
                false,
                dbNotification.getProcess()
        );


        return notificationRepository.save(notification);
    }

    private Notification updateNotificationStatus(Notification notification, String status){

        String id = notification.getId();

        if (status.equalsIgnoreCase("read")){
            notification.setRead(true);
        }
        if (status.equalsIgnoreCase("unread")){
            notification.setRead(false);
        }

        return notificationRepository.findById(id)
                .map(notificationOld ->{
                    notificationOld.setRead(notification.isRead());
                    notificationOld.setMessage(notification.getMessage());
                    notificationOld.setNotificationType(notification.getNotificationType());
                    notificationOld.setProcess(notification.getProcess());
                    return notificationRepository.save(notificationOld);
                }).orElse(null);

    }

    private Notification getNotificationInfo(String notificationId){
        Optional<Notification> optionalGroups = notificationRepository.findById(notificationId);
        return optionalGroups.orElse(null);
    }

    private List<Notification> getAllUserNotifications(
            String userId,
            int page,
            int size,
            boolean isRead){

        //Search by filter and sort
        return getPagedUserNotification(page, size, "", "", userId, isRead);
//        return notificationRepository.findAllByUserId(userId);

    }

    private List<Notification> getPagedUserNotification(
            int pageNo,
            int pageSize,
            String sortField,
            String sortDirection,
            String userId,
            boolean isRead) {
        String sortPageField = "";
        String sortPageDirection = "";

        if (sortField.equals("")){sortPageField = "createdAt"; }else {sortPageField = sortField;}
        if (sortDirection.equals("")){sortPageDirection = "DESC"; }else {sortPageDirection = sortField;}

        Sort sort = sortPageDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortPageField).ascending() : Sort.by(sortPageField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        Page<Notification> page = notificationRepository.findAllByUserIdAndIsRead(
                userId,isRead, pageable);

        return page.getContent();
    }
}
