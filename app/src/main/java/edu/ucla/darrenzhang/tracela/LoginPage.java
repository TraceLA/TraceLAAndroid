package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;

public class LoginPage extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, UCLAIDEditText;
    private TextView errorTextView;
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

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String id = UCLAIDEditText.getText().toString();
        boolean error = false;
        if (email.length()==0){
            errorTextView.setText("Please enter an email");
            error = true;
        }
        if (email.indexOf("@ucla.edu")!= email.length()-9){
            errorTextView.setText(errorTextView.getText().toString()+" Please enter your @ucla.edu email");
            error = true;
        }
        if (password.length()==0){
            errorTextView.setText(errorTextView.getText().toString()+" Please enter a password");
            error = true;
        }
        if (id.length() <9 || id.length()>9){
            errorTextView.setText(errorTextView.getText().toString()+" Please enter your 9-digit UCLA ID");
            error = true;
        }
        if (error) return;
        writeToInternalMemory(email+"\n");
        writeToInternalMemory(password+"\n");
        writeToInternalMemory(id+"\n");
        finish();
    }
    public void writeToInternalMemory(String s){
        try (FileOutputStream fos = openFileOutput("memory", Context.MODE_APPEND)){
            fos.write(s.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}