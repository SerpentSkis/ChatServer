package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String ROOM_NAME = "com.example.chatapp.ROOM_NAME";

    public static String roomNameString;
    public static String userNameString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * Called when the user taps the Join Room button
     */
    public void joinRoom(View view) {
        Intent chatRoomIntent = new Intent(this, ChatRoom.class);
        EditText roomNameEditText = findViewById(R.id.roomName);
        EditText userNameEditText = findViewById(R.id.userName);

        roomNameString = roomNameEditText.getText().toString();
        userNameString = userNameEditText.getText().toString();


        chatRoomIntent.putExtra(MainActivity.ROOM_NAME, roomNameString);
        startActivity(chatRoomIntent);


    }


}
