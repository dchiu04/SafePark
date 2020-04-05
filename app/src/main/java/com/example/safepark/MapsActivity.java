package com.example.safepark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.safepark.App.CHANNEL_1_ID;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback{
    private NotificationManagerCompat notificationManager;
    private EditText minutes;
    private EditText hours;
    private GoogleMap mMap;
    private RequestQueue mQueue;
    private Marker marker;
    private AppBarConfiguration mAppBarConfiguration;
    private Button park_here;
    private Button bt1;
    private Button cancel;
    private String probability;
    TextView et;
    public static final String CHANNEL_ID = "channel1";
    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bt1 = findViewById(R.id.btn1);
        cancel = findViewById(R.id.cancel);
        et = findViewById(R.id.textView);
        notificationManager = NotificationManagerCompat.from(this);
        minutes = findViewById(R.id.minutes);
        hours = findViewById(R.id.hours);
        park_here = findViewById(R.id.button_id);
        park_here.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){
                park_here.setVisibility(View.GONE);
                displayPopup();
            }
        });

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Allows user to cancel the timer
                cancel.setVisibility(v.VISIBLE);

                //Removes buttons/textfields for better user experience
                bt1.setVisibility(v.GONE);
                hours.setVisibility(v.GONE);
                minutes.setVisibility(v.GONE);

                int secs = 0;
                int h = 0;
                int m = 0;

                if (!hours.getText().toString().equals("")) {
                    h = Integer.parseInt(hours.getText().toString());
                    h *= 60 * 60 * 1000;
                }

                if (!minutes.getText().toString().equals("")) {
                    m = Integer.parseInt(minutes.getText().toString());
                    m *= 60 * 1000;
                }

                // Calculates the amount of time by adding hours minutes and seconds
                secs += (h + m);

                countDownTimer = new CountDownTimer(secs, 1000) {
                    int secs = 60;
                    int mins = 59;
                    int hour = 0;
                    boolean minsSent = false;
                    boolean hoursSent = false;
                    @Override
                    public void onTick(long millis) {

                        // Calculating minutes remaining
                        if((int) (millis / 60 /1000) < 0) {
                            mins = (int)(millis / 60 / 1000);
                        }

                        // Resetting seconds and subtracting minutes
                        if (secs == 0 && millis >= 1000) {
                            secs = 60;
                            mins -= 1;
                        }
                        secs -= 1;

                        // User specified minutes only
                        if (!minutes.getText().toString().equals("") && !minsSent ) {
                            mins = Integer.parseInt(minutes.getText().toString());
                            mins -= 1;
                            minsSent = true;
                        }

                        et.setText("Time Remaining: " + String.format("%02d",(int) (millis / 60 / 60 / 1000) ) +
                                ":" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
                    }

                    @Override
                    public void onFinish() {
                        et.setText("Your timer has finished. Press cancel to set another parking spot.");
                        sendNotification(v);
                    }
                }.start();
            }

        });
    }

    // Cancels the count down timer
    public void cancel(View v) {
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            et.setText("");
            et.setVisibility(v.GONE);
            park_here.setVisibility(v.VISIBLE);
            bt1.setVisibility(v.GONE);
            cancel.setVisibility(v.GONE);
        }

    }
    public void onMenuClick(View v){
        Intent i = new Intent(MapsActivity.this, MainActivity.class);
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setMQueue(){
        mQueue = Volley.newRequestQueue(this);
    }

    public String convertPercentage(String json){
        String s = json.substring(0,7);
        Double d = Double.parseDouble(s) * 100;
        return String.format("%.2f", d);
    }

    public void makeMarker(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(point)
                .title("My Parking Spot")
                .snippet("There is a " + probability + "% chance of a vehicular crime\noccuring in a 1 mile radius in the next hour")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker = mMap.addMarker(markerOptions);
    }

    public void jsonParse(String lat, String lon, final LatLng point){
        String url = "https://poisson-distribution-vpd.herokuapp.com/?lon="+lon+"&distance=1&lat="+lat;
        System.out.println(url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            probability = convertPercentage(response.getString("probabilityOfACrimeInTheNextHour"));
                            makeMarker(point);

                            park_here.setVisibility(View.VISIBLE);
                            System.out.println("Successfully parsed json " + probability);
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    public void displayPopup(){
        minutes.setVisibility(View.VISIBLE);
        hours.setVisibility(View.VISIBLE);
        bt1.setVisibility(View.VISIBLE);
        et.setVisibility(View.VISIBLE);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Vancouver and move the camera
        LatLng vancouver = new LatLng(49.246292, -123.116226);
        mMap.addMarker(new MarkerOptions().position(vancouver).title("Current location "));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 17.0f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                setMQueue();
                jsonParse(Double.toString(point.latitude), Double.toString(point.longitude), point);
                mMap.clear();
                mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker m) {
                if(m.isInfoWindowShown()){
                    m.hideInfoWindow();
                    System.out.println("hiding info window");
                }else{
                    m.showInfoWindow();
                    System.out.println("showing info window");
                }
                return true;
            }
        });
    }


    /**sendNotification
     * Sends notifications to the user when their timer has ran out.
     * @param v View the current view - MapsActivity
      */
    public void sendNotification(View v) {

        /** https://developer.android.com/training/notify-user/navigation
         * allows user to go back to the app (should be the popup "parking notification"
        */

        String title = "SafePark - Your timer has expired";
        String message = "Check on your car to ensure its safety.";
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.icon_car)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);

    }
}
