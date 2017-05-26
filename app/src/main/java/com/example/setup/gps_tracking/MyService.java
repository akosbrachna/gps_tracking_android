package com.example.setup.gps_tracking;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by Setup on 24/02/2016.
 */
public class MyService extends Service {
    public SharedPreferences sp;
    public static List<Messenger> mClients = new ArrayList<Messenger>();
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_GPS_DISABLED = 3;
    public static final int MSG_STRING_MESSAGE = 4;
    public static final String LOGTAG = "MyService";
    private static boolean isRunning = false;
    public static boolean isGpsEnabled = false;
    public static boolean isServerConnected;
    private MyLocationListener mlocListener;
    private LocationManager mlocManager;
    private final Messenger mMessenger = new Messenger(new MyMessageHandler(this));
    private static final int MY_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private Notification notification;
    private Intent stop_self;
    private PendingIntent pIntent;
    private NotificationCompat.Builder builder;
    private String notification_text;
    private String UI_message = "";
    private int frequency = 5;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("settings", getApplicationContext().MODE_PRIVATE);
        frequency = sp.getInt("frequency", 5);
        MyService.isServerConnected = sp.getBoolean("connected", false);
        mlocListener = new MyLocationListener();
        mlocListener.email = sp.getString("email", "");
        mlocListener.password = sp.getString("password", "");
        mlocListener.server_address = sp.getString("server_address", "");
        mlocListener.sms_number = sp.getString("sms_number", "");
        mlocListener.sms_enabled = sp.getBoolean("sms_enabled", false);
        mlocListener.http_enabled = sp.getBoolean("http_enabled", false);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        stop_self = new Intent(this, MyService.class);
        stop_self.setAction("Start");
        pIntent = PendingIntent.getService(this, 0, stop_self, PendingIntent.FLAG_CANCEL_CURRENT);
        builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pIntent);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("Stop".equals(intent.getAction())) {
            deactivate_GPS();
            stop_self.setAction("Start");
            pIntent = PendingIntent.getService(this, 0, stop_self, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setSmallIcon(R.mipmap.stop_service);
            if (isGpsEnabled == false)
                notification_text = "GPS disabled.\nTap to resume the service.";
            else notification_text = "Tap to resume the service";
            builder.setContentTitle("GPS Tracking halted");
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(notification_text + "\n" + UI_message));
            builder.setContentText(notification_text + "\n" + UI_message);
            builder.setContentIntent(pIntent);
            notification = builder.build();
            notificationManager.notify(MY_NOTIFICATION_ID, notification);
            sendMessageToUI("Service halted", MSG_STRING_MESSAGE);
        } else {
            activate_GPS();
            stop_self.setAction("Stop");
            pIntent = PendingIntent.getService(this, 0, stop_self, PendingIntent.FLAG_CANCEL_CURRENT);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            if (isGpsEnabled == false)
                notification_text = "GPS is disabled.\nTap to pause the service.";
            else notification_text = "Tap to pause the service";
            builder.setContentTitle("GPS Tracking running");
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(notification_text + "\n" + UI_message));
            builder.setContentText(notification_text + "\n" + UI_message);
            builder.setContentIntent(pIntent);
            notification = builder.build();
            startForeground(MY_NOTIFICATION_ID, notification);
            isRunning = true;
        }
        return START_STICKY;
    }

    private void activate_GPS() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, frequency * 60 * 1000, 50, mlocListener);
        isGpsEnabled = true;
        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            isGpsEnabled = false;
        }
        sendMessageToUI("Service is running", MSG_STRING_MESSAGE);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void deactivate_GPS() {
        if (mlocManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mlocManager.removeUpdates(mlocListener);
            mlocManager = null;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        deactivate_GPS();
        sendMessageToUI("Service stopped", MSG_UNREGISTER_CLIENT);
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    /**
     * Send the data to all clients.
     */
    public static void sendMessageToUI(String message, int message_id)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while (messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                // Send data as a String
                Bundle bundle = new Bundle();
                bundle.putString("service_message", message);
                Message msg = Message.obtain(null, message_id);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                // The client is dead. Remove it from the list.
                mClients.remove(messenger);
            }
        }
    }
}
