package com.example.securitypatrol.Viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView descriptionTextView;
    public TextView locationTextView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        locationTextView = itemView.findViewById(R.id.locationTextView);
    }

}
