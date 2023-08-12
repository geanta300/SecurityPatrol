package com.example.securitypatrol.Viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.R;

public class SquareViewHolder extends RecyclerView.ViewHolder {

    public TextView textTitle;
    public TextView textFileName1;
    public TextView textFileName2;
    public ImageButton buttonShareFiles;

    public SquareViewHolder(@NonNull View itemView) {
        super(itemView);
        textTitle = itemView.findViewById(R.id.textTitle);
        textFileName1 = itemView.findViewById(R.id.textFileName1);
        textFileName2 = itemView.findViewById(R.id.textFileName2);
        buttonShareFiles = itemView.findViewById(R.id.buttonShareFiles);
    }
}
