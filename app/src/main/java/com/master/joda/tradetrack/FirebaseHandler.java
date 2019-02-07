package com.master.joda.tradetrack;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHandler extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        Log.d(FirebaseHandler.class.getSimpleName(), "Persistence enabled");
    }
}
