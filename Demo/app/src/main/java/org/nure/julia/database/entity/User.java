package org.nure.julia.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = User.NAME)
public class User implements BaseEntity {
    final static String NAME = "user";

    @PrimaryKey
    public long id;
    public String email;
    public String password;

    @Override
    public String getEntityName() {
        return NAME;
    }
}
