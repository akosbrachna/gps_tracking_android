package com.example.setup.gps_tracking;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Setup on 10/05/2017.
 */

public class SendCoordinatesTask extends AsyncTask<String, Integer, Long>
{
    public String display_time;
    @Override
    protected Long doInBackground(String... urls)
    {
        try
        {
            URL url = new URL(urls[0]);

            //String basicAuth = "Basic " + Base64.encodeToString("sctuser:atyclb".getBytes(), Base64.NO_WRAP);;

            URLConnection uc = url.openConnection();
            //uc.setRequestProperty("Authorization", basicAuth);

            InputStream bis = uc.getInputStream();
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int bytesRead = 0;
            while ((bytesRead = bis.read(buffer)) > 0)
            {
                String text = new String(buffer, 0, bytesRead);
                sb.append(text);
            }
            bis.close();

            int result=0;
            try
            {
                result = Integer.parseInt(sb.toString());
            }
            catch(NumberFormatException nfe) {}
            String connection_status;
            if (result == 1)
                connection_status = "\nConnection to server is up";
            else
                connection_status = "\nConnection to server is down";

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            display_time = "Last updated: " + formatter.format(calendar.getTime());
            display_time += connection_status;
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Long unused)
    {
        super.onPostExecute(unused);
        this.cancel(true);
        MyService.sendMessageToUI(display_time, MyService.MSG_STRING_MESSAGE);
    }
}

