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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NewsPage extends AppCompatActivity {
    private TextView content;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_news_page);
        content = findViewById(R.id.contentTextView);
        checkForExposure();
        displayUpdatedToastMsg();
    }
    public void checkForExposure(){
        JsonObjectRequest contactsGetRequest = new JsonObjectRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/contacts?username=" + MainActivity.username, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject contacts) {
                        Log.d(".NewsPage"," contacts list is: "+contacts.length()+" long");
                        if (contacts.length()>0) {
                            String msg = "You have been exposed to someone who tested positive for COVID-19 on: \n";
                            String[] arr = contacts.toString().split(",");
                            for (int i = 0; i < arr.length; i++) {
                                    String userDateKeyValPair = arr[i];
                                    int indexOfColon = userDateKeyValPair.indexOf(':');
                                    String time = userDateKeyValPair.substring(indexOfColon+2, userDateKeyValPair.indexOf("\"", indexOfColon+2));
                                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH);
                                    LocalDate date = LocalDate.parse(time, inputFormatter);
                                    time = outputFormatter.format(date);
                                    msg += time + "\n";
                            }
                            content.setText(msg);
                        } else {
                            content.setText("You have not been recently exposed to anyone who has tested positive for COVID-19\n");
                        }
                        checkForInfectionSpots();
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
        queue.add(contactsGetRequest);
    }
    public void checkForInfectionSpots()
    {
        JsonArrayRequest spotsGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/spots?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray exposureSpots) {
                        Log.d(".NewsPage"," exposure spots list is: "+exposureSpots.length()+" long");
                        if (exposureSpots.length() > 0) {
                            String msg = content.getText().toString();
                            msg += "\nYou may have been exposed to COVID-19 at these potential high-risk locations:\n";
                            for (int i = 0; i < exposureSpots.length(); i++) {
                                try {
                                    msg+= exposureSpots.getString(i)+"\n";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            content.setText(msg);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".NewsPage", "Getting exposure spots for " + MainActivity.username + ": " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        });
        queue.add(spotsGetRequest);
    }

    public void getContacts(View view){
        checkForExposure();
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