package com.example.counterreader.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.Models.SquareItem;
import com.example.counterreader.R;
import com.example.counterreader.Viewholders.SquareViewHolder;

import java.util.List;

public class SquareAdapter extends RecyclerView.Adapter<SquareViewHolder> {
    private final List<SquareItem> squareItemList;

    public SquareAdapter(List<SquareItem> squareItemList) {
        this.squareItemList = squareItemList;
    }

    @NonNull
    @Override
    public SquareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_square, parent, false);
        return new SquareViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SquareViewHolder holder, int position) {
        SquareItem squareItem = squareItemList.get(position);
        holder.textTitle.setText(squareItem.getSquareTitle());
        holder.textFileName1.setText(squareItem.getFileName1());
        holder.textFileName2.setText(squareItem.getFileName2());

        holder.buttonShareFiles.setOnClickListener(v -> {
            // Handle share button click here
        });
    }

    @Override
    public int getItemCount() {
        return squareItemList.size();
    }
}
