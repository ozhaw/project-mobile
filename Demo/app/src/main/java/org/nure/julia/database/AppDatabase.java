package org.nure.julia.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import org.nure.julia.database.entity.Device;
import org.nure.julia.database.entity.Notification;
import org.nure.julia.database.entity.User;
import org.nure.julia.database.repository.DeviceRepository;
import org.nure.julia.database.repository.NotificationRepository;
import org.nure.julia.database.repository.UserRepository;

@Database(entities = {Device.class, User.class, Notification.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceRepository deviceRepository();

    public abstract UserRepository userRepository();

    public abstract NotificationRepository notificationRepository();
}