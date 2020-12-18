package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

public class LoginPage extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText, UCLAIDEditText;
    private TextView errorTextView;
    private String username, password, id;
    private InternalMemory internalMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        internalMemory = new InternalMemory(this);
    }

    public void onLoginClick(View view) {
        usernameEditText = findViewById(R.id.loginUsernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
//        UCLAIDEditText = findViewById(R.id.uclaIDEditText);
        errorTextView = findViewById(R.id.errorTextView);

        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
//        id = UCLAIDEditText.getText().toString();
        boolean error = false;
        if (username.length() == 0) {
            errorTextView.setText("Please enter an username\n");
            error = true;
        }
//        if (username.indexOf("@ucla.edu") != username.length() - 9) {
//            errorTextView.setText(errorTextView.getText().toString() + " Please enter your @ucla.edu email\n");
//            error = true;
//        }
        if (password.length() == 0) {
            errorTextView.setText(errorTextView.getText().toString() + " Please enter a password\n");
            error = true;
        }
//        if (id.length() < 9 || id.length() > 9) {
//            errorTextView.setText(errorTextView.getText().toString() + " Please enter your 9-digit UCLA ID\n");
//            error = true;
//        }
        if (error) return;
        loginPOST();

    }

    public void createAccount(View view) {
        Intent createAccountIntent = new Intent(this, CreateAccount.class);
        startActivity(createAccountIntent);
    }

    private void loginPOST(){
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.DATABASE_URL+"/userLogin/?username="+username+"&password="+password;

        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int start = response.indexOf(":")+2;
                        int end = response.indexOf("\"",start);
                        response = response.substring(start, end);
                        MainActivity.api_key = response;
                        MainActivity.username = username;
                        MainActivity.password = password;
                        Log.d(".LoginPage.class", MainActivity.api_key);
                        internalMemory.writeToInternalMemory(username+'\n'+password+'\n'+MainActivity.api_key);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".LoginPage", "wrong Username/pword"+error.toString());
            }
        });

        queue.add(userPOSTRequest);
    }
}