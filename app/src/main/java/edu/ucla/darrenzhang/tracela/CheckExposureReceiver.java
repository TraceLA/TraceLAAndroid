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
    private Context context;

    /**
     * Called when the BroadcastReceiver receives an Intent broadcast.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.d(".CheckExposureReceiver", "Received intent");
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        checkForExposure(); //will deliver notification if there is exposure
    }

    public void checkForExposure() {
        //TODO: this just checks for contacts right now, need to update to check for exposure once backend adds that
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest coordGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL + "/exposure/contacts?username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONArray contacts) {
                        Log.d(".CheckExposureReceiver", " contacts list is: " + contacts.length() + " long");
                        if (contacts.length() > 0) {
                            String msg = "You have been exposed to someone who tested positive for COVID-19 on: \n";
                            for (int i = 0; i < contacts.length(); i++) {
                                try {
                                    JSONObject contact = contacts.getJSONObject(contacts.length() - 1);
                                    String location = contact.getString("location");
                                    String time = contact.getString("date");
                                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
                                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH);
                                    LocalDate date = LocalDate.parse(time, inputFormatter);
                                    time = outputFormatter.format(date);
                                    msg += time + " at " + location;
                                    Log.d(".CheckExposureReceiver", MainActivity.username + " was exposed to " + contact.getString("other_username") + " at " + location + ": " + time);
                                } catch (JSONException e) {
                                    Log.d(".CheckExposureReceiver", "error processing contacts data: " + e.toString());
                                }
                            }
                            Log.d(".CheckExposureReceiver", "delivering notification");
                            deliverNotification(context, "You've been exposed to someone who tested positive for COVID-19.");
//                            MainActivty.
//                            content.setText(msg);
                        } else {
                            Log.d(".CheckExposureReceiver", "Checked backend and the user has no exposure");
//                            deliverNotification(context, "No Exposure");
//                            content.setText("You have not been recently exposed to anyone who has tested positive for COVID-19");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".CheckExposureReceiver", "Getting contacts for " + MainActivity.username + ": " + error.toString());
                if (error.toString().equals("com.android.volley.AuthFailureError")) {
                    updateApiKey();
                }
            }
        });
        queue.add(coordGetRequest);
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
//        String exposedMsg = "You've been exposed to someone who tested positive for COVID-19.";
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

    private void updateApiKey() {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = Constants.DATABASE_URL + "/userLogin/?username=" + MainActivity.username + "&password=" + MainActivity.password;
        Log.d(".CheckExposureReceiver", "Login Post Attempt: " + MainActivity.username + ", " + MainActivity.password);

        StringRequest userPOSTRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int start = response.indexOf(":") + 2;
                        int end = response.indexOf("\"", start);
                        response = response.substring(start, end);
                        MainActivity.api_key = response;

                        Log.d("SUCCESS: ", MainActivity.api_key + MainActivity.username);
//                        writeCredentialsToMemory();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(".CheckExposureReceiver", "wrong Username/pword" + error.toString());
            }
        });

        queue.add(userPOSTRequest);
    }
}

