package edu.ucla.darrenzhang.tracela;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class LocationUpdates extends Service {
    public static boolean isRunning = false;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    public static long TIME_INTERVAL = 60000;    //in milliseconds
    private String username, password, api_key;
    private Intent intent;
    private LocationObject currentLocationObject;
    public static final double DISTANCE_THRESHOLD = 6; //meters
    private boolean locationNeedsToBeUpdated = false;
    private int startID;
    private LocationCallback locationCallback;


    public LocationUpdates() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        this.intent = intent;
        this.startID = startID;
        Log.d(".LocationUpdates", "received start location updates intent");
        if (locationCallback!= null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            stopForeground(true);
        }
        isRunning = true;
        startForegroundTask();
        return START_STICKY;
    }

    public void startForegroundTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundHighAPILvl();
        } else {
            startForeground(startID, new Notification());
        }
        isRunning = true;
        if (intent.getExtras() != null) {
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("password");
            api_key = intent.getStringExtra("api_key");
            if (username == null || password == null || api_key == null || username.equals("") || password.equals("") || api_key.equals("")){
                return;
            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationUpdates();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundHighAPILvl() {
        String NOTIFICATION_CHANNEL_ID = "TraceLA";
        String channelName = "Background Location Updates";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Location Updates are running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(startID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        mFusedLocationClient.removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelfResult(startID);
        Log.d(".LocationUpdates", "Updating Location Service and Foreground task destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locRequest = LocationRequest.create();
        locRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(TIME_INTERVAL);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
                double dist = 0;

                if (currentLocationObject == null) {
                    currentLocationObject = new LocationObject(latitude, longitude);
                    locationNeedsToBeUpdated = true;
                } else {
                    dist = currentLocationObject.getDistanceInMeters(latitude, longitude);
                    if (dist > DISTANCE_THRESHOLD) {
                        locationNeedsToBeUpdated = true;
                    }
                    currentLocationObject = new LocationObject(latitude, longitude);
                }
                Log.d(".LocationUpdates", "Has moved is " + locationNeedsToBeUpdated + " and distance moved is " + dist + " meters");


                if (locationNeedsToBeUpdated) {
                    postLocation(latitude, longitude);
                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(locRequest, locationCallback, Looper.myLooper());
    }

    private void postLocation(double latitude, double longitude){
        RequestQueue queue = Volley.newRequestQueue(LocationUpdates.this);
        String url = Constants.DATABASE_URL + "/coords/?lat=" + latitude + "&long=" + longitude + "&username=" + username + "&api_key=" + api_key;

        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        locationNeedsToBeUpdated = false;
                        String text = response.toString();
                        Log.d(".LocationUpdate", "-------------------------------------successfully sent coordinates. " + text);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".LocationUpdate", "-------------------------------------------Error sending coordinates. " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")) {
                    updateApiKey();
                    postLocation(latitude, longitude);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api-key", api_key);
                return params;
            }
        };
        queue.add(userPOSTRequest);
    }
    private void updateApiKey() {
        RequestQueue queue = Volley.newRequestQueue(this);
        if (username == null || password == null) return;
        String url = Constants.DATABASE_URL + "/userLogin/?username=" + username + "&password=" + password;
        Log.d(".LocationUpdate", "Login Post Attempt: " + username + ", " + password);

        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int start = response.indexOf(":") + 2;
                        int end = response.indexOf("\"", start);
                        response = response.substring(start, end);
                        api_key = response;

                        Log.d("SUCCESS: ", api_key+MainActivity.username);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("wrong Username/pword", error.toString());
            }
        });

        queue.add(userPOSTRequest);
    }
}