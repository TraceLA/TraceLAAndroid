package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SymptomsSurvey extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_survey);
    }

    public void onClickPositive(View view) {
        Log.d("friend", "onclick");
        sendRequest();
        finish();
    }

    public void onClickNegative(View view) {
        Log.d("friend", "onclick");
        sendRequest2();
        finish();
    }

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }

    private void sendRequest() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/results?username=" + MainActivity.username+"&result=true&date=11-27-2020";
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

    private void sendRequest2() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/results?username=" + MainActivity.username+"&result=false&date=11-27-2020";
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