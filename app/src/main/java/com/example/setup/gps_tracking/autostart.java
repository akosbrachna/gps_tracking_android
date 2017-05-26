package com.example.setup.gps_tracking;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Created by Setup on 27/02/2016.
 */
public class autostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1)
    {
        SharedPreferences sp = context.getSharedPreferences("settings", context.MODE_PRIVATE);
        if (sp.getBoolean("boot_enabled", false) == false) {
            return;
        }
        Intent intent = new Intent(context, MyService.class);
        context.startService(intent);
    }
}