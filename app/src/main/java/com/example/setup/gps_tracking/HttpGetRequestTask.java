package com.example.setup.gps_tracking;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpGetRequestTask extends AsyncTask<String, Integer, Boolean>
{
    public String data = "[]";
    private httpGetRequestInterface activity;

    public HttpGetRequestTask(httpGetRequestInterface from_activity)
    {
        activity = from_activity;
    }

    @Override
    protected Boolean doInBackground(String... urls)
    {
        InputStream stream = null;
        HttpURLConnection uc = null;
        try
        {
            URL url = new URL(urls[0]);

            uc = (HttpURLConnection)url.openConnection();
            uc.setConnectTimeout(15000);
            uc.setReadTimeout(15000);

            int responseCode = uc.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
            {
                throw new IOException("0");
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
        catch (java.net.SocketTimeoutException e)
        {
            data = "0";
        }
        catch (MalformedURLException e)
        {
            data = "0";
        }
        catch (IOException e)
        {
            data = "0";
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
                    data = "0";
                }
            }
            if (uc != null)
            {
                uc.disconnect();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean unused)
    {
        super.onPostExecute(unused);
        this.cancel(true);
        activity.RequestResult(data);
    }
}
