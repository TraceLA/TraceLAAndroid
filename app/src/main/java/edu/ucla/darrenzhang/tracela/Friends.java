package edu.ucla.darrenzhang.tracela;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

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

import java.util.ArrayList;

public class Friends extends AppCompatActivity {
    //Initialize variable
    ListView listView;
    ArrayList<String> stringArrayList = new ArrayList<>();
    ArrayList<String> confirmedArrayList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    String username;
    Boolean isFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest userGetRequest1 = new JsonArrayRequest(Request.Method.GET, Constants.DATABASE_URL+"/friends?reverse=true&confirmed=true&username=" + MainActivity.username, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray userList) {
                        for (int i = 0; i < userList.length(); i++) {
                            JSONObject user = null;
                            try {
                                user = userList.getJSONObject(i);
                                confirmedArrayList.add(user.getString("username_a"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("noFriend", error.toString());
            }
        });

        queue.add(userGetRequest1);

        //Assign variable
        listView = findViewById(R.id.list_view);

        JsonArrayRequest userGetRequest = new JsonArrayRequest(Request.Method.GET, Constants.usersURL, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray userList) {
                        for (int i = 0; i < userList.length(); i++) {
                            JSONObject user = null;
                            try {
                                user = userList.getJSONObject(i);
                                username = user.getString("username");
                                for (int j = 0; j < confirmedArrayList.size(); j++) {
                                    Log.d("user", confirmedArrayList.get(j));
                                    if (confirmedArrayList.get(j).equals(username)) {
                                        isFriend = true;
                                    }
                                }
                                if ((!username.equals(MainActivity.username)) && (!isFriend)) {
                                        stringArrayList.add(username);
                                }
                                //Initialize adapter
                                adapter = new ArrayAdapter<>(Friends.this, android.R.layout.simple_list_item_1, stringArrayList);

                                //Set adapter on list view
                                listView.setAdapter(adapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        //Display click item position in toast
                                      //  Toast.makeText(getApplicationContext(), adapter.getItem(position), Toast.LENGTH_SHORT).show();
                                        Log.d("fr", "on click");
                                        Intent friendReq = new Intent(Friends.this, FriendRequest.class);
                                        friendReq.putExtra("friendUser", stringArrayList.get(position));
                                        finish();
                                        startActivity(friendReq);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("noFriend", error.toString());
            }
        });

        queue.add(userGetRequest);

        Log.d("string contents", stringArrayList.toString());


        //Add item in array list
//        for (int i = 0; i <= 100; i++) {
//            stringArrayList.add("User " + i);
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Initialize menu inflater
        MenuInflater menuInflater = getMenuInflater();
        //Inflate menu
        menuInflater.inflate(R.menu.menu_search, menu);
        //Initialize menu item
        MenuItem menuItem = menu.findItem(R.id.search_view);
        //Initialize search view
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Filter array list
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}