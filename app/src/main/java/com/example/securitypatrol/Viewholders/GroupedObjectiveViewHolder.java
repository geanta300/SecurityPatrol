package com.example.securitypatrol.Viewholders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.R;

public class GroupedObjectiveViewHolder extends RecyclerView.ViewHolder {

    public TextView descriptionTextView;
    public TextView locationTextView;
    public LinearLayout verificationsContainer;

    public GroupedObjectiveViewHolder(View itemView) {
        super(itemView);
        descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        locationTextView = itemView.findViewById(R.id.locationTextView);
        verificationsContainer = itemView.findViewById(R.id.verificationsContainer);
    }
}
