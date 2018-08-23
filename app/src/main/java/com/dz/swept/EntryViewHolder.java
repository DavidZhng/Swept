package com.dz.swept;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EntryViewHolder extends RecyclerView.ViewHolder {

    public TextView tvUserEmail, tvSingleScore, tvScoreScore1, tvScoreScore2,tvWinner, tvTo;


    public EntryViewHolder(View itemView) {
        super(itemView);
        Log.d("Entryviewholder","fourth");

        tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
        tvSingleScore = itemView.findViewById(R.id.tvSingleScore);
        tvScoreScore1 = itemView.findViewById(R.id. tvScoreScore1);
        tvScoreScore2 = itemView.findViewById(R.id.tvScoreScore2);
        tvWinner = itemView.findViewById(R.id.tvWinner);
        tvTo = itemView.findViewById(R.id.tvTo);




    }

}
