package edu.ucla.darrenzhang.tracela;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class LocationUpdates extends Service {
    public static boolean isRunning = false;
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    public static long TIME_INTERVAL = 4000;    //in milliseconds
    private String username, password, api_key;
    private Intent intent;
    private LocationObject currentLocationObject;
    public static final double DISTANCE_THRESHOLD = 6; //meters
    private boolean locationNeedsToBeUpdated =false;


    public LocationUpdates() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        this.intent = intent;
        isRunning =true;
        startSelf();
        return START_STICKY;
    }

    public void startSelf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        } else {
            startForeground(1, new Notification());
        }
        isRunning = true;
        if (intent.getExtras() != null) {
            username = intent.getStringExtra("username");
            password = intent.getStringExtra("password");
            api_key = intent.getStringExtra("api_key");
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sendLocation();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
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
        startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(".LocationUpdate", "onTaskRemoved() called");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
        sendLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startSelf();
        Log.d(".LocationUpdates", "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sendLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest locRequest = LocationRequest.create();
        locRequest.setPriority(PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(TIME_INTERVAL);
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                double latitude = currentLocation.getLatitude();
                double longitude = currentLocation.getLongitude();
//                boolean hasMoved = false;

                Log.d(".LocationUpdates","Recieved Location Data: ( "+latitude +", "+longitude+")");
                if (currentLocationObject == null) {
                    currentLocationObject = new LocationObject(latitude, longitude);
//                    hasMoved = true;
                    locationNeedsToBeUpdated = true;
                } else {
                    double dist = currentLocationObject.getDistanceInMeters(latitude, longitude);
                    Log.d(".LocationUpdates","Distance: "+dist);
                    if (dist > DISTANCE_THRESHOLD) {
//                        hasMoved = true;
                        locationNeedsToBeUpdated = true;
                    }
                    currentLocationObject = new LocationObject(latitude, longitude);
                }
                //Log.d("Locations", currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                Log.d(".LocationUpdates","Has moved is "+locationNeedsToBeUpdated);


                if (locationNeedsToBeUpdated) {
                    RequestQueue queue = Volley.newRequestQueue(LocationUpdates.this);
//                Log.d("Send Coords: ", username +", "+api_key);
                    String url = Constants.DATABASE_URL + "/coords/?lat=" + latitude + "&long=" + longitude + "&username=" + username + "&api_key=" + api_key;

                    StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    locationNeedsToBeUpdated = false;
                                    String text = response.toString();
                                    Log.d(".LocationUpdate", "successfully sent coordinates. " + text);
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.toString().equals("com.android.volley.AuthFailureError")) {
                                // startLoginActivity();
                                updateApiKey();
                            }
                            Log.d(".LocationUpdate", "Error sending coordinates. " + error.toString());
                            currentLocationObject = null;
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
            }
        };
        mFusedLocationClient.requestLocationUpdates(locRequest, locationCallback, Looper.myLooper());

    }

    private void updateApiKey() {
        RequestQueue queue = Volley.newRequestQueue(this);

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

                        Log.d("SUCCESS: ", api_key);
//                        writeCredentialsToMemory();
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