package org.nure.julia.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = Notification.NAME)
public class Notification implements BaseEntity {
    final static String NAME = "notification";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String time;
    public String severity;
    public String advice;
    public String type;

    @Override
    public String getEntityName() {
        return NAME;
    }
}
