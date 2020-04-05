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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.example.safepark.App.CHANNEL_1_ID;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private NotificationManagerCompat notificationManager;
    private EditText seconds;
    private EditText minutes;
    private EditText hours;
    private GoogleMap mMap;
    private Marker marker;
    private AppBarConfiguration mAppBarConfiguration;
    private Button bt1;
    TextView et;
    public static final String CHANNEL_ID = "channel1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bt1 = findViewById(R.id.btn1);
        et = findViewById(R.id.textView);
        notificationManager = NotificationManagerCompat.from(this);
        seconds = findViewById(R.id.seconds);
        minutes = findViewById(R.id.minutes);
        hours = findViewById(R.id.hours);


        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //DEFAULT VALUE WAITS OF 10 SECONDS
                int secs = 0;
                int h = 0;
                int m = 0;

                // Not changing time properly when user enters a time
                    if (!seconds.getText().toString().equals("")) {
                        secs = Integer.parseInt(seconds.getText().toString());
                    }
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
                    System.out.println("Passing in total seconds:" + secs);
                    CountDownTimer countDownTimer = new CountDownTimer(secs, 1000) {
                        @Override
                        public void onTick(long millis) {

                            et.setText("seconds: " + (int)(millis / 1000));
                        }

                        @Override
                        public void onFinish() {
                            et.setText("Finished! Should be getting a phone notification");
                            sendNotification(v);
                        }
                    }.start();
                }

        });
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
        mMap.addMarker(new MarkerOptions().position(vancouver).title("Marker in Vancouver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vancouver, 17.0f));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point));
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
        String message = "CHECK ON YOUR CAR OR IT WILL BE TOWED.";
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
