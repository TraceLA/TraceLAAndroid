package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class SymptomsSurvey extends AppCompatActivity {
    private EditText etOtherUsername, etDate, etAddress;
    private Switch contactsSwitch, covidResultSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_survey);
        etOtherUsername = findViewById(R.id.editTextOtherUsername);
        etDate = findViewById(R.id.editTextDate);
        etAddress = findViewById(R.id.addressOfLocationEditText);
        contactsSwitch = findViewById(R.id.contactsSwitch);
        covidResultSwitch = findViewById(R.id.covidResultSwitch);
        contactsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    etOtherUsername.setVisibility(View.VISIBLE);
                    etDate.setVisibility(View.VISIBLE);
                    etAddress.setVisibility(View.VISIBLE);
                }else{
                    etOtherUsername.setVisibility(View.INVISIBLE);
                    etDate.setVisibility(View.INVISIBLE);
                    etAddress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void submitSurvey(View view){
        if (contactsSwitch.isChecked()){
            Log.d(".SymptomsSurvey","in contactsSwitch.isChecked()");
            String oUsername = etOtherUsername.getText().toString();
            String date = etDate.getText().toString();
            String location = etAddress.getText().toString();
            if (oUsername.length()== 0 || date.length() ==0 || location.length() ==0){
                Log.d(".SymptomsSurvey", "invalid arguments for the contacts");
                String msg = "Please enter the username of the person you were in contact with, the date of contact in mm-dd-yyyy format, and the address of the contact";
                Toast.makeText(this, Html.fromHtml("<font color='#ed0000'>"+ msg+"</font>"),Toast.LENGTH_LONG).show();
                return;
            }else{
                Log.d(".SymptomsSurvey", "posting contact to backend");
                postContact(oUsername, location, date);
            }
        }
        sendCOVIDResult(covidResultSwitch.isChecked());
        finish();
    }

    public void postContact(String oUsername, String location, String date){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/contacts/?other_username=" + oUsername+"&location="+location+"&date="+date;
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(".SymptomsSurvey", "successfully sent contact: "+MainActivity.api_key);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SYMPTOMS SURVEY: ", error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api-key", MainActivity.api_key);
                return params;
            }
        };
        queue.add(userPOSTRequest);
    }

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }

    private void sendCOVIDResult(boolean positive) {
        String msg = positive ? "true" : "false";
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/results?username=" + MainActivity.username+"&result="+msg+"&date="+Calendar.getInstance().getTime();
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(".SymptomsSurvey", "successfully sent positive test: "+MainActivity.api_key);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SYMPTOMS SURVEY: ", error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")){
                    startLoginActivity();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api-key", MainActivity.api_key);
                return params;
            }
        };
        queue.add(userPOSTRequest);
    }
    
}