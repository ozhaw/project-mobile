package org.nure.julia.database.repository;

import androidx.room.Dao;
import androidx.room.Query;

import com.google.firebase.perf.metrics.AddTrace;

import org.nure.julia.database.entity.Device;
import org.nure.julia.database.entity.User;

import java.util.List;

@Dao
public interface UserRepository extends BaseDao<User> {

    @Query("SELECT * FROM user")
    @AddTrace(name = "UserTable_GetAll")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE id = :id")
    @AddTrace(name = "UserTable_GetById")
    User getById(long id);

    @Query("DELETE FROM user")
    @AddTrace(name = "UserTable_DeleteAll")
    void deleteAll();

}
