package edu.ucla.darrenzhang.tracela;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static edu.ucla.darrenzhang.tracela.Constants.ERROR_DIALOG_REQUEST;
import static edu.ucla.darrenzhang.tracela.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static edu.ucla.darrenzhang.tracela.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean mLocationPermissionGranted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    public static String username, password, api_key;
    private ToggleButton toggle;
    private boolean sendingLocation = true;
    private ToggleButton toggleFriendSharing;
    private boolean sharingWithFriends=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (internalMemoryIsEmpty()) {
            startLoginActivity();
        }
        setCredentials();
        Log.d("CREDENTIALS", username+", "+password+", "+api_key);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sendingLocation = getSendingBoolFromMemory();
        toggle = (ToggleButton) findViewById(R.id.toggleUpdateLocButton);

        toggleFriendSharing = findViewById(R.id.shareLocationToggleBtn);
        sharingWithFriends = getSharingPermissionsFromMemory();

        toggle.setChecked(sendingLocation);
        toggleFriendSharing.setChecked(sharingWithFriends);
        if (sendingLocation){
            if (!LocationUpdates.isRunning) {
                Log.d(".MainActivity", "starting");
                startUpdatingLocation();
            }
        } else if (LocationUpdates.isRunning){
            endUpdatingLocation();
        }

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                        Log.d(".MainActivity", "ToggleButton turned on");
                        sendingLocation = true;
                        updateInternalMemoryToggleState(true);
                        startUpdatingLocation();
                }else{
                        Log.d(".MainActivity", "ToggleButton turned off");
                        sendingLocation = false;
                        updateInternalMemoryToggleState(false);
                        endUpdatingLocation();
                }
            }
        });

        toggleFriendSharing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    if (!sharingWithFriends) {
                        postSharingWithFriendsPermissions(true);
                    }
                } else {
                    if (sharingWithFriends) {
                        postSharingWithFriendsPermissions(false);
                    }
                }
            }
        });
    }
    public void postSharingWithFriendsPermissions(boolean sharingState){
        int msg = sharingState ? 1 : 0;
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL+"/userPrivacy/?allowSharing="+msg;

        StringRequest updateFriendSharingPermissions = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(".MainActivity", "successfully update sharing permissions to "+ (sharingState ? "on":"off"));
                        sharingWithFriends = sharingState;
                        updateInternalMemorySharingPermissions(sharingState);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".MainActivty", "updating sharing permissions to "+sharingState+": "+error.toString());
                sharingWithFriends = !sharingState;
                toggleFriendSharing.setChecked(!sharingState);
                if (error.toString().equals("com.android.volley.AuthFailureError")) {
                    startLoginActivity();
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

        queue.add(updateFriendSharingPermissions );
    }


    public void startUpdatingLocation(){
        if (!LocationUpdates.isRunning) {
            Log.d(".MainActivity", "started updating location");
            Intent startUpdateLocIntent = new Intent(this, LocationUpdates.class);
            startUpdateLocIntent.putExtra("username", username);
            startUpdateLocIntent.putExtra("password", password);
            startUpdateLocIntent.putExtra("api_key", api_key);

            startService(startUpdateLocIntent);
        }
    }
    public void endUpdatingLocation(){
        Log.d(".MainActivity", "ended updating location");
        Intent endUpdateLocIntent = new Intent(this, LocationUpdates.class);
        stopService(endUpdateLocIntent);
    }
    public void updateInternalMemoryToggleState(boolean sending){
        writeToInternalMemory(username+'\n'+password+'\n'+MainActivity.api_key+'\n'+sending+'\n'+sharingWithFriends);
    }
    public void updateInternalMemorySharingPermissions(boolean sharing){
        writeToInternalMemory(username+'\n'+password+'\n'+MainActivity.api_key+'\n'+sendingLocation+'\n'+sharing);
    }

    public boolean getSharingPermissionsFromMemory(){
        FileInputStream stream = null;
        try {
            stream = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))){
            for (int i = 0; i<4;i++) {
                reader.readLine();
            }
            String line = reader.readLine();
            if (line == null || line.length() == 0){
                return true;
            }else{
                return line.equals("true");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }
    public boolean getSendingBoolFromMemory(){
        FileInputStream stream = null;
        try {
            stream = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))){
            for (int i = 0; i<3;i++) {
               reader.readLine();
            }
            String line = reader.readLine();
            if (line == null || line.length() == 0){
                return true;
            }else{
                return line.equals("true");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return true;
    }

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }

    public boolean internalMemoryIsEmpty(){
        try (FileOutputStream fos = openFileOutput("memory", Context.MODE_APPEND)){    //this creates a "memory" file in case it was never created before

        }catch(IOException e){
            e.printStackTrace();
        }

        FileInputStream stream = null;
        try {
            stream = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))){
            String line = reader.readLine();
            if (line == null || line.length() == 0){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    public void setCredentials(){
        FileInputStream stream = null;
        try {
            stream = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))){
            username = reader.readLine();
            password = reader.readLine();
            api_key = reader.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void writeToInternalMemory(String s){
        try (FileOutputStream fos = openFileOutput("memory", Context.MODE_PRIVATE)){
            fos.write(s.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void logout(View view){
        writeToInternalMemory("");
        startLoginActivity();
    }
    public void onClickNews(View view) {
        Intent intent = new Intent(this, NewsPage.class);
        startActivity(intent);
    }

    public void onClickFriends(View view) {
        Intent intent = new Intent(this, Friends.class);
        startActivity(intent);
    }

    public void onClickFriendView(View view) {
        Intent intent = new Intent(this, ViewFriend.class);
        startActivity(intent);
    }

    public void onClickSurvey(View view) {
        Intent intent = new Intent(this, SymptomsSurvey.class);
        startActivity(intent);
    }

    public void onClickMap(View view) {
        Intent intent = new Intent(this, Maps.class);
        startActivity(intent);
    }

    public void onClickAboutUs(View view) {
        Intent intent = new Intent(this, AboutUs.class);
        startActivity(intent);
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        if (requestCode == PERMISSIONS_REQUEST_ENABLE_GPS){
            if(!mLocationPermissionGranted){
                getLocationPermission();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(!mLocationPermissionGranted){
                getLocationPermission();
            }
        }
    }

}