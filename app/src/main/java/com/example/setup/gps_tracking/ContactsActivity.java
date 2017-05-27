package com.example.setup.gps_tracking;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity implements httpGetRequestInterface {
    private String json_contacts;
    public String contacts;
    private ArrayList<String> listItems;
    private ArrayAdapter adapter;
    private ListView listView;
    private SharedPreferences sp;
    private String server_address, email, password;
    JSONArray users;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.contacts_toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("settings", getApplicationContext().MODE_PRIVATE);
        server_address = sp.getString("server_address", "");
        email = sp.getString("email", "");
        password = sp.getString("password", "");

        String url = server_address + "/android/data_exchange/get_all_contacts/"+ email+"/"+password+"/";
        new HttpGetRequestTask(ContactsActivity.this).execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent myIntent;
        switch (item.getItemId())
        {
            case R.id.action_contacts_settings:
                myIntent = new Intent(this, MainActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_map:
                new httpPostRequestTask(ContactsActivity.this);
                myIntent = new Intent(this, MapsActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void draw_listview()
    {
        try
        {
            users = new JSONArray(json_contacts);

            if (users.length() == 0)
            {
                return;
            }
            listItems = new ArrayList<String>();
            for (int i = 0; i < users.length(); i++)
            {
                JSONObject c = users.getJSONObject(i);
                String name = c.getString("first_name")+" "+c.getString("last_name");
                listItems.add(name);
            }
            adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, listItems);
            listView = (ListView) findViewById(R.id.contacts_list);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                    try {
                        JSONObject c = ContactsActivity.this.users.getJSONObject(position);
                        String contact_email = c.getString("email");
                        String status = c.getString("status");
                        if (status.toLowerCase().contains("invisible")) {
                            status = "visible";
                            c.put("status", status);
                            ContactsActivity.this.users.put(position, c);
                            listView.getChildAt(position).setBackgroundColor(Color.rgb(125, 75, 236));
                        } else {
                            status = "invisible";
                            c.put("status", status);
                            ContactsActivity.this.users.put(position, c);
                            listView.getChildAt(position).setBackgroundColor(Color.rgb(255, 255, 255));
                        }
                        String url = server_address + "/android/data_exchange/change_contact_settings/"
                                + email + "/" + password + "/" + contact_email + "/" + status;
                        new HttpGetRequestTask(ContactsActivity.this).execute(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < users.length(); i++)
                {
                    try {
                        JSONObject c = users.getJSONObject(i);
                        String status = c.getString("status");
                        if (status.toLowerCase().contains("invisible"))
                        {
                            listView.getChildAt(i).setBackgroundColor(Color.rgb(255, 255, 255));
                        }
                        else{
                            listView.getChildAt(i).setBackgroundColor(Color.rgb(125, 75, 236));
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void RequestResult(String data)
    {
        int result = 0;
        try
        {
            result = Integer.parseInt(data);
            switch(result)
            {
                case 1:
                    Toast.makeText(ContactsActivity.this,
                            "successfully modified on server.", Toast.LENGTH_LONG).show();
                    return;
                case 0:
                    Toast.makeText(ContactsActivity.this,
                            "failed sending modification on server, please test your connection",
                            Toast.LENGTH_LONG).show();
                    return;
            }
        }
        catch(NumberFormatException nfe) {
            json_contacts = data;
            draw_listview();
        }
    }
}
