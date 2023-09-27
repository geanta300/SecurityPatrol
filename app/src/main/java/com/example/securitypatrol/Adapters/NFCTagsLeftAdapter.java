package com.example.securitypatrol.Adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.R;
import com.example.securitypatrol.Viewholders.ItemViewHolder;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class NFCTagsLeftAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final Cursor cursor;

    public NFCTagsLeftAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // Extract the data from the cursor for the current position
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRPIPTION));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));

        // Format the data and set it in the TextViews using string resources with placeholders
        String descriptionText = "Descriere: " + description;
        String locationText = "Locatie: " + location;

        holder.descriptionTextView.setText(descriptionText);
        holder.locationTextView.setText(locationText);

        holder.optionalCommentTextView.setVisibility(View.GONE);
        holder.photoImageView.setVisibility(View.GONE);
        holder.userNameTextView.setVisibility(View.GONE);
        holder.datatimeTextView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
