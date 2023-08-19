package com.example.securitypatrol.Viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView userNameTextView;
    public TextView datatimeTextView;
    public TextView descriptionTextView;
    public TextView locationTextView;
    public ImageView photoImageView;
    public TextView optionalCommentTextView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        userNameTextView = itemView.findViewById(R.id.userNameTextView);
        datatimeTextView = itemView.findViewById(R.id.datatimeTextView);
        descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        locationTextView = itemView.findViewById(R.id.locationTextView);
        photoImageView = itemView.findViewById(R.id.photoImageView);
        optionalCommentTextView = itemView.findViewById(R.id.optionalCommentTextView);
    }

}
