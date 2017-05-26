package com.example.setup.gps_tracking;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity
{
    private LinearLayout contacts_layout;
    private String json_contacts;
    private ArrayList<String> listItems;
    private ArrayAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.contacts_toolbar);
        setSupportActionBar(toolbar);

        contacts_layout = (LinearLayout)findViewById(R.id.contacts_layout);
        json_contacts = getIntent().getStringExtra("json_contacts");
        create_checkboxes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
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
                myIntent = new Intent(this, MapsActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void create_checkboxes()
    {
        try
        {
            JSONArray users = new JSONArray(json_contacts);

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
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id)
                {

                    listView.getChildAt(position).setBackgroundColor(Color.rgb(125, 75, 236));
                    AlertDialog.Builder adb = new AlertDialog.Builder(ContactsActivity.this);
                    adb.setTitle("ListView OnClick");
                    adb.setMessage("Selected Item is = " + listView.getItemAtPosition(position));
                    adb.setPositiveButton("Ok", null);
                    adb.show();
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
