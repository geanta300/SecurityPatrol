package com.example.counterreader.Adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.Viewholders.CountersLeftViewHolder;
import com.example.counterreader.Helpers.DatabaseHelper;
import com.example.counterreader.R;

public class CountersLeftAdapter extends RecyclerView.Adapter<CountersLeftViewHolder>{
    private final Cursor cursor;

    public CountersLeftAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public CountersLeftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new CountersLeftViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CountersLeftViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        // Extract the data from the cursor for the current position
        String chirias = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHIRIAS));
        String locatie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
        String felContor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEL_CONTOR));
        String serie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERIE));

        // Format the data and set it in the TextViews using string resources with placeholders
        String chiriasText = holder.itemView.getContext().getString(R.string.chirias_text, chirias);
        String locatieText = holder.itemView.getContext().getString(R.string.locatie_text, locatie);
        String felContorText = holder.itemView.getContext().getString(R.string.fel_contor_text, felContor);
        String serieText = holder.itemView.getContext().getString(R.string.serie_text, serie);

        holder.chiriasTextView.setText(chiriasText);
        holder.locatieTextView.setText(locatieText);
        holder.felContorTextView.setText(felContorText);
        holder.serieTextView.setText(serieText);
    }

    @Override
    public int getItemCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }
}
