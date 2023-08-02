package com.example.counterreader.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.Helpers.FileShareHelper;
import com.example.counterreader.Models.SquareItem;
import com.example.counterreader.R;
import com.example.counterreader.Viewholders.SquareViewHolder;
import com.itextpdf.io.exceptions.IOException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SquareAdapter extends RecyclerView.Adapter<SquareViewHolder> {
    private final List<SquareItem> squareItemList;

    private final String directoryPathOfFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/CounterReader";

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
        holder.textFileName2.setText(squareItem.getFileName2());
        holder.buttonShareFiles.setOnClickListener(v -> {
            fileShareHelper = new FileShareHelper(holder.itemView.getContext(), directoryPathOfFiles, squareItem.getFileName1(), squareItem.getFileName2());
            fileShareHelper.shareFiles();
        });
    }

    @Override
    public int getItemCount() {
        return squareItemList.size();
    }
}
