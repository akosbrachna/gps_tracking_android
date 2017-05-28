package com.example.setup.gps_tracking;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MyLocationListener implements LocationListener, httpGetRequestInterface
{
    public String email, password, server_address, sms_number;
    public boolean sms_enabled, http_enabled;

    @Override
    public void onLocationChanged(Location loc)
    {
        Double lat = loc.getLatitude();
        Double lon = loc.getLongitude();

        if (sms_enabled)
        {
            String uri = "http://maps.google.com/?q=" + lat + "," + lon;
            StringBuffer smsBody = new StringBuffer();
            smsBody.append(Uri.parse(uri));
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(sms_number, null, smsBody.toString(), null, null);
                MyService.sendMessageToUI("SMS has been sent.", MyService.MSG_STRING_MESSAGE);
            } catch (Exception e) {
                MyService.sendMessageToUI("SMS failed.", MyService.MSG_STRING_MESSAGE);
            }
        }
        if (!server_address.isEmpty() && !email.isEmpty() && http_enabled)
        {
            String url = server_address + "/android/data_exchange/set_my_location/"+ email+"/"+password+"/"+lat+"/"+lon;
            new HttpGetRequestTask(MyLocationListener.this).execute(url);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        MyService.isGpsEnabled = true;
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        MyService.isGpsEnabled = false;
    }

    @Override
    public void RequestResult(String data)
    {
        String display_time = "\nConnection to server is down";
        int result=0;
        try
        {
            result = Integer.parseInt(data);
            if (result == 1)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                display_time = "Last updated: " + formatter.format(calendar.getTime())+"\nConnection to server is up";
            }
        }
        catch(NumberFormatException nfe) {
        }
        MyService.sendMessageToUI(display_time, MyService.MSG_STRING_MESSAGE);
    }
}