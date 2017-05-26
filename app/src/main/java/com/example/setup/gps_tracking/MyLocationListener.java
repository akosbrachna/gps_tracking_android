package com.example.setup.gps_tracking;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;

public class MyLocationListener implements LocationListener
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
            String url_string = server_address + "/android/data_exchange/set_my_location/"
                              + email+"/"+password+"/"+lat+"/"+lon;

            new SendCoordinatesTask().execute(url_string);
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
}