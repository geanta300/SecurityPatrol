package com.example.securitypatrol.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Models.ObjectiveModel;
import com.example.securitypatrol.Models.VerificationModel;
import com.example.securitypatrol.R;
import com.example.securitypatrol.Viewholders.GroupedObjectiveViewHolder;

import java.util.List;

public class GroupedObjectiveAdapter extends RecyclerView.Adapter<GroupedObjectiveViewHolder> {

    private final Context context;
    private final List<ObjectiveModel> objectives;
    private final DatabaseHelper databaseHelper;

    public GroupedObjectiveAdapter(Context context, List<ObjectiveModel> objectives, DatabaseHelper databaseHelper) {
        this.context = context;
        this.objectives = objectives;
        this.databaseHelper = databaseHelper;
    }

    @NonNull
    @Override
    public GroupedObjectiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_objective_group, parent, false);
        return new GroupedObjectiveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupedObjectiveViewHolder holder, int position) {
        ObjectiveModel objective = objectives.get(position);
        String description = objective.getDescriere() != null ? objective.getDescriere() : "";
        String location = objective.getLocatie() != null ? objective.getLocatie() : "";

        holder.descriptionTextView.setText(description);
        holder.locationTextView.setText(holder.itemView.getContext().getString(R.string.location_text, location));

        // Clear old children when view is recycled
        holder.verificationsContainer.removeAllViews();

        boolean isScanned = databaseHelper.isObjectiveScanned(objective.getUniqueId());
        if (!isScanned) {
            // Show a single red label "Nu a fost scanat"
            TextView notScanned = new TextView(context);
            notScanned.setText(R.string.not_scanned);
            notScanned.setTextColor(Color.WHITE);
            notScanned.setBackgroundColor(Color.RED);
            int pad = (int) (holder.itemView.getResources().getDisplayMetrics().density * 8);
            notScanned.setPadding(pad, pad, pad, pad);
            holder.verificationsContainer.addView(notScanned);
            return;
        }

        // Add all verifications + answers
        List<VerificationModel> verifications = databaseHelper.getVerificationsByObjectiveId(objective.getUniqueId());
        LayoutInflater inflater = LayoutInflater.from(context);
        for (VerificationModel v : verifications) {
            View row = inflater.inflate(R.layout.item_verification_row, holder.verificationsContainer, false);

            TextView verifTv = row.findViewById(R.id.verificationTextView);
            TextView raspunsTv = row.findViewById(R.id.raspunsVerificareTextView);

            String verifText = v.getDescriereVerificare() != null ? v.getDescriereVerificare() : "";
            String raspunsText = v.getRaspunsVerificare() != null ? v.getRaspunsVerificare() : "";

            verifTv.setText(holder.itemView.getContext().getString(R.string.verification_text, verifText));
            raspunsTv.setText(holder.itemView.getContext().getString(R.string.raspuns_verificare_text, raspunsText));

            // Add spacing between rows
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (holder.itemView.getResources().getDisplayMetrics().density * 6);
            row.setLayoutParams(lp);

            holder.verificationsContainer.addView(row);
        }
    }

    @Override
    public int getItemCount() {
        return objectives != null ? objectives.size() : 0;
    }
}
