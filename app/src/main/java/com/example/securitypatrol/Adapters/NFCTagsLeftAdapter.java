package com.example.securitypatrol.Adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Services.DatabaseStructure;
import com.example.securitypatrol.R;
import com.example.securitypatrol.Viewholders.ItemViewHolder;

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

        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseStructure.COLUMN_DESCRIERE_OBIECTIV));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseStructure.COLUMN_LOCATIE));

        String descriptionText = "Obiectiv: " + description;
        String locationText = "Locatie: " + location;

        holder.descriptionTextView.setText(descriptionText);
        holder.locationTextView.setText(locationText);
        holder.raspunsVerificareTextView.setVisibility(View.GONE);
        holder.verificationTextView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
