package com.example.setup.gps_tracking;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

public class SettingsActivity extends AppCompatActivity implements ServiceConnection, httpGetRequestInterface
{
    private EditText email_input, password_input, server_address, sms_number;
    public CheckBox boot_enabled, sms_enabled, http_enabled;
    public TextView status;
    private SharedPreferences sp;
    public SharedPreferences.Editor editor;
    private boolean mIsBound = false;
    private ServiceConnection mConnection = this;
    public String status_message = "";
    private Button service_button;
    private NumberPicker numberpicker;
    private Messenger mServiceMessenger = null;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("settings", getApplicationContext().MODE_PRIVATE);
        editor = sp.edit();

        email_input = (EditText) findViewById(R.id.email);
        password_input = (EditText) findViewById(R.id.password);
        server_address = (EditText) findViewById(R.id.edit_server_address);
        sms_number = (EditText) findViewById(R.id.edit_sms_number);
        status = (TextView) findViewById(R.id.status);
        boot_enabled = (CheckBox) findViewById(R.id.boot_enabled);
        sms_enabled = (CheckBox) findViewById(R.id.check_sms_enabled);
        http_enabled = (CheckBox) findViewById(R.id.check_server_enabled);
        numberpicker = (NumberPicker) findViewById(R.id.numberPicker1);
        numberpicker.setMaxValue(10);
        numberpicker.setMinValue(1);
        numberpicker.setValue(sp.getInt("frequency", 5));
        numberpicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                editor.putInt("frequency", newVal);
                editor.commit();
            }
        });

        MyService.isServerConnected = sp.getBoolean("connected", false);
        boot_enabled.setChecked(sp.getBoolean("boot_enabled", false));
        sms_enabled.setChecked(sp.getBoolean("sms_enabled", false));
        http_enabled.setChecked(sp.getBoolean("http_enabled", false));
        email_input.setText(sp.getString("email", ""));
        password_input.setText(sp.getString("password", ""));
        server_address.setText(sp.getString("server_address", ""));
        sms_number.setText(sp.getString("sms_number", ""));

        if (sp.getBoolean("connected", false))
            status.setText(status_message);
        else status.setText("Please test connection before starting service.");

        final Button test_button = (Button) findViewById(R.id.test_button);

        test_button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String email = email_input.getText().toString().trim();
                String password = password_input.getText().toString().trim();
                String server_addr = server_address.getText().toString();
                String phone = sms_number.getText().toString();
                int freq = numberpicker.getValue();

                editor.putString("server_address", server_addr);
                editor.putString("sms_number", phone);
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putInt("frequency", freq);
                editor.commit();

                if (!server_addr.isEmpty())
                {
                    status.setText("Sending login request to server...");

                    String url = server_address.getText().toString()
                            + "/android/data_exchange/check_my_connection/" + email + "/" + password;
                    new HttpGetRequestTask(SettingsActivity.this).execute(url);
                }
                if (!phone.isEmpty() && sms_enabled.isChecked())
                {
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone, null, "http://maps.google.com/?q=0.0,0.0", null, null);
                        MyService.isServerConnected = true;
                        status.setText("SMS has been sent successfully");
                    } catch (Exception e) {
                        status.setText("SMS sending failed.");
                        MyService.isServerConnected = false;
                        e.printStackTrace();
                    }
                    editor.putBoolean("connected", MyService.isServerConnected);
                    editor.commit();
                }
            }
        });

        service_button = (Button) findViewById(R.id.service_button);

        service_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (MyService.isRunning()) {
                    doUnbindService();
                } else {
                    if (MyService.isServerConnected == false) {
                        status.setText("Please test connection before starting service.");
                        return;
                    }
                    startService(new Intent(SettingsActivity.this, MyService.class));
                    doBindService();
                }
            }
        });

        boot_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("boot_enabled", isChecked);
                editor.commit();
            }
        });
        sms_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("sms_enabled", isChecked);
                editor.commit();
            }
        });
        http_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("http_enabled", isChecked);
                editor.commit();
            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        if (MyService.isRunning())
            doBindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent myIntent;
        switch (item.getItemId())
        {
            case R.id.action_map:
                if (sp.getString("server_address", "").isEmpty())
                {
                    Toast.makeText(SettingsActivity.this,
                                "Map won't start without server address, email, password \n" +
                                "because the locations of the followed members come from the server.",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
                myIntent = new Intent(this, MapsActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_contacts:
                if (sp.getString("server_address", "").isEmpty())
                {
                    Toast.makeText(SettingsActivity.this,
                            "Contacts cannot be checked without server address, email, password \n" +
                                    "because contact details come from the server.",
                            Toast.LENGTH_LONG).show();
                    return true;
                }
                myIntent = new Intent(this, ContactsActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.example.setup.gps_tracking/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.example.setup.gps_tracking/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void RequestResult(String data)
    {
        int result=0;
        try
        {
            result = Integer.parseInt(data);
        }
        catch(NumberFormatException nfe) {}
        if (result == 1)
        {
            MyService.isServerConnected = true;
            status_message = "Connection successful.\n";
            if (MyService.isRunning() == false)
                status_message += "You can start the service.";
        }
        else
        {
            MyService.isServerConnected = false;
            status_message = "Connection failed.\n" +
                    "Please check email and password.";
        }
        status.setText(status_message);
        editor.putBoolean("connected", MyService.isServerConnected);
        editor.putBoolean("boot_enabled", MyService.isServerConnected);
        boot_enabled.setChecked(MyService.isServerConnected);
        editor.commit();
    }

    /**
     * Handle incoming messages from MyService
     */
    private class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            status_message = msg.getData().getString("service_message");
            status.setText(status_message);
            switch (msg.what) {
                case MyService.MSG_GPS_DISABLED:
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    break;
                case MyService.MSG_UNREGISTER_CLIENT:
                    mIsBound = false;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Send data to the service
     */
    private void sendMessageToService(Bundle bundle) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    // Send data as a String
                    Message msg = Message.obtain(null, MyService.MSG_STRING_MESSAGE);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }
    }

    /**
     * Bind this Activity to MyService
     */
    private void doBindService() {
        if (mIsBound) return;
        bindService(new Intent(this, MyService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        status.setText("Service is running.");
        service_button = (Button) findViewById(R.id.service_button);
        service_button.setText("Stop service");
    }

    /**
     * Un-bind this Activity from MyService
     */
    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mServiceMessenger != null) {
                try {
                    Message msg = Message.obtain(null, MyService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            stopService(new Intent(SettingsActivity.this, MyService.class));
            mIsBound = false;
            status.setText(status_message + "\nService stopped.");
            service_button = (Button) findViewById(R.id.service_button);
            service_button.setText("Start service");
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        try {
            Message msg = Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
        mServiceMessenger = null;
    }
}
