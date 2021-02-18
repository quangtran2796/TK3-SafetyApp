package com.example.tk3_safetyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.text.Html;
import android.os.Handler;
import android.util.Pair;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tk3_safetyapp.backend.supermarket.SupermarketHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.DetectedActivity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * This MainActivity class provides three services:
 * 1. Remind to wear the seatbelt, when the user starts to drive
 * 2. Remind to wear the helmet, when the user starts to ride the bike
 * 3. Remind to wear the mask, when the user moves near to a supermarket
 */
public class MainActivity extends AppCompatActivity {
    Switch swCar, swBike, swPublic, swShop;
    TextView txtActivity, txtLatitude, txtLongitude;
    int currentActivity = DetectedActivity.UNKNOWN;

    //Create a 10s timer to periodically check the location when the shop switch is on
    private Handler mHandler = new Handler();
    private int mInterval = 10000;

    private NotificationManagerCompat notificationManager;

    FusedLocationProviderClient fusedLocationProviderClient;

    private String TAG = MainActivity.class.getSimpleName();
    BroadcastReceiver broadcastReceiver;

    private SupermarketHelper supermarketHelper;
    private double currentLocationLat = 0;
    private double currentLocationLon = 0;
    private double lastLocationLat = 0;
    private double lastLocationLon = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        supermarketHelper = new SupermarketHelper(this);
        notificationManager = NotificationManagerCompat.from(this);

        assignVariable();
        broadcastReceiver();
        carReminder();
        shopRemeinder();
        BikeReminder();

    }

    /**
     * Listen to the shop switch. When this switch is turned on, the distances to close supermarkets
     * will be checked every 10s
     */
    private void shopRemeinder() {
        swShop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (swShop.isChecked()) {
                    startDistanceTimer();
                } else {
                    stopDistanceTimer();
                }
            }
        });
    }

    /**
     * Listen to the car switch. Start tracking user's activities and activating reminder
     * when this switch is turned on.
     */
    private void carReminder(){
        swCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(swCar.isChecked()){
                    startTracking();
                }
                else if(!swBike.isChecked() && !swCar.isChecked()){
                    txtActivity.setText("Off");
                    stopTracking();
                }
            }
        });
    }

    /**
     * Listen to the b switch. Start tracking user's activities and activating reminder
     * when this switch is turned on.
     */
    private void BikeReminder(){
        swBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(swBike.isChecked()){
                    startTracking();
                }
                else if(!swCar.isChecked() && !swCar.isChecked()){
                    txtActivity.setText("Off");
                    stopTracking();
                }
            }
        });

    }

    /**
     * Create a broadcast receiver to get new activity. Whenever the activity changes, the broadcast
     * receiver will call handleUserActivity to show the notification
     */
    private void broadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

    }

    /**
     * Update current location, check the distances to supermarkets and show notification when
     * the user moves near to a supermarket
     */
    private void distanceCheck() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Check permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //When permission grantedupermarket, get the current location
            getLocation();

           if(currentLocationLat != lastLocationLat || currentLocationLon != lastLocationLon){
                if(supermarketHelper.nearSupermarket(currentLocationLon, currentLocationLat)){
                    // Show notification you are near a supermarket
                    swShop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (swShop.isChecked()) {
                                Notification notification = new NotificationCompat.Builder(MainActivity.this, basechannel.CHANNEL_1_ID)
                                        .setSmallIcon(R.drawable.ic_message)
                                        .setContentTitle("Safety alert")
                                        .setContentText("You´re currently in a supermarket, please wear a mask")
                                        .build();
                                notificationManager.notify(1, notification);
                            }
                        }
                    });
               }
           }
            lastLocationLat = currentLocationLat;
            lastLocationLon = currentLocationLon;
        } else {
            //When permission denied
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    /**
     * Get current location
     */
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                Location location = task.getResult();
                if (location != null) {
                    try {
                        //Initialize geoCoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        txtLatitude.setText(String.format(Locale.US,"%.6f", addresses.get(0).getLatitude()));

                        txtLongitude.setText(String.format(Locale.US,"%.6f", addresses.get(0).getLongitude()));


                        currentLocationLat = location.getLatitude();
                        currentLocationLon = location.getLongitude();
                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    /**
     * Initialize layout variables
     */
    private void assignVariable() {
        swCar = (Switch) findViewById(R.id.switchCar);
        swBike = (Switch) findViewById(R.id.switchBike);
        swShop = (Switch) findViewById(R.id.switchShop);
        txtActivity = (TextView) findViewById(R.id.textActivity);
        txtLatitude = (TextView) findViewById(R.id.textLatitude);
        txtLongitude = (TextView) findViewById(R.id.textLongitude);
    }

    /**
     * The broadcast receiver will call this handle whenever the activity changes if the car
     * or bike switch is on and when the user's activity changes to IN_VEHICLE or ON_BICYCLE, the
     * corresponding notification will be shown
     */
    private void handleUserActivity(int type, int confidence) {
        String label = "Activity Unknown";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "In Vehicle";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = "On Bicycle";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "On Foot";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "Running";
                break;
            }
            case DetectedActivity.STILL: {
                label = "Still";
                break;
            }
            case DetectedActivity.TILTING: {
                label = "Tilting";
                break;
            }
            case DetectedActivity.WALKING: {
                label = "Walking";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = "Unknown";
                break;
            }
        }

        if (confidence > Constants.CONFIDENCE) {
            txtActivity.setText(label);

            //Sending notification when user is in Car or on Bike and Notification Switch on
            if ( type == DetectedActivity.IN_VEHICLE && currentActivity != DetectedActivity.IN_VEHICLE ){
                swCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (swCar.isChecked()) {
                            Notification notification = new NotificationCompat.Builder(MainActivity.this, basechannel.CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.ic_message)
                                    .setContentTitle("Safety alert")
                                    .setContentText("You´re currently driving, please wear a seatbelt")
                                    .build();
                            notificationManager.notify(1, notification);
                        }
                    }
                });
            }
            else if ( type == DetectedActivity.ON_BICYCLE && currentActivity != DetectedActivity.ON_BICYCLE){
                swBike.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (swBike.isChecked()) {
                            Notification notification = new NotificationCompat.Builder(MainActivity.this, basechannel.CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.ic_message)
                                    .setContentTitle("Safety alert")
                                    .setContentText("You´re currently biking, please wear a helmet")
                                    .build();
                            notificationManager.notify(1, notification);
                        }
                    }
                });
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "There is no notification", Toast.LENGTH_SHORT);
                toast.show();
            }
            currentActivity = type;
        }
    }

    /**
     * Start tracking activity
     */
    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    /**
     * Stop tracking activity
     */
    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    /**
     * start timer and check distances to supermarkets
     */
    void startDistanceTimer() {
        mStatusChecker.run();
    }

    /**
     * stop check distance timer
     */
    void stopDistanceTimer() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //Check the distances to the supermarkets and show notification
                distanceCheck();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDistanceTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Constants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}



