package com.example.springChatServer;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;

public class WSHandler extends TextWebSocketHandler {
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {

        String[] splitMessage = message.getPayload().split(" ", 2);
        if (splitMessage[0].equals("join")) {
            String roomName = splitMessage[1].split(" ")[0];
            if (!Room.roomMap.containsKey(roomName)) {
                Room.roomMap.put(roomName, new Room(roomName));
            }
            Room room = Room.roomMap.get(roomName);
            room.joinRoom(session);
        } else {

            //TODO use collections search?

            final Room[] userRoom = new Room[1];
            Room.roomMap.forEach((s, room) -> {
                if (room.userListConnections.contains(session)) {
                    userRoom[0] = room;
                }
            });

            if (splitMessage[0].equals("close")) {
                //TODO handle users leaving
                //TODO use GSON
                //TODO add features?
                userRoom[0].userListConnections.remove(session);
            } else {
                ArrayList<TextMessage> textMessages = new ArrayList<>(1);
                textMessages.add(message);
                userRoom[0].fullMessageList.add(message);
                Room.postMessages(textMessages, userRoom[0].roomName);
            }

        }

    }
}