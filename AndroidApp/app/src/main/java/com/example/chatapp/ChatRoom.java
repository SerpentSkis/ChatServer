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
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class ChatRoom extends AppCompatActivity {

    //TODO set server up, crashes with app
    private static final String SERVER = "ws://10.0.2.2:8080";
    ArrayList<String> messageList = new ArrayList<>(1);
    ListView listView;
    ArrayAdapter<String> stringArrayAdapter;
    private WebSocket ws;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String roomName = intent.getStringExtra(MainActivity.ROOM_NAME);

        // Capture the layout's TextView and set the string as its text
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
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ws = connect();
                    ws.sendText("join" + " " + MainActivity.roomNameString);



                } catch (IOException | WebSocketException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();

    }

    /**
     * Connect to the server.
     */
    private WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {
                    public void onTextMessage(WebSocket websocket, String messageJSON) throws JSONException {

                        JSONObject reader = new JSONObject(messageJSON);
                        messageList.add(reader.getString("user") + ": " + reader.getString("message"));
                        //TODO get viewBox to update properly
                        stringArrayAdapter.notifyDataSetChanged();
                        listView.setSelection(stringArrayAdapter.getCount() - 1);


                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect();
    }


    public void sendMessage(View view) {

        EditText enterMessageEditText = findViewById(R.id.enterMessage);
        String enterMessageString = enterMessageEditText.getText().toString();
        ws.sendText(MainActivity.userNameString + " " + enterMessageString);
        stringArrayAdapter.notifyDataSetChanged();
        listView.setSelection(stringArrayAdapter.getCount() - 1);


    }


}
