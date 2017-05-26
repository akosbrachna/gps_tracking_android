package com.example.setup.gps_tracking;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Setup on 10/05/2017.
 */
public class MyMessageHandler  extends Handler
{ // Handler of incoming messages from clients.

    private MyService s;
    public MyMessageHandler(MyService service)
    {
        s = service;
    }

    @Override
    public void handleMessage(Message msg)
    {
        Log.d(MyService.LOGTAG, "handleMessage: " + msg.what);
        switch (msg.what) {
            case MyService.MSG_REGISTER_CLIENT:
                s.mClients.add(msg.replyTo);
                break;
            case MyService.MSG_UNREGISTER_CLIENT:
                s.mClients.remove(msg.replyTo);
                break;
            default:
                super.handleMessage(msg);
        }
    }
}