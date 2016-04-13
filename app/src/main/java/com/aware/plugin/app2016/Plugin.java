package com.aware.plugin.app2016;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Locations;
import com.aware.Screen;
import com.aware.utils.Aware_Plugin;
import com.aware.providers.Applications_Provider;
import com.aware.plugin.app2016.Provider.Unlock_Monitor_Data;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_APP2016 = "ACTION_AWARE_PLUGIN_ACP_APP2016";

    public static final String EXTRA_DATA = "data";

    //context
    private static ContextProducer sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_INSTALLATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NOTIFICATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_CRASHES, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_GPS, 180);

        IntentFilter application_filter = new IntentFilter();
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_CRASHES);
        registerReceiver(applicationListener, application_filter);


        //Any active plugin/sensor shares its overall context using broadcasts
        sContext = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
                ContentValues data = new ContentValues();
                data.put(Unlock_Monitor_Data.TIMESTAMP, System.currentTimeMillis());
                data.put(Unlock_Monitor_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));

                //send to AWARE
                Intent context_unlock = new Intent();
                context_unlock.setAction(ACTION_AWARE_PLUGIN_APP2016);
                context_unlock.putExtra(EXTRA_DATA,data);
                sendBroadcast(context_unlock);

                getContentResolver().insert(Unlock_Monitor_Data.CONTENT_URI, data);
            }
        };
        CONTEXT_PRODUCER = sContext;

        //Add permissions you need (Support for Android M) e.g.,
        //REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        //table 1, 2, 3
        CONTEXT_URIS = new Uri[]{ Provider.Unlock_Monitor_Data.CONTENT_URI};

        /*
        if (Aware.getSetting(this, "study_id").length() == 0) {
            Intent joinStudy = new Intent(this, Aware_Preferences.StudyConfig.class);
            joinStudy.putExtra(Aware_Preferences.StudyConfig.EXTRA_JOIN_STUDY, "https://api.awareframework.com/index.php/webservice/index/634/0FOT21HRz8IZ");
            startService(joinStudy);
        }
        */

        Log.d(TAG, "fuck off");
    }


    private static ApplicationListener applicationListener = new ApplicationListener();

    public static class ApplicationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS)) {
                Log.d("Session notification","101");

                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Notifications.CONTENT_URI, null, null, null, Applications_Provider.Applications_Notifications.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    String application_notification = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Notifications.PACKAGE_NAME));
                    Log.d("Session notification", application_notification);
                }
                if (cursor != null && !cursor.isClosed()) cursor.close();
            }

            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_CRASHES)) {

                Log.d("Session notification","113");

                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Notifications.CONTENT_URI, null, null, null, Applications_Provider.Applications_Crashes.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    String application_notification = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Crashes.PACKAGE_NAME));
                    Log.d("Session crash", application_notification);
                }
                if (cursor != null && !cursor.isClosed()) cursor.close();
            }
        }
    }
    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //TODO

        if(applicationListener != null) { unregisterReceiver(applicationListener); }
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_INSTALLATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NOTIFICATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_CRASHES, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, false);
        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.acpunlock");
    }
}
