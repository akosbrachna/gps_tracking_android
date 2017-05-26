package com.example.setup.gps_tracking;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Setup on 10/05/2017.
 */
public class CheckConnectionTask extends AsyncTask<String, Integer, Void>
{
    MainActivity ma;
    public CheckConnectionTask(MainActivity activity)
    {
        ma = activity;
    }
    @Override
    protected Void doInBackground(String... urls)
    {
        try {
            URL url = new URL(urls[0]);

            //String basicAuth = "Basic " + Base64.encodeToString("sctuser:atyclb".getBytes(), Base64.NO_WRAP);;

            URLConnection uc = url.openConnection();
            //uc.setRequestProperty("Authorization", basicAuth);

            InputStream bis = uc.getInputStream();
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int bytesRead;
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
            if (result == 1)
            {
                MyService.isServerConnected = true;
                ma.status_message = "Connection successful.\n";
                if (MyService.isRunning() == false)
                    ma.status_message += "You can start the service.";
            }
            else
            {
                MyService.isServerConnected = false;
                ma.status_message = "Connection failed.\n" +
                        "Please check email and password.";
            }

        } catch (MalformedURLException e)
        {
            ma.status_message = "Connection failed.\nThis should never happen.";
            MyService.isServerConnected = false;
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            ma.status_message = "Connection failure.\nEither server is down or no network.";
            MyService.isServerConnected = false;
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused)
    {
        super.onPostExecute(unused);
        ma.status.setText(ma.status_message);
        ma.editor.putBoolean("connected", MyService.isServerConnected);
        ma.editor.putBoolean("boot_enabled", MyService.isServerConnected);
        ma.boot_enabled.setChecked(MyService.isServerConnected);
        ma.editor.commit();
    }
}
