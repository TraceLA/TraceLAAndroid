package edu.ucla.darrenzhang.tracela;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class FriendRequest extends AppCompatActivity {
    public static String s;
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
    //    private void sendRequest() {
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        String url = Constants.DATABASE_URL;
//        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        String text = response.toString();
//                        Log.d("SUCCESS: ", text);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("SEND REQUEST: ", error.toString());
//            }
//        });
//        queue.add(userPOSTRequest);
//    }

}

