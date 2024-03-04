package com.example.securitypatrol.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Viewholders.ItemViewHolder;
import com.example.securitypatrol.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final Cursor cursor;
    Context context;
    DatabaseHelper databaseHelper;

    public ItemAdapter(Cursor cursor, Context context) {
        this.cursor = cursor;
        this.context = context;
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        databaseHelper = new DatabaseHelper(context);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
        String verification = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIERE_VERIFICARI));
        String raspunsVerificare = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RASPUNS_VERIFICARE));


        String descriptionText = description;
        String locationText = "Locatie: " + location;
        String verificationText = "Verificare: " + verification;
        if (raspunsVerificare == null) {
            raspunsVerificare = "";
        }
        String raspunsVerificareText = "Raspuns verificare: " + raspunsVerificare;

        holder.descriptionTextView.setText(descriptionText);
        holder.locationTextView.setText(locationText);
        holder.verificationTextView.setText(verificationText);
        holder.raspunsVerificareTextView.setText(raspunsVerificareText);

    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
