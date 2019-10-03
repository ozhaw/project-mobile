package org.nure.julia.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import org.nure.julia.database.entity.Device;
import org.nure.julia.database.repository.DeviceRepository;

@Database(entities = {Device.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceRepository deviceRepository();
}