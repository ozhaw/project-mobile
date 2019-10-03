package org.nure.julia.database.repository;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.TypeConverters;
import androidx.room.Update;

import org.nure.julia.database.converters.DateConverter;
import org.nure.julia.database.entity.BaseEntity;

@TypeConverters(DateConverter.class)
public interface BaseDao<T extends BaseEntity> {

    @Insert
    void insert(T t);

    @Update
    void update(T t);

    @Delete
    void delete(T t);

}
