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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ArrayList<Marker> markerlist = new ArrayList<>();
    Handler handler = new Handler();
    private SharedPreferences sp;
    public SharedPreferences.Editor editor;
    private String json_contacts;
    private String server_address, email, password;

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        float zoom = 1;
        LatLng myLocation = new LatLng(0.0, 0.0);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, zoom));
        handler.post(runnableCode);
    }

    public void show_users_on_map(String json_users)
    {
        float zoom = 7;
        try
        {
            JSONArray users = new JSONArray(json_users);

            if (users.length() > 0 )
            {
                mMap.clear();
                json_contacts = json_users;
            }
            else {
                return;
            }
            for (int i = 0; i < users.length(); i++)
            {
                JSONObject c = users.getJSONObject(i);
                String name = " "+c.getString("first_name")+" "+c.getString("last_name")+" ";
                double latitude = Double.parseDouble(c.getString("latitude"));
                double longitude = Double.parseDouble(c.getString("longitude"));
                LatLng userLocation = new LatLng(latitude,longitude);

                TextView text = new TextView(MapsActivity.this);
                text.setText(name);
                text.setTypeface(null, Typeface.BOLD);
                text.setTextColor(Color.BLACK);
                IconGenerator generator = new IconGenerator(MapsActivity.this);
                //generator.setBackground(MapsActivity.this.getDrawable(R.drawable.turtle));
                generator.setContentView(text);
                Bitmap icon = generator.makeIcon();

                mMap.addMarker(new MarkerOptions()
                        .position(userLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(icon)));

                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(userLocation);
                circleOptions.radius(20);
                circleOptions.strokeColor(Color.BLACK);
                circleOptions.fillColor(Color.BLACK);
                circleOptions.strokeWidth(2);
                mMap.addCircle(circleOptions);

                if (i == 0) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoom));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private Runnable runnableCode = new Runnable()
    {
        @Override
        public void run()
        {
            String url = server_address+"/android/data_exchange/get_contacts_locations/"+email+"/"+password;
            new GetUsersCoordinatesTask(MapsActivity.this).execute(url);
            handler.postDelayed(runnableCode, 60 * 1000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
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
                myIntent = new Intent(this, MainActivity.class);
                startActivity(myIntent);
                return true;
            case R.id.action_contacts:
                myIntent = new Intent(this, ContactsActivity.class);
                myIntent.putExtra("json_contacts", json_contacts);
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
        handler.removeCallbacks(runnableCode);
    }
}