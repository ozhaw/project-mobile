package org.nure.julia.database.repository;

import androidx.room.Dao;
import androidx.room.Query;

import com.google.firebase.perf.metrics.AddTrace;

import org.nure.julia.database.entity.Device;
import org.nure.julia.database.entity.Notification;

import java.util.List;

@Dao
public interface NotificationRepository extends BaseDao<Notification> {

    @Query("SELECT * FROM notification ORDER BY id DESC LIMIT 50")
    @AddTrace(name = "NotificationTable_GetLast50")
    List<Notification> getLast50();

    @Query("SELECT * FROM notification WHERE id = :id")
    @AddTrace(name = "NotificationTable_GetById")
    Notification getById(long id);

    @Query("SELECT * FROM notification WHERE severity IN ('NODATA', 'DANGER', 'CRITICAL') AND type = 'health' ORDER BY id DESC LIMIT 1")
    @AddTrace(name = "NotificationTable_GetLastCriticalHealthSeverity")
    Notification getLastCriticalHealthSeverity();

    @Query("SELECT * FROM notification WHERE severity IN ('NDCHRG', 'MLFUNC', 'OFFLINE') AND type = 'device' ORDER BY id DESC LIMIT 1")
    @AddTrace(name = "NotificationTable_GetLastCriticalDeviceSeverity")
    Notification getLastCriticalDeviceSeverity();

    @Query("SELECT COUNT(*) FROM notification")
    @AddTrace(name = "NotificationTable_GetTotalNotifications")
    long getTotalNotifications();

}
