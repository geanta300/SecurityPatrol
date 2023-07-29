package com.example.counterreader.Viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.R;

public class CountersLeftViewHolder extends RecyclerView.ViewHolder {

    public TextView chiriasTextView;
    public TextView locatieTextView;
    public TextView felContorTextView;
    public TextView serieTextView;
    public View conturLine;

    public CountersLeftViewHolder(View itemView) {
        super(itemView);
        chiriasTextView = itemView.findViewById(R.id.chiriasTextView);
        locatieTextView = itemView.findViewById(R.id.locatieTextView);
        felContorTextView = itemView.findViewById(R.id.felContorTextView);
        serieTextView = itemView.findViewById(R.id.serieTextView);
        conturLine = itemView.findViewById(R.id.conturLine);
        conturLine.setVisibility(View.GONE);

    }
}