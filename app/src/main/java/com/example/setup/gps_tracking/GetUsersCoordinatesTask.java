package com.example.setup.gps_tracking;

import android.os.AsyncTask;
import android.widget.Toast;

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

public class GetUsersCoordinatesTask extends AsyncTask<String, Integer, Long>
{
    public String users;
    private MapsActivity ma;

    public GetUsersCoordinatesTask(MapsActivity map_activity)
    {
        ma = map_activity;
    }

    @Override
    protected Long doInBackground(String... urls)
    {
        try
        {
            URL url = new URL(urls[0]);

            URLConnection uc = url.openConnection();

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
            try
            {
                users = sb.toString();
            }
            catch(NumberFormatException nfe) {}
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
//        ma.editor.putString("users", users);
//        ma.editor.commit();
        ma.show_users_on_map(users);
    }
}
