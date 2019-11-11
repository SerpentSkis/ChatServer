package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String roomName = intent.getStringExtra(MainActivity.ROOM_NAME);
        String userName = intent.getStringExtra(MainActivity.USER_NAME);

        setContentView(R.layout.activity_display_message);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, ItemFragment.newInstance(roomName, userName), ItemFragment.TAG)
                    .commit();
        }

    }


    @Override
    public void onListFragmentInteraction(String item) {
        Log.d("ChatRoomActivity", "touched item: " + item);
    }

}
