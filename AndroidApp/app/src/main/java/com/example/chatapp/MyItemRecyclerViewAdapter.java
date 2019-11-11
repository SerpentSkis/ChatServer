package com.example.chatapp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.ItemFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;
import java.util.List;

/**
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<String> messages;
    private final OnListFragmentInteractionListener listener;


    /**
     * @param listener
     */
    MyItemRecyclerViewAdapter(OnListFragmentInteractionListener listener) {
        this.messages = new ArrayList<>();
        this.listener = listener;
    }

    /**
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.d("adapter", "binded item: " + position);
        holder.item = messages.get(position);
        holder.contentView.setText(messages.get(position));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder.item);
                }
            }
        });
    }

    /**
     * @return Item count.
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * adds item to messages notifies.
     *
     * @param item item to be added to messages
     */
    void addItem(String item) {
        messages.add(item);
        notifyItemInserted(messages.size());
    }


    /**
     * Holds the view
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView contentView;
        String item;

        /**
         * @param view
         */
        ViewHolder(View view) {
            super(view);
            this.view = view;
            this.contentView = view.findViewById(R.id.messageText);
        }
    }
}
