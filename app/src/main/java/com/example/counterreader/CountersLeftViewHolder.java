package com.example.counterreader;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CountersLeftViewHolder extends RecyclerView.ViewHolder {

    public TextView chiriasTextView;
    public TextView locatieTextView;
    public TextView felContorTextView;
    public TextView serieTextView;

    public CountersLeftViewHolder(View itemView) {
        super(itemView);
        chiriasTextView = itemView.findViewById(R.id.chiriasTextViewa);
        locatieTextView = itemView.findViewById(R.id.locatieTextViewa);
        felContorTextView = itemView.findViewById(R.id.felContorTextViewa);
        serieTextView = itemView.findViewById(R.id.serieTextViewa);
    }
}