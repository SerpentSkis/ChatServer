package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class ChatRoomActivity extends AppCompatActivity {

    private static final String SERVER = "ws://10.0.2.2:8080";
    ArrayList<String> messageList = new ArrayList<>(8);
    //TODO use recyclerView
    ListView listView;
    ArrayAdapter<String> stringArrayAdapter;
    private WebSocket ws;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String roomName = intent.getStringExtra(MainActivity.ROOM_NAME);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        TextView roomNameTextView = findViewById(R.id.roomName);
        roomNameTextView.setText(roomName);

        stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);
        listView = findViewById(R.id.chatBox);
        listView.setAdapter(stringArrayAdapter);
        setUpWebSocket();
        stringArrayAdapter.setNotifyOnChange(true);
        listView.setSelection(stringArrayAdapter.getCount() - 1);
    }

    void setUpWebSocket() {
        Intent intent = getIntent();
        final String roomName = intent.getStringExtra(MainActivity.ROOM_NAME);
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ws = connect();
                    ws.sendText("join" + " " + roomName);
                } catch (IOException | WebSocketException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {
                    public void onTextMessage(WebSocket websocket, String messageJSON) throws JSONException {
                        JSONObject reader = new JSONObject(messageJSON);
                        messageList.add(reader.getString("user") + ": " + reader.getString("message"));
                        stringArrayAdapter.notifyDataSetChanged();
                        listView.setSelection(stringArrayAdapter.getCount() - 1);
                    }
                })
                .connect();
    }

    public void sendMessage(View view) {
        Intent intent = getIntent();
        String userName = intent.getStringExtra(MainActivity.USER_NAME);
        EditText enterMessageEditText = findViewById(R.id.enterMessage);
        String enterMessageString = enterMessageEditText.getText().toString();
        ws.sendText(userName + " " + enterMessageString);
        stringArrayAdapter.notifyDataSetChanged();
        listView.setSelection(stringArrayAdapter.getCount() - 1);
    }
}
