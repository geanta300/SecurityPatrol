package com.example.counterreader.Viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.R;

public class SquareViewHolder extends RecyclerView.ViewHolder {

    public TextView textTitle;
    public TextView textFileName1;
    public TextView textFileName2;
    public Button buttonShareFiles;

    public SquareViewHolder(@NonNull View itemView) {
        super(itemView);
        textTitle = itemView.findViewById(R.id.textTitle);
        textFileName1 = itemView.findViewById(R.id.textFileName1);
        textFileName2 = itemView.findViewById(R.id.textFileName2);
        buttonShareFiles = itemView.findViewById(R.id.buttonShareFiles);
    }
}
