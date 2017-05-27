package com.example.setup.gps_tracking;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Setup on 10/05/2017.
 */

public class httpPostRequestTask extends AsyncTask<String, Integer, Long>
{
    public String contacts;
    private ContactsActivity ca;

    public httpPostRequestTask(ContactsActivity activity)
    {
        ca = activity;
        contacts = ca.contacts;
    }

    @Override
    protected Long doInBackground(String... urls)
    {
        try
        {
            URL url = new URL(urls[0]);

            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            uc.setRequestMethod("POST");

            DataOutputStream localDataOutputStream = new DataOutputStream(uc.getOutputStream());
            localDataOutputStream.writeBytes(contacts);
            localDataOutputStream.flush();
            localDataOutputStream.close();

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
                contacts = sb.toString();
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
        ca.contacts = contacts;
    }
}
