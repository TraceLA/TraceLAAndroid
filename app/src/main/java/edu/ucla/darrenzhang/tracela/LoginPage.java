package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.icu.text.IDNA;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;

public class LoginPage extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, UCLAIDEditText;
    private TextView errorTextView;
    private String email, password, id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    public void onLoginClick(View view) {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        UCLAIDEditText = findViewById(R.id.uclaIDEditText);
        errorTextView = findViewById(R.id.errorTextView);

        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        id = UCLAIDEditText.getText().toString();
        boolean error = false;
        if (email.length() == 0) {
            errorTextView.setText("Please enter an email\n");
            error = true;
        }
        if (email.indexOf("@ucla.edu") != email.length() - 9) {
            errorTextView.setText(errorTextView.getText().toString() + " Please enter your @ucla.edu email\n");
            error = true;
        }
        if (password.length() == 0) {
            errorTextView.setText(errorTextView.getText().toString() + " Please enter a password\n");
            error = true;
        }
        if (id.length() < 9 || id.length() > 9) {
            errorTextView.setText(errorTextView.getText().toString() + " Please enter your 9-digit UCLA ID\n");
            error = true;
        }
        if (error) return;
        processUserCredentials();
//        if (userIsInDatabase()) {
//            writeToInternalMemory(email + "\n");
//            writeToInternalMemory(password + "\n");
//            writeToInternalMemory(id + "\n");
//            finish();
//        } else {
//            errorTextView.setText("Please create an account with the Sign Up button\n");
//            return;
//        }
    }

    public void writeToInternalMemory(String s) {
        try (FileOutputStream fos = openFileOutput("memory", Context.MODE_APPEND)) {
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createAccount(View view) {
        Intent createAccountIntent = new Intent(this, CreateAccount.class);
        startActivity(createAccountIntent);
    }

    private void processUserCredentials() {
        RequestQueue queue = Volley.newRequestQueue(this);

//        final InfoObject inDatabase = new InfoObject(false);
        JsonArrayRequest userGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.usersURL, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray userList) {
                        for (int i = 0; i < userList.length(); i++) {
                            JSONObject user = null;
                            try {
                                user = userList.getJSONObject(i);
                                if (user.getString("email").equals(email) /*&& user.getString("password").equals(password)*/){
//                                     inDatabase.setInDatabase(true);
                                     Log.d("matched: ",email);
                                     writeCredentialsToMemory();
                                     finish();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            if (inDatabase.isInDatabase())
//                                break;
                        }
                        errorTextView.setText("Please create an account with the Sign Up button\n");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("CREATE ACCOUNT: ", error.toString());
            }
        });

        queue.add(userGetRequest);
//        Log.d("database contains:", Boolean.toString(inDatabase.isInDatabase()));
//        return inDatabase.isInDatabase();
    }
    private void writeCredentialsToMemory(){
        writeToInternalMemory(email + "\n");
        writeToInternalMemory(password + "\n");
        writeToInternalMemory(id + "\n");
    }
}