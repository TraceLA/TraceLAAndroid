package edu.ucla.darrenzhang.tracela;
/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Broadcast receiver for the alarm, which delivers the notification.
 */
public class CheckExposureReceiver extends BroadcastReceiver {

    private NotificationManager mNotificationManager;
    // Notification ID.
    private static final int NOTIFICATION_ID = 0;
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary notification channel";
    private static final String NOTIFICATION_MSG = "You've been exposed to someone who tested positive for COVID-19.";
    private Context context;
    private InternalMemory internalMemory;
    private RequestQueue queue;
    private String exposures = "";
    private String username;

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        username = intent.getStringExtra("username");
        queue = Volley.newRequestQueue(context);
        internalMemory = new InternalMemory(context);
        exposures = "";
        Log.d(".CheckExposureReceiver", "Received intent");
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        checkForExposure(); //will deliver notification if there is exposure
    }

    public void checkForExposure(){
        JsonObjectRequest contactsGetRequest = new JsonObjectRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/contacts?username=" + username, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject contacts) {
                        Log.d(".CheckExposureReceiver"," contacts list is: "+contacts.length()+" long");
                        if (contacts.length()>0) {
                            String msg = "You have been exposed to someone who tested positive for COVID-19 on: \n";
                            String[] arr = contacts.toString().split(",");
                            for (int i = 0; i < arr.length; i++) {
                                String userDateKeyValPair = arr[i];
                                int indexOfColon = userDateKeyValPair.indexOf(':');
                                String time = userDateKeyValPair.substring(indexOfColon+2, userDateKeyValPair.indexOf("\"", indexOfColon+2));
                                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy", Locale.ENGLISH);
                                LocalDate date = LocalDate.parse(time, inputFormatter);
                                time = outputFormatter.format(date);
                                msg += time + "\n";
                            }
                            exposures = msg;
                        } else {
                            exposures = "You have not been recently exposed to anyone who has tested positive for COVID-19\n";
                        }
                        checkForInfectionSpots();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".CheckExposureReceiver", "Getting contacts for " + MainActivity.username + ": " + error.toString());
            }
        });
        queue.add(contactsGetRequest);
    }
    public void checkForInfectionSpots()
    {
        JsonArrayRequest spotsGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/spots?username=" + username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray exposureSpots) {
                        Log.d(".CheckExposureReceiver"," exposure spots list is: "+exposureSpots.length()+" long");
                        if (exposureSpots.length() > 0) {
                            exposures += "\nYou may have been exposed to COVID-19 at these potential high-risk locations:\n";
                            for (int i = 0; i < exposureSpots.length(); i++) {
                                try {
                                    exposures+= exposureSpots.getString(i)+"\n";
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (internalMemory.hasNewContacts(exposures)){
                            internalMemory.writeExposureInfoToMemory(exposures);
                            deliverNotification(context, NOTIFICATION_MSG);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".CheckExposureReceiver", "Getting exposure spots for " + MainActivity.username + ": " + error.toString());
            }
        });
        queue.add(spotsGetRequest);
    }
    /**
     * Builds and delivers the notification.
     *
     * @param context, activity context.
     */
    private void deliverNotification(Context context, String msg) {
        // Create the content intent for the notification, which launches
        // this activity
        Intent contentIntent = new Intent(context, NewsPage.class);

        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, NOTIFICATION_ID, contentIntent, PendingIntent
                        .FLAG_UPDATE_CURRENT);
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder
                (context, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("TraceLA COVID-19 Exposure Alert")
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Deliver the notification
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}

