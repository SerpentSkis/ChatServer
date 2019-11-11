package com.example.chatapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.io.IOException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment {
    static final String TAG = ItemFragment.class.getName();
    private static final String SERVER = "ws://10.0.2.2:8080";
    private static final String ARG_ROOM_NAME = "room-name";
    private static final String ARG_USER_NAME = "user-name";
    private final Gson gson = new Gson();
    private WebSocket ws;
    private String roomName;
    private String userName;
    private OnListFragmentInteractionListener listener;
    private MyItemRecyclerViewAdapter myItemRecyclerViewAdapter;
    private EditText enterMessageEditText;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    /**
     * @param roomName
     * @param userName
     * @return
     */
    static ItemFragment newInstance(String roomName, String userName) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_NAME, roomName);
        args.putString(ARG_USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userName = getArguments().getString(ARG_USER_NAME);
            roomName = getArguments().getString(ARG_ROOM_NAME);
        }
    }

    /**
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.list);

//         Set the adapter
        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(myItemRecyclerViewAdapter);

        TextView roomNameTextView = view.findViewById(R.id.roomName);
        roomNameTextView.setText(roomName);
        enterMessageEditText = view.findViewById(R.id.enterMessage);
        setUpWebSocket();

        view.findViewById(R.id.sendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        return view;
    }


    /**
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            listener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
        myItemRecyclerViewAdapter = new MyItemRecyclerViewAdapter(listener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void setUpWebSocket() {

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ws = connect();
                    ws.sendText("join " + roomName);
                } catch (IOException | WebSocketException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * @return connected web socket
     * @throws IOException
     * @throws WebSocketException
     */
    private WebSocket connect() throws IOException, WebSocketException {
        return new WebSocketFactory()
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {
                    public void onTextMessage(WebSocket websocket, final String messageJSON) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("ItemFragment", "item added: " + messageJSON);
                                    ChatMessageModel chatMessageModel = gson.fromJson(messageJSON, ChatMessageModel.class);
                                    myItemRecyclerViewAdapter.addItem(chatMessageModel.user + ": " + chatMessageModel.message);
                                    //TODO get list to auto scroll on new messages.
                                }
                            });
                        }
                    }
                })
                .connect();
    }

    private void sendMessage() {
        String enterMessageString = enterMessageEditText.getText().toString();
        ws.sendText(userName + " " + enterMessageString);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(String item);
    }

    public class ChatMessageModel {
        String user, message;
    }

}
