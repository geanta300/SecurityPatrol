package com.example.securitypatrol.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Viewholders.ItemViewHolder;
import com.example.securitypatrol.R;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final Cursor cursor;
    Context context;
    DatabaseHelper databaseHelper;
    private final Map<String, Integer> firstUnscannedPosition = new HashMap<>();

    public ItemAdapter(Cursor cursor, Context context) {
        this.cursor = cursor;
        this.context = context;
        buildFirstUnscannedPositions();
    }

    private void buildFirstUnscannedPositions() {
        if (cursor == null) return;
        if (cursor.moveToFirst()) {
            int idxDesc = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV);
            int idxLoc = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATIE);
            int idxScan = cursor.getColumnIndex(DatabaseHelper.COLUMN_SCANAT);
            int position = 0;
            do {
                String description = (idxDesc != -1) ? cursor.getString(idxDesc) : "";
                String location = (idxLoc != -1) ? cursor.getString(idxLoc) : "";
                boolean isScanned = false;
                if (idxScan != -1) {
                    try {
                        isScanned = cursor.getInt(idxScan) == 1;
                    } catch (Exception ignored) {}
                }
                if (!isScanned) {
                    String key = (description != null ? description : "") + "|" + (location != null ? location : "");
                    if (!firstUnscannedPosition.containsKey(key)) {
                        firstUnscannedPosition.put(key, position);
                    }
                }
                position++;
            } while (cursor.moveToNext());
        }
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

        // Hard reset view state to avoid recycled hidden/collapsed items after scroll
        holder.itemView.setVisibility(View.VISIBLE);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
        if (lp != null) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(lp);
        }
        holder.locationTextView.setBackgroundColor(Color.TRANSPARENT);
        holder.locationTextView.setTextColor(Color.BLACK);
        holder.verificationTextView.setVisibility(View.VISIBLE);
        holder.verificationTextView.setBackgroundColor(Color.TRANSPARENT);
        holder.verificationTextView.setTextColor(Color.BLACK);
        holder.raspunsVerificareTextView.setVisibility(View.VISIBLE);

        // Safe indices and value extraction to avoid crashes on missing columns
        int idxDesc = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV);
        int idxLoc = cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATIE);
        int idxVerif = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIERE_VERIFICARI);
        int idxRasp = cursor.getColumnIndex(DatabaseHelper.COLUMN_RASPUNS_VERIFICARE);
        int idxScan = cursor.getColumnIndex(DatabaseHelper.COLUMN_SCANAT);

        String description = (idxDesc != -1) ? cursor.getString(idxDesc) : "";
        String location = (idxLoc != -1) ? cursor.getString(idxLoc) : "";
        String verification = (idxVerif != -1) ? cursor.getString(idxVerif) : "";
        String raspunsVerificare = (idxRasp != -1) ? cursor.getString(idxRasp) : "";

        holder.descriptionTextView.setText(description != null ? description : "");

        boolean isScanned = false;
        if (idxScan != -1) {
            try {
                isScanned = cursor.getInt(idxScan) == 1;
            } catch (Exception ignored) {}
        }

        // Build key by description|location to collapse duplicate unscanned rows from LEFT JOIN
        String key = (description != null ? description : "") + "|" + (location != null ? location : "");
        Integer firstPos = firstUnscannedPosition.get(key);
        if (!isScanned) {
            if (firstPos == null || position != firstPos) {
                holder.itemView.setVisibility(View.GONE);
                // Collapse the item completely to avoid empty gaps
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                if (params != null) {
                    params.width = 0;
                    params.height = 0;
                    holder.itemView.setLayoutParams(params);
                }
                holder.locationTextView.setText("");
                holder.verificationTextView.setVisibility(View.GONE);
                holder.raspunsVerificareTextView.setVisibility(View.GONE);
                return;
            }
            holder.itemView.setVisibility(View.VISIBLE);
            // Reset size in case a recycled view was previously collapsed
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (params != null) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(params);
            }
            holder.locationTextView.setText(holder.itemView.getContext().getString(R.string.location_text, (location != null ? location : "")));
            holder.locationTextView.setBackgroundColor(Color.TRANSPARENT);
            holder.locationTextView.setTextColor(Color.BLACK);

            // Show status message with red background
            holder.verificationTextView.setVisibility(View.VISIBLE);
            holder.verificationTextView.setText(R.string.not_scanned);
            holder.verificationTextView.setBackgroundColor(Color.RED);
            holder.verificationTextView.setTextColor(Color.WHITE);

            holder.raspunsVerificareTextView.setVisibility(View.GONE);
            holder.raspunsVerificareTextView.setText("");
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            // Reset size in case a recycled view was previously collapsed
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            if (params != null) {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemView.setLayoutParams(params);
            }
            holder.locationTextView.setText(holder.itemView.getContext().getString(R.string.location_text, (location != null ? location : "")));
            holder.locationTextView.setBackgroundColor(Color.TRANSPARENT);
            holder.locationTextView.setTextColor(Color.BLACK);

            holder.verificationTextView.setVisibility(View.VISIBLE);
            holder.raspunsVerificareTextView.setVisibility(View.VISIBLE);

            holder.verificationTextView.setText(holder.itemView.getContext().getString(R.string.verification_text, (verification != null ? verification : "")));
            if (raspunsVerificare == null) {
                raspunsVerificare = "";
            }
            holder.raspunsVerificareTextView.setText(holder.itemView.getContext().getString(R.string.raspuns_verificare_text, (raspunsVerificare != null ? raspunsVerificare : "")));
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
