package com.dz.swept;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class EntryAdapter extends RecyclerView.Adapter<EntryViewHolder> {

    MainActivity mainActivity;
    ViewEntriesActivity viewEntriesActivity;
    ArrayList<Entry> entryArrayList;
    SwipeRefreshLayout swipe;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    View view;


    public EntryAdapter(ViewEntriesActivity viewEntriesActivity, ArrayList<Entry> entryArrayList, SwipeRefreshLayout swipe) {
        this.viewEntriesActivity = viewEntriesActivity;
        this.entryArrayList = entryArrayList;
        this.swipe = swipe;

    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        Log.d("Entryadapter","first");

        LayoutInflater layoutInflater = LayoutInflater.from(viewEntriesActivity.getBaseContext());
        view = layoutInflater.inflate(R.layout.activity_user_entry,parent,false);



        return new EntryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final EntryViewHolder holder, final int position) {

        if(entryArrayList.get(position).getStringformat().equals("formatSingleScore")){
            holder.tvSingleScore.setVisibility(View.VISIBLE);
            holder.tvScoreScore1.setVisibility(View.GONE);
            holder.tvScoreScore2.setVisibility(View.GONE);
            holder.tvTo.setVisibility(View.GONE);
            holder.tvUserEmail.setText(entryArrayList.get(position).getStringUserEmail());
            holder.tvSingleScore.setText(entryArrayList.get(position).getStringSingleScore());
            holder.tvWinner.setText(entryArrayList.get(position).getStringWinner());
        }
        if(entryArrayList.get(position).getStringformat().equals("formatScoreVsScore")){
            holder.tvSingleScore.setVisibility(View.GONE);
            holder.tvScoreScore1.setVisibility(View.VISIBLE);
            holder.tvScoreScore2.setVisibility(View.VISIBLE);
            holder.tvTo.setVisibility(View.VISIBLE);
            holder.tvUserEmail.setText(entryArrayList.get(position).getStringUserEmail());
            holder.tvScoreScore1.setText(entryArrayList.get(position).getStringScoreScore1());
            holder.tvScoreScore2.setText(entryArrayList.get(position).getStringScoreScore2());
            holder.tvWinner.setText(entryArrayList.get(position).getStringWinner());
        }
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try{

                    viewEntriesActivity.loadDataFromFireStore();
                    swipe.setRefreshing(false);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }
        });





    }


    @Override
    public int getItemCount() {
        System.out.println("we 3");

        Log.d("Entryadapter","third");
        return entryArrayList.size();


    }




}
