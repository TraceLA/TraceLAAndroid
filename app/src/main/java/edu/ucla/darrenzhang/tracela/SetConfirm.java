package edu.ucla.darrenzhang.tracela;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SetConfirm extends AppCompatActivity {
    public static String x;
    private String confirmUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_confirm);
        Intent intent = getIntent();
        x = intent.getStringExtra("confirmFriend");
//        Log.d("getuser", x);
        TextView friendTextView = (TextView) findViewById(R.id.textViewReq);
        friendTextView.setText(x);
    }

    public void onClickConfirm(View view) {
        confirmUsername = x;
        sendRequestConfirm();
        finish();
    }
    public void onClickReject(View view) {
        confirmUsername = x;
        sendRequestReject();
        finish();
    }

    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }

    private void sendRequestConfirm() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/friendRequest/confirm?friend_username=" + confirmUsername;
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SUCCESS: ", MainActivity.api_key);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FRIEND REQUEST: ", error.toString());
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

    private void sendRequestReject() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/friendRequest/confirm?reject=true&friend_username=" + confirmUsername;
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("SUCCESS: ", MainActivity.api_key);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FRIEND REQUEST: ", error.toString());
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
