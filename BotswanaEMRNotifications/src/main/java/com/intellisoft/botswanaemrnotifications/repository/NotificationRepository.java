package com.intellisoft.botswanaemrnotifications.repository;

import com.intellisoft.botswanaemrnotifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findAllByUserId(String userId);
    Page<Notification> findAllByUserIdAndIsRead(String userId, boolean isRead, Pageable pageable);


//    List<Notification> findByUserIdAndEa(String userId);
//    List<Notification> findByUserIdAndReadFalse(String userId);

}
