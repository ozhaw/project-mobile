package org.nure.julia;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.pusher.pushnotifications.fcm.MessagingService;

import org.nure.julia.database.PersistenceContext;
import org.nure.julia.database.entity.Notification;

public class NotificationsMessagingService extends MessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        remoteMessage.getData().forEach((key, value) -> {
            if (!value.contains("instanceId")) {
                Notification notification = new Gson().fromJson(value, Notification.class);
                PersistenceContext.INSTANCE.getConnection()
                        .notificationRepository()
                        .insert(notification);
            }
        });
    }
}