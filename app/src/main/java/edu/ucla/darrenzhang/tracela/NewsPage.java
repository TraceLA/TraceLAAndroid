package edu.ucla.darrenzhang.tracela;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NewsPage extends AppCompatActivity {
    TextView content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_page);
        content = findViewById(R.id.contentTextView);

    }

    public void getContacts(View view){
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/contacts?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray contacts) {
                        Log.d("..NewsPage"," contacts list is: "+contacts.length()+" long");
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
                                    msg += time + " at "+location;
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

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }
}