package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String ROOM_NAME = "com.example.chatapp.ROOM_NAME";
    public static final String USER_NAME = "com.example.chatapp.USER_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void joinRoom(View view) {
        Intent chatRoomIntent = new Intent(this, ChatRoomActivity.class);
        EditText roomNameEditText = findViewById(R.id.roomName);
        EditText userNameEditText = findViewById(R.id.userName);
        chatRoomIntent.putExtra(MainActivity.ROOM_NAME, roomNameEditText.getText().toString());
        chatRoomIntent.putExtra(MainActivity.USER_NAME, userNameEditText.getText().toString());
        startActivity(chatRoomIntent);
    }
}
