package edu.ucla.darrenzhang.tracela;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class FriendGeneral extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_gen);
    }

    public void onClickPending(View view) {
        Intent intent = new Intent(this, ViewFriend.class);
        startActivity(intent);
    }

    public void onClickViewFriends(View view) {
        Intent intent = new Intent(this, ViewConfirmed.class);
        startActivity(intent);
    }

    public void onClickFindFriends(View view) {
        Intent intent = new Intent(this, Friends.class);
        startActivity(intent);
    }
}
