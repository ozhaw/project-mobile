package org.nure.julia.database;

import android.app.Application;

import androidx.room.Room;

import org.nure.julia.ApplicationInstance;

public final class PersistenceContext {
    private AppDatabase connection;

    public static final PersistenceContext INSTANCE = new PersistenceContext();

    private PersistenceContext() {
        connection = Room.databaseBuilder(ApplicationInstance.getContext(),
                AppDatabase.class, "database").allowMainThreadQueries().build();
    }

    public AppDatabase getConnection() {
        return connection;
    }
}
