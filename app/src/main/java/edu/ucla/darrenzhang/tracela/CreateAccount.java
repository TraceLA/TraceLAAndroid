package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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

        JSONObject user = new JSONObject();
        try {
            user.put("first_name", firstName);
            user.put("last_name", lastName);
            user.put("username", username);
            user.put("password", password);
            user.put("email", email);
            user.put("studentid", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        JsonObjectRequest userPOSTRequest = new JsonObjectRequest(Request.Method.POST, Constants.usersURL, user,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        String text = response.toString();
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