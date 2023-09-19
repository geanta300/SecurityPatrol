package com.example.securitypatrol.Adapters;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.FileShareHelper;
import com.example.securitypatrol.Models.SquareItem;
import com.example.securitypatrol.R;
import com.example.securitypatrol.Viewholders.SquareViewHolder;

import java.util.List;

public class SquareAdapter extends RecyclerView.Adapter<SquareViewHolder> {
    private final List<SquareItem> squareItemList;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;

    public SquareAdapter(List<SquareItem> squareItemList) {
        this.squareItemList = squareItemList;
    }

    FileShareHelper fileShareHelper;

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
        holder.buttonShareFiles.setOnClickListener(v -> {
            fileShareHelper = new FileShareHelper(holder.itemView.getContext(), directoryPathOfFiles, squareItem.getFileName1());
            fileShareHelper.shareFiles();
        });
    }

    @Override
    public int getItemCount() {
        return squareItemList.size();
    }
}
