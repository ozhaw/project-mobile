package org.nure.julia.database.repository;

import androidx.room.Dao;
import androidx.room.Query;

import org.nure.julia.database.entity.Device;

import java.util.List;

@Dao
public interface DeviceRepository extends BaseDao<Device> {

    @Query("SELECT * FROM device")
    List<Device> getAll();

    @Query("SELECT * FROM device WHERE id = :id")
    Device getById(long id);

    @Query("SELECT * FROM device WHERE deviceId = :deviceId")
    Device getByDeviceId(String deviceId);

}
