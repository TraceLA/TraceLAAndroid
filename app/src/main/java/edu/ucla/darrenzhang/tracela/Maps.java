package edu.ucla.darrenzhang.tracela;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng myLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Handler handler;
    private Runnable updateFriendsMarkers;
    private final long UPDATE_INTERVAL = 10000; //in milliseconds
    private TileOverlay tileOverlay;
    private HeatmapTileProvider provider;
    private boolean heatMapUninitialized = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d(".Maps", "current location: (" + latitude + ", " + longitude + ")");
                        LatLng latlngCurrLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlngCurrLoc));
                    }
                }
            }
        });
        placeMarkersForFriends();
        getAllLocationData();

        handler = new Handler();
        updateFriendsMarkers = new Runnable() {
             @Override
             public void run() {
                 placeMarkersForFriends();
                 getAllLocationData();
                 handler.postDelayed(this, UPDATE_INTERVAL);
             }
         };
        handler.post(updateFriendsMarkers);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(updateFriendsMarkers);
    }

    public void setHeatMap(List<LatLng> data){
        Log.d(".Maps", "setHeatMap is being ran");
        int[] colors = {
                Color.rgb(245,185,66),   //green
                Color.rgb(194,31,31)};    //red
        float [] startPoints = {0.2f,1f};
        Gradient gradient = new Gradient(colors,startPoints);
         provider = new HeatmapTileProvider.Builder()
                .data(data)
                 .gradient(gradient)
                .build();
         tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
        heatMapUninitialized = false;

    }

    public void updateHeatMap(List<LatLng> data){
        provider.setData(data);
        tileOverlay.clearTileCache();
    }

    public void getAllLocationData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest getAllUsers = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/coords/", new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray coordinates) {
                        HashMap<String, Date> usernames = new HashMap<>();
                        List<LatLng> locationData = new ArrayList<>();
                        ArrayList<String> coordinatesUsername = new ArrayList<>();
                        ArrayList<Marker> markers = new ArrayList<>();
                        for (int i = 0; i <coordinates.length(); i++) {
                            JSONObject coordinate = null;
                            try {
                                coordinate = coordinates.getJSONObject(i);
                                String username = coordinate.getString("username");
                                Date currTime = new Date(coordinate.getString("stamp"));
                                if (!usernames.containsKey(username) || currTime.compareTo(usernames.get(username)) > 0) {  //new user or more recent coordinate
                                    double latitude = coordinate.getDouble("lat");
                                    double longitude = coordinate.getDouble("lng");
                                    LatLng latLng = new LatLng(latitude, longitude);
                                    Log.d(".Maps", "looping through all location data: (" + latitude + ", " + longitude + ")");
                                    markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(username)));
                                    if (usernames.containsKey(username)){   //if this is an updated coordinate for an already visited user
                                        for (int j = 0; j <coordinatesUsername.size(); j++){    //remove the old coordinate from locationData
                                            if (coordinatesUsername.get(j).equals(username)){
                                                coordinatesUsername.remove(j);
                                                locationData.remove(j);
                                                markers.get(j).remove();
                                                markers.remove(j);
                                                break;
                                            }
                                        }
                                    }
                                    locationData.add(latLng);
                                    coordinatesUsername.add(username);
                                    usernames.put(username, currTime);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d(".Maps","number of coordinates is "+locationData.size());
                        if (heatMapUninitialized){
                            setHeatMap(locationData);
                        }else {
                            Log.d(".Maps", "firstTimeCreatingMaps is false");
                            updateHeatMap(locationData);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".Maps", "Trying to get list of all coordinates: "+error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });

        queue.add(getAllUsers);
    }
    public void placeMarkersForFriends() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest friendsGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/friends?username=" + MainActivity.username + "&confirmed=true", new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray userList) {
                        for (int i = 0; i < userList.length(); i++) {
                            JSONObject user = null;
                            try {
                                user = userList.getJSONObject(i);
                                String username = user.getString("username_b");
                                Log.d(".Maps",username);
                                placeMarkerFor(username);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.d(".Maps","number of friends is "+userList.length());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".Maps", error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });

        queue.add(friendsGetRequest);
    }

    public void placeMarkerFor(String friendUsername) {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/coords?username=" + friendUsername, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray coordinates) {
                        Log.d(".Maps",friendUsername+" location list is: "+coordinates.length()+" long");
                       if (coordinates.length()>0) {
                                try{
                                    JSONObject coordinate = coordinates.getJSONObject(coordinates.length()-1);
                                    double latitude = coordinate.getDouble("lat");
                                    double longitude = coordinate.getDouble("lng");
                                    LatLng friend = new LatLng(latitude, longitude);
                                    mMap.addMarker(new MarkerOptions().position(friend).title(friendUsername));
                                    Log.d(".Maps", "Marker was placed for "+friendUsername+" at ( "+latitude+", "+longitude+")");
                                }catch (JSONException e){
                                    Log.d(".Maps","error processing location data: "+e.toString());
                                }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".Maps", "Getting coordinates for " + friendUsername + ": " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });
        queue.add(coordGetRequest);
    }
    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }
}