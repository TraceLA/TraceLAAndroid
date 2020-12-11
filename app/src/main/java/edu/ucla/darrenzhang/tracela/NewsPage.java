package edu.ucla.darrenzhang.tracela;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NewsPage extends AppCompatActivity {
    private TextView content;
    public static final int DISTANCE_THRESHOLD_FOR_SPOT = 100;      //in meters
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page);
        content = findViewById(R.id.contentTextView);
        checkForExposure();
        checkForInfectionSpots();
        displayUpdatedToastMsg();
    }
    public void checkForExposure(){
        //TODO: this should ideally be a post request to exposure not contacts, need to implement once backend updates
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/contacts?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray contacts) {
                        Log.d(".NewsPage"," contacts list is: "+contacts.length()+" long");
                        if (contacts.length()>0) {
                            String msg = "You have been exposed to someone who tested positive for COVID-19 on: \n";
                            for (int i = 0; i < contacts.length(); i++) {
                                try{
                                    JSONObject contact = contacts.getJSONObject(contacts.length()-1);
                                    String location = contact.getString("location");
                                    String time = contact.getString("date");
                                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH);
                                    LocalDate date = LocalDate.parse(time, inputFormatter);
                                    time = outputFormatter.format(date);
                                    msg += time + " at "+location+"\n";
                                    Log.d(".NewsPage", MainActivity.username + " was exposed to " + contact.getString("other_username")+" at "+location+": "+time);
                                }catch (JSONException e){
                                    Log.d(".NewsPage","error processing contacts data: "+e.toString());
                                }
                            }
                            content.setText(msg);
                        } else {
                            content.setText("You have not been recently exposed to anyone who has tested positive for COVID-19");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".NewsPage", "Getting contacts for " + MainActivity.username + ": " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });
        queue.add(coordGetRequest);
    }
    public void checkForInfectionSpots()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/contacts?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray contacts) {
                        Log.d(".NewsPage"," contacts list is: "+contacts.length()+" long");
                        if (contacts.length()>0) {
                            getPastLocation(contacts);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".NewsPage", "Getting contacts for " + MainActivity.username + ": " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });
        queue.add(coordGetRequest);
        Toast.makeText(this, "UPDATED",Toast.LENGTH_SHORT).show();
    }
    public void getPastLocation(JSONArray infectionSpots){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/coords?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray coordinates) {
                        Log.d(".NewsPage",MainActivity.username+"'s location list is "+coordinates.length()+" long");
                        String msg = content.getText().toString();
                        for (int i = 0; i < coordinates.length(); i++) {
                            try{
                                JSONObject coordinate = coordinates.getJSONObject(i);
                                double latitude = coordinate.getDouble("lat");
                                double longitude = coordinate.getDouble("lng");
                                LocationObject userLoc = new LocationObject(latitude, longitude);
                                String time = coordinate.getString("date");
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH);
                                LocalDate date = LocalDate.parse(time, inputFormatter);
                                time = outputFormatter.format(date);
                                for (int j =0; j < infectionSpots.length(); j++){
                                    JSONObject spot = infectionSpots.getJSONObject(j);
                                    if (userLoc.getDistanceInMeters(spot.getDouble("lat"), spot.getDouble("lng")) <= DISTANCE_THRESHOLD_FOR_SPOT)
                                    {
                                        msg += time+" at ("+latitude+", "+longitude+")\n";
                                    }
                                }
                            }catch (JSONException e){
                                Log.d(".NewsPage","error processing location data: "+e.toString());
                            }
                        }
                        content.setText(msg);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    Log.d(".NewsPage", "Can't get coordinates for "+MainActivity.username+ " because they have turned off location sharing with friends");
                }else{
                    Log.d(".NewsPage", "Error Getting coordinates for " + MainActivity.username + ": " + error.toString());
                }
            }
        });
        queue.add(coordGetRequest);
    }

    public void getContacts(View view){
        checkForExposure();
        checkForInfectionSpots();
        displayUpdatedToastMsg();
    }
    public void displayUpdatedToastMsg()
    {
        Toast.makeText(this, "UPDATED",Toast.LENGTH_SHORT).show();
    }

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }
}