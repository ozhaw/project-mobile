package org.nure.julia;

import android.app.Application;
import android.content.Context;

public class ApplicationInstance extends Application {

    private static ApplicationInstance sApplication;

    public static ApplicationInstance getApplication() {
        return sApplication;
    }

    public static Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
    }
}
