package org.nure.julia.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = Device.NAME)
public class Device implements BaseEntity {

    final static String NAME = "device";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String deviceId;
    public String type;

    @Override
    public String getEntityName() {
        return NAME;
    }
}
