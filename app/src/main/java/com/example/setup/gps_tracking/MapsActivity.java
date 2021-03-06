package com.example.setup.gps_tracking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, httpGetRequestInterface
{
    private GoogleMap mMap;
    Handler connection_handler = new Handler();
    private SharedPreferences sp;
    public SharedPreferences.Editor editor;
    private String server_address, email, password;
    private int request_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("settings", getApplicationContext().MODE_PRIVATE);
        editor = sp.edit();
        server_address = sp.getString("server_address", "");
        email = sp.getString("email", "");
        password = sp.getString("password", "");
        request_counter = 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        LatLng myLocation = new LatLng(0.0, 0.0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 1));
        connection_handler.post(fetch_contacts_from_server);
    }

    @Override
    public void RequestResult(String data)
    {
        try
        {
            JSONArray contacts = new JSONArray(data);

            if ( contacts.length() == 0 )
            {
                return;
            }
            mMap.clear();
            for (int i = 0; i < contacts.length(); i++)
            {
                JSONObject c = contacts.getJSONObject(i);
                String name = " "+c.getString("first_name")+" "+c.getString("last_name")+" ";
                try
                {
                    double latitude  = Double.parseDouble(c.getString("latitude"));
                    double longitude = Double.parseDouble(c.getString("longitude"));

                    LatLng userLocation = new LatLng(latitude,longitude);

                    TextView text = new TextView(MapsActivity.this);
                    text.setText(name);
                    text.setTypeface(null, Typeface.BOLD);
                    text.setTextColor(Color.BLACK);
                    IconGenerator generator = new IconGenerator(MapsActivity.this);
                    generator.setContentView(text);
                    Bitmap icon = generator.makeIcon();

                    mMap.addMarker(new MarkerOptions().position(userLocation)
                                                      .icon(BitmapDescriptorFactory.fromBitmap(icon)));

                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(userLocation);
                    circleOptions.radius(20);
                    circleOptions.strokeColor(Color.BLACK);
                    circleOptions.fillColor(Color.BLACK);
                    circleOptions.strokeWidth(2);
                    mMap.addCircle(circleOptions);

                    if (request_counter == 0)
                    {
                        request_counter = 1;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 8));
                    }

                }
                catch (NumberFormatException e){
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private Runnable fetch_contacts_from_server = new Runnable()
    {
        @Override
        public void run()
        {
            String url = server_address+"/android/data_exchange/get_contacts_locations/"+email+"/"+password;
            new HttpGetRequestTask(MapsActivity.this).execute(url);
            connection_handler.postDelayed(fetch_contacts_from_server, 60 * 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent myIntent;
        switch (item.getItemId())
        {
            case R.id.action_settings:
                myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_contacts:
                myIntent = new Intent(this, ContactsActivity.class);
                startActivity(myIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        connection_handler.removeCallbacks(fetch_contacts_from_server);
    }
}
