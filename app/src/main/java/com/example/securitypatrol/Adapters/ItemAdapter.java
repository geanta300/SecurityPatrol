package com.example.securitypatrol.Adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Viewholders.ItemViewHolder;
import com.example.securitypatrol.R;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder>{
    private final Cursor cursor;

    public ItemAdapter(Cursor cursor) {
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
        String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
        String datatime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATATIME));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRPIPTION));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
        String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));

        // Format the data and set it in the TextViews using string resources with placeholders
        String userNameText = "Nume: " + userName;
        String datatimeText = "Data si ora: " + datatime;
        String descriptionText = "Descriere: " + description;
        String locationText = "Locatie: " + location;

        holder.userNameTextView.setText(userNameText);
        holder.datatimeTextView.setText(datatimeText);
        holder.descriptionTextView.setText(descriptionText);
        holder.locationTextView.setText(locationText);

        if (!Objects.equals(photoUri, "")) {
            holder.photoImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(photoUri).into(holder.photoImageView);
        } else {
            holder.photoImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
