//for AWARE v4
//L0umfeSKYRj3

package com.aware.plugin.app2016;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

//add my library here

public class Plugin extends Aware_Plugin implements SensorEventListener {

    public static final String ACTION_AWARE_PLUGIN_APP2016 = "ACTION_AWARE_PLUGIN_ACP_APP2016";

    public static final String EXTRA_DATA = "data";

    //context
    private static ContextProducer sContext;


    private SensorManager mSensorManager;
    private Sensor mSensor_Step_Counter;

    private static int step = 0;

    @Override
    public void onCreate() {
        super.onCreate();


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensor_Step_Counter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mSensorManager.registerListener(this, mSensor_Step_Counter, SensorManager.SENSOR_DELAY_NORMAL);

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.
        /*
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_INSTALLATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NOTIFICATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_CRASHES, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_GPS, 180);
*/
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


        if (Aware.getSetting(this, "study_id").length() == 0) {
            Aware.joinStudy(this, "https://api.awareframework.com/index.php/webservice/index/691/L0umfeSKYRj3");
        }

        Log.d("app2016", "fuc off");
    }


    private static ApplicationListener applicationListener = new ApplicationListener();

    public static class ApplicationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("app2016","100");

            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS)) {
                Log.d("app2016","101");

                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Notifications.CONTENT_URI, null, null, null, Applications_Provider.Applications_Notifications.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    String application_notification = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Notifications.PACKAGE_NAME));
                    Log.d("app2016", application_notification);
                }
                if (cursor != null && !cursor.isClosed()) cursor.close();
            }

            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_CRASHES)) {

                Log.d("app2016","113");

                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Crashes.CONTENT_URI, null, null, null, Applications_Provider.Applications_Crashes.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    String application_notification = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Crashes.PACKAGE_NAME));
                    Log.d("app2016", application_notification);
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

        if(applicationListener != null) { unregisterReceiver(applicationListener); }

        /*Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_INSTALLATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NOTIFICATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_CRASHES, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, false);
        */

        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.acpunlock");
    }

    public static void BroadContext(Context context) {
        //Broadcast your context here
        ContentValues data = new ContentValues();
        data.put(Unlock_Monitor_Data.TIMESTAMP, System.currentTimeMillis());
        data.put(Unlock_Monitor_Data.DEVICE_ID, Aware.getSetting(context.getApplicationContext(), Aware_Preferences.DEVICE_ID));
        data.put(Unlock_Monitor_Data.STEP,step);
        //send to AWARE
        Intent context_unlock = new Intent();
        context_unlock.setAction(ACTION_AWARE_PLUGIN_APP2016);
        context_unlock.putExtra(EXTRA_DATA,data);

        context.sendBroadcast(context_unlock);

        Log.d("UNLOCK", "113 broadcast");

        context.getContentResolver().insert(Unlock_Monitor_Data.CONTENT_URI, data);
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    //step sensor
    //if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
    //return Math.round(event.values[0]);
    //}
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            step = Math.round(event.values[0]);
            Log.d("app2016","step = "+ step);
            BroadContext(getApplicationContext());
        }


    }
}
