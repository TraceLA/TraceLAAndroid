package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FileInputStream fis = null;
        try {
            fis = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Button saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText inputString = findViewById(R.id.inputText);
                write(inputString.getText().toString());
                display();
            }
        });

    }
    public void write(String s){
        try (FileOutputStream fos = openFileOutput("memory", Context.MODE_APPEND)){
            fos.write(s.getBytes());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void display(){
        FileInputStream stream = null;
        try {
            stream = openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        StringBuilder phrase = new StringBuilder();

        try (BufferedReader reader = new BufferedReader((inputStreamReader))){
            String line = reader.readLine();
            while(line != null){
                phrase.append(line);
                line = reader.readLine();
            }
            TextView outputText = findViewById(R.id.displayText);
            outputText.setText(phrase.toString());
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}