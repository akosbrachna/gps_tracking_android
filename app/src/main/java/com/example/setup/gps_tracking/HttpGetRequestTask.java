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

public class HttpGetRequestTask extends AsyncTask<String, Integer, Long>
{
    public String data;
    private httpGetRequestInterface activity;

    public HttpGetRequestTask(httpGetRequestInterface from_activity)
    {
        activity = from_activity;
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
                data = sb.toString();
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
        activity.RequestResult(data);
    }
}
