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

import javax.net.ssl.HttpsURLConnection;

public class HttpGetRequestTask extends AsyncTask<String, Integer, Long>
{
    public String data = "[]";
    private httpGetRequestInterface activity;

    public HttpGetRequestTask(httpGetRequestInterface from_activity)
    {
        activity = from_activity;
    }

    @Override
    protected Long doInBackground(String... urls)
    {
        InputStream stream = null;
        HttpsURLConnection uc = null;
        try
        {
            URL url = new URL(urls[0]);

            uc = (HttpsURLConnection)url.openConnection();
            uc.setConnectTimeout(10000);
            uc.setDoInput(true);
            uc.setRequestMethod("GET");
            uc.connect();

            int responseCode = uc.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK)
            {
                throw new IOException("HTTP error code: " + responseCode);
            }

            stream = uc.getInputStream();
            byte[] buffer = new byte[1024];
            StringBuilder sb = new StringBuilder();
            int bytesRead = 0;
            if (stream != null)
            {
                while ((bytesRead = stream.read(buffer)) > 0)
                {
                    String text = new String(buffer, 0, bytesRead);
                    sb.append(text);
                }
                data = sb.toString();
            }
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (uc != null)
            {
                uc.disconnect();
            }
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
