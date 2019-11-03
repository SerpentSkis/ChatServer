package com.example.springChatServer;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Room {
    //TODO return less info
    static Map<String, Room> roomMap = new HashMap<>();
    ArrayList<TextMessage> fullMessageList = new ArrayList<>(10);
    String roomName;
    ArrayList<WebSocketSession> userListConnections = new ArrayList<>(10);

    public Room(String roomName) {
        this.roomName = roomName;
    }

    synchronized static void postMessages(ArrayList<TextMessage> messageList, String roomName) throws IOException {
        for (WebSocketSession wss : roomMap.get(roomName).userListConnections) {
            for (TextMessage message : messageList) {
                wss.sendMessage(message);
            }
        }
    }

    void joinRoom(WebSocketSession user) throws IOException {
        userListConnections.add(user);
        for (TextMessage message : fullMessageList) {
            user.sendMessage(message);
        }
    }
}