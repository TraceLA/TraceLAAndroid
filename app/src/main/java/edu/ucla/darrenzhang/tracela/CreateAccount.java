package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
        finish();
    }

    private void addUserToDatabase() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = Constants.usersURL+ "/?first_name="+firstName+"&last_name="+lastName+"&username="+username+"&email="+email+"&studentid="+id+"&password="+password;
        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        String text = response.toString();
                        Log.d("SUCCESS: ", text);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("CREATE ACCOUNT: ", error.toString());
            }
        });
        queue.add(userPOSTRequest);
    }
}