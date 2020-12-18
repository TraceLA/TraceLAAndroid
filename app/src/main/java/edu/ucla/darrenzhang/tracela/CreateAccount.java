package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.security.ProviderInstaller;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CreateAccount extends AppCompatActivity {
    private EditText fNameEditText, lNameEditText, usernameEditText, emailEditText, idEditText, passwordEditText;
    private String firstName, lastName, username, email, password;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
    }

    public void createAccount(View view) {
        fNameEditText = findViewById(R.id.firstNameEditText);
        lNameEditText = findViewById(R.id.lastNameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.newEmailEditText);
        idEditText = findViewById(R.id.newIDEditText);
        passwordEditText = findViewById(R.id.newPasswordEditText);
        firstName = fNameEditText.getText().toString();
        lastName = lNameEditText.getText().toString();
        username = usernameEditText.getText().toString();
        email = emailEditText.getText().toString();
        id = Integer.valueOf(idEditText.getText().toString());
        password = passwordEditText.getText().toString();
        addUserToDatabase();
//        addUserJSON();
        finish();
    }

    private void addUserToDatabase() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL+"/users/?first_name="+firstName+"&last_name="+lastName+"&username="+username+"&email="+email+"&studentid="+id+"&password="+password;
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String text = response.toString();
                        Log.d(".CreateAccount", "SUCCESS: " + text);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".CreateAccount", error.toString());
            }
        });
        queue.add(userPOSTRequest);
    }
//    private void addUserJSON(){
//        RequestQueue queue = Volley.newRequestQueue(this);
//
//        JSONObject data = new JSONObject();
//        try{
//            data.put("first_name",firstName);
//            data.put("last_name",lastName);
//            data.put("username",username);
//            data.put("email", email);
//            data.put("studentid",Integer.valueOf(id));
//            data.put("password",password);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        String url = Constants.DATABASE_URL+"/users";
//        JsonObjectRequest userPOSTRequest = new JsonObjectRequest(Request.Method.POST, url, data,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                     String text = response.toString();
//                     Log.d("Create account: ",text);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.d("CREATE ACCOUNT: ", error.toString());
//            }
//        });
//        queue.add(userPOSTRequest);
//    }
}