package com.dz.swept;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

public class StakeViewHolder extends RecyclerView.ViewHolder {

    public TextView tvStakeName, tvFormat, tvCreator, tvParticipants, tvStatus, tvReward;
    public Button btnEnterStake,btnConfigStake, btnView;


    public StakeViewHolder (View itemView) {
        super(itemView);
        Log.d("Stakeviewholder","fourth");

        tvStakeName = itemView.findViewById(R.id.tvStakeName);
        tvFormat = itemView.findViewById(R.id. tvFormat);
        tvCreator = itemView.findViewById(R.id.tvCreator);
        tvParticipants = itemView.findViewById(R.id.tvParticipants);
        tvStatus = itemView.findViewById(R.id.tvStatus);
        tvReward = itemView.findViewById(R.id.tvReward);
        btnEnterStake = itemView.findViewById(R.id.btnEnterStake);
        btnConfigStake = itemView.findViewById(R.id.btnConfigStake);
        btnView = itemView.findViewById(R.id.btnView);






    }

}
