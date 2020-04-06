package com.example.safepark;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.ui.AppBarConfiguration;
import android.app.Notification;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import static com.example.safepark.App.CHANNEL_1_ID;

/**MapsActivity
 * Main screen that allows the user to pick a point on the map in Vancouver to
 * set a timer for parking.
 */
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
    private boolean timer;
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

        //Park button that displays the edit texts
        park_here.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){
                park_here.setVisibility(View.GONE);
                displayPopup();
            }
        });

        // Park here button that starts the timer
        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                timer = true;

                //Allows user to cancel the timer once timer has been started
                cancel.setVisibility(v.VISIBLE);

                //Removes buttons/text fields for better user experience
                bt1.setVisibility(v.GONE);
                hours.setVisibility(v.GONE);
                minutes.setVisibility(v.GONE);

                int totalSecs = 0;
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
                totalSecs += (h + m);

                countDownTimer = new CountDownTimer(totalSecs, 1000) {
                    int secs = 60;
                    int mins = 59;
                    boolean minsSent = false;

                    /**onTick
                     * Method is called every countDownInterval(1000 milliseconds or 1 second)
                     * @param millis long which is taken from totalSecs
                     */
                    @Override
                    public void onTick(long millis) {
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

                        String strHrs = String.format(Locale.getDefault(),"%02d",(int) (millis / 60 / 60 / 1000)) + ":";
                        String strMins = String.format(Locale.getDefault(),"%02d", mins) + ":";
                        String strSecs = String.format(Locale.getDefault(),"%02d", secs);
                        String tr = getResources().getString(R.string.time_remaining);
                        String text = tr + strHrs + strMins + strSecs;
                        et.setText(text);
                    }

                    @Override
                    public void onFinish() {
                        String tr = getResources().getString(R.string.finished);
                        et.setText(tr);
                        sendNotification(v);
                        timer = false;
                    }
                }.start();
            }
        });
    }

    /**cancel
     * Cancels the CountDownTimer and shows/hides the necessary elements
     * @param v View that it is currently on
     */
    public void cancel(View v) {
        // Cancels the timer
        if(countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            et.setText("");

            et.setVisibility(v.GONE);
            park_here.setVisibility(v.VISIBLE);
            bt1.setVisibility(v.GONE);
            cancel.setVisibility(v.GONE);

            //Allows the user to re-select a new location to park
            timer = false;
        }
    }

    /**setMQueue
     * Creates a new volley request queue.
     */
    public void setMQueue(){
        mQueue = Volley.newRequestQueue(this);
    }

    /**convertPercentage
     * Converts the json string into a string with maximum 2 decimal places.
     * @param json String the number to be converted
     * @return String formatted to maximum 2 decimal places
     */
    public String convertPercentage(String json){
        String s = json.substring(0,7);
        Double d = Double.parseDouble(s) * 100;
        return String.format(Locale.getDefault(),"%.2f", d);
    }

    /**makeMarker
     * Creates a blue marker signifying the user has moved it around to be selected for parking.
     * @param point LatLng the location of the marker to be selected
     */
    public void makeMarker(LatLng point){
        String parkingSpot = getResources().getString(R.string.parking_spot);
        String infoStart = getResources().getString(R.string.info_start);
        String chance = getResources().getString(R.string.chance);
        String text = infoStart + probability + chance ;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(point)
                .title(parkingSpot)
                .snippet(text)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        marker = mMap.addMarker(markerOptions);
    }

    /**jsonParse
     * Parses the data from the given URL and adds the request to a queue.
     * @param lat String the latitude of the marker for the URL
     * @param lon String the longitude of the marker for the URL
     * @param point LatLng object that contains both the latitude and longitude to create a marker with
     */
    public void jsonParse(String lat, String lon, final LatLng point){
        String url = "https://poisson-distribution-vpd.herokuapp.com/?lon="+lon+"&distance=1&lat="+lat;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            probability = convertPercentage(response.getString("probabilityOfACrimeInTheNextHour"));
                            makeMarker(point);
                            park_here.setVisibility(View.VISIBLE);
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

    /**displayPopup
     * Changes visibility of the edit texts and text views for the user to
     * enter a period of time to start the timer.
     */
    public void displayPopup(){
        minutes.setVisibility(View.VISIBLE);
        hours.setVisibility(View.VISIBLE);
        bt1.setVisibility(View.VISIBLE);
        et.setVisibility(View.VISIBLE);
    }

    /**onMapReady
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * @param googleMap GoogleMap the map that the app loads
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
                if(!timer) {
                    setMQueue();
                    jsonParse(Double.toString(point.latitude), Double.toString(point.longitude), point);
                    mMap.clear();
                    mMap.setInfoWindowAdapter(new InfoWindowAdapter(MapsActivity.this));
                }
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
        String title = getResources().getString(R.string.notification_title);
        String message = getResources().getString(R.string.notification_message);
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
