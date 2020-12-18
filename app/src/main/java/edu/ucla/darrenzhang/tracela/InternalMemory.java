package edu.ucla.darrenzhang.tracela;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class InternalMemory {
    private Context context;
    private ArrayList<String> linesFromMemory = new ArrayList<>();
    public static final int LINE_OF_USERNAME = 0;
    public static final int LINE_OF_PASSWORD = 1;
    public static final int LINE_OF_API_KEY = 2;
    public static final int LINE_OF_LOCATION_SHARING = 3;
    public static final int LINE_OF_SHARING_WITH_FRIENDS = 4;

    //line 0 is username
    //line 1 is password
    //line 2 is api_key
    //line 3 is sendingLocationState (true or false)
    //line 4 is sharingWithFriendsState (true or false)
    //line 5 and beyond is pass contacts and spots

    public InternalMemory(Context context) {
        this.context = context;
        updateLinesFromMemory();
    }

    public void writeExposureInfoToMemory(String exposureInfo) {
        String info = "";
        for (int i = 0; i <= LINE_OF_SHARING_WITH_FRIENDS; i++) {
            if (i >= linesFromMemory.size() && (i == LINE_OF_LOCATION_SHARING || i == LINE_OF_SHARING_WITH_FRIENDS)){
                info += "true\n";
            } else {
                info += linesFromMemory.get(i) + '\n';
            }
        }
        info += exposureInfo;
        writeToInternalMemory(info);
        updateLinesFromMemory();
    }

    public boolean hasNewContacts(String newExposures) {
        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {

            for (int i = 0; i < LINE_OF_SHARING_WITH_FRIENDS + 1; i++) {
                reader.readLine();
            }
            String[] newLines = newExposures.split("\n");
            for (int i = 0; i < newLines.length; i++) {
                String line = reader.readLine();
                Log.d(".InternalMemory", "the line is " + line);
                if (line == null) {     //if memory has less contacts than new
                    return true;
                }
                if (!line.equals(newLines[i])) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateInternalMemoryToggleState(boolean sending) {
        String info = "";
        for (int i = 0; i < linesFromMemory.size() && i < LINE_OF_LOCATION_SHARING; i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        info += sending + "\n";
        for (int i = 0; i < linesFromMemory.size(); i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        writeToInternalMemory(info);
    }

    public void updateInternalMemorySharingPermissions(boolean sharing) {
        String info = "";
        for (int i = 0; i < linesFromMemory.size() && i < LINE_OF_SHARING_WITH_FRIENDS; i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        info += sharing + "\n";
        for (int i = 0; i < linesFromMemory.size(); i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        writeToInternalMemory(info);
    }

    public void updateUsername(String username) {
        String info = "";
        for (int i = 0; i < linesFromMemory.size() && i < LINE_OF_USERNAME; i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        info += username + "\n";
        for (int i = 0; i < linesFromMemory.size(); i++) {
            info += linesFromMemory.get(i) + '\n';
        }
        writeToInternalMemory(info);
    }

    public boolean getSharingPermissionsFromMemory() {
        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {
            for (int i = 0; i < LINE_OF_SHARING_WITH_FRIENDS; i++) {
                reader.readLine();
            }
            String line = reader.readLine();
            if (line == null || line.length() == 0) {
                putToArrayList("true", LINE_OF_SHARING_WITH_FRIENDS);
                return true;
            } else {
                return line.equals("true");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean getSendingBoolFromMemory() {
        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {
            for (int i = 0; i < LINE_OF_LOCATION_SHARING; i++) {
                reader.readLine();
            }
            String line = reader.readLine();
            if (line == null || line.length() == 0) {
                putToArrayList("true", LINE_OF_LOCATION_SHARING);
                return true;
            } else {
                return line.equals("true");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void updateLinesFromMemory() {
        //sets linesFromMemory to match internal memory
        try (FileOutputStream fos = context.openFileOutput("memory", Context.MODE_APPEND)) {    //this creates a "memory" file in case it was never created before
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {
            linesFromMemory = new ArrayList<>();
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) break;
                linesFromMemory.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean internalMemoryIsEmpty() {
        try (FileOutputStream fos = context.openFileOutput("memory", Context.MODE_APPEND)) {    //this creates a "memory" file in case it was never created before

        } catch (IOException e) {
            e.printStackTrace();
        }

        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {
            String line = reader.readLine();
            if (line == null || line.length() == 0) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setCredentialsOfMainActivity() {
        FileInputStream stream = null;
        try {
            stream = context.openFileInput("memory");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
        try (BufferedReader reader = new BufferedReader((inputStreamReader))) {
            MainActivity.username = reader.readLine();
            MainActivity.password = reader.readLine();
            MainActivity.api_key = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToInternalMemory(String s) {
        try (FileOutputStream fos = context.openFileOutput("memory", Context.MODE_PRIVATE)) {
            fos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putToArrayList(String msg, int line) {
        if (line < linesFromMemory.size()) {
            linesFromMemory.set(line, msg);
        } else {
            while (linesFromMemory.size() < line) {
                linesFromMemory.add("");
            }
            linesFromMemory.add(msg);
        }
    }
}
