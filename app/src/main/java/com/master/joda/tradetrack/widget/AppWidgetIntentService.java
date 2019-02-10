package com.master.joda.tradetrack.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.master.joda.tradetrack.model.Record;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class AppWidgetIntentService extends IntentService {

    private static final String ACTION_APPWIDGET_UPDATE = "com.master.joda.tradetrack.widget.action.APPWIDGET_UPDATE";

    private String mDate;
    private String mProfit;

    public AppWidgetIntentService() {
        super("AppWidgetIntentService");
    }

    /**
     * Starts this service to perform action AppWidget_Update with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidget(Context context) {
        Intent intent = new Intent(context, AppWidgetIntentService.class);
        intent.setAction(ACTION_APPWIDGET_UPDATE);
        context.startService(intent);
    }

    private void connectToFirebase() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            DatabaseReference recordsDatabaseReference = FirebaseDatabase.getInstance()
                    .getReference()
                    .child("records")
                    .child(userId);
            Query query = recordsDatabaseReference.limitToLast(1);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot recordDataSnapshot : dataSnapshot.getChildren()) {
                        Record record = recordDataSnapshot.getValue(Record.class);
                        assert record != null;
                        mDate = record.getDate();
                        mProfit = record.getProfit();
                        handleActionUpdateWidget();
                        Log.i(AppWidgetIntentService.class.getSimpleName(), record.toString());
                        Log.i(AppWidgetIntentService.class.getSimpleName(), "Record created. Date: " + mDate + ". Profit: " + mProfit);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDate = "";
                    mProfit = "";
                }
            });
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_APPWIDGET_UPDATE.equals(action)) {
                connectToFirebase();
            }
        }
    }

    /**
     * Handle action AppWidget_Update in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidget() {

        Log.i(AppWidgetIntentService.class.getSimpleName(), "Widget set method called. Date: " + mDate + ". Profit: " + mProfit);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(this, AppWidget.class));

        if (mDate != null && mProfit != null) {
            AppWidget.updateAllAppWidgets(this, appWidgetManager, ids, mDate, mProfit);
            Log.i(AppWidgetIntentService.class.getSimpleName(), "Widget set. Date: " + mDate + ". Profit: " + mProfit);
        }
    }
}
