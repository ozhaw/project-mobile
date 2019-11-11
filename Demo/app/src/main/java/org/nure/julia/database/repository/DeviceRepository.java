package org.nure.julia.database.repository;

import androidx.room.Dao;
import androidx.room.Query;

import com.google.firebase.perf.metrics.AddTrace;

import org.nure.julia.database.entity.Device;

import java.util.List;

@Dao
public interface DeviceRepository extends BaseDao<Device> {

    @Query("SELECT * FROM device")
    @AddTrace(name = "DeviceTable_GetAll")
    List<Device> getAll();

    @Query("SELECT * FROM device WHERE id = :id")
    @AddTrace(name = "DeviceTable_GetById")
    Device getById(long id);

    @Query("SELECT * FROM device WHERE deviceId = :deviceId")
    @AddTrace(name = "DeviceTable_GetDeviceById")
    Device getByDeviceId(String deviceId);

}
