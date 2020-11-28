package edu.ucla.darrenzhang.tracela;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class FriendRequest extends AppCompatActivity {
    public static String s;
    private String friendUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        Intent intent = getIntent();
        s = intent.getStringExtra("friendUser");
        Log.d("getuser", s);
        TextView friendTextView = (TextView) findViewById(R.id.friendRequestTextView);
        friendTextView.setText(s);
    }

    public void onClickFriendReq(View view) {
        Log.d("friend", "onclick");
        friendUsername = s;
        sendRequest();
        finish();
    }

    private void sendRequest() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL + "/friendRequest?friend_username=" + friendUsername;
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
    public void startLoginActivity(){
        Intent loginIntent = new Intent(this, LoginPage.class);
        startActivity(loginIntent);
    }
}
