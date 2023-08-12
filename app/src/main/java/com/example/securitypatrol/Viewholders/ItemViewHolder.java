package com.example.securitypatrol.Viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.R;

public class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView chiriasTextView;
    public TextView locatieTextView;
    public TextView felContorTextView;
    public TextView serieTextView;
    public TextView indexVechiTextView;
    public TextView indexNouTextView;
    public ImageView photoImageView;

    public ItemViewHolder(View itemView) {
        super(itemView);
        chiriasTextView = itemView.findViewById(R.id.chiriasTextView);
        locatieTextView = itemView.findViewById(R.id.locatieTextView);
        felContorTextView = itemView.findViewById(R.id.felContorTextView);
        serieTextView = itemView.findViewById(R.id.serieTextView);
        indexVechiTextView = itemView.findViewById(R.id.indexVechiTextView);
        indexNouTextView = itemView.findViewById(R.id.indexNouTextView);
        photoImageView = itemView.findViewById(R.id.photoImageView);
    }

}
