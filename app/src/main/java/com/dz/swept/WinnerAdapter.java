package com.dz.swept;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class WinnerAdapter extends RecyclerView.Adapter<EntryViewHolder> {


    WinnerActivity winnerActivity;
    ArrayList<Entry> entryArrayList;
    SwipeRefreshLayout swipe;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    View view;
    public ArrayList<String> alUsers;
    public ArrayList<Integer> alScore;
    public ArrayList<Integer> alScore1;
    public ArrayList<Integer> alScore2;


    public WinnerAdapter(WinnerActivity winnerActivity, ArrayList<Entry> entryArrayList) {
        this.winnerActivity= winnerActivity;
        this.entryArrayList = entryArrayList;


    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        System.out.println("attempted");

        Log.d("Entryadapter","first");

        LayoutInflater layoutInflater = LayoutInflater.from(winnerActivity.getBaseContext());
        view = layoutInflater.inflate(R.layout.activity_user_entry,parent,false);


        return new EntryViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final EntryViewHolder holder, final int position) {
        System.out.println("attempted");

        if(entryArrayList.get(position).getStringformat().equals("formatSingleScore")){
            String userEmail = entryArrayList.get(position).getStringUserEmail();
            String userScore = entryArrayList.get(position).getStringSingleScore();
            alUsers.add(userEmail);
            alScore.add(Integer.parseInt(userScore));

        }
        if(entryArrayList.get(position).getStringformat().equals("formatScoreVsScore")){
            String userEmail = entryArrayList.get(position).getStringUserEmail();
            String userScore1 = entryArrayList.get(position).getStringScoreScore1();
            String userScore2 = entryArrayList.get(position).getStringScoreScore2();
            alUsers.add(userEmail);
            alScore1.add(Integer.parseInt(userScore1));
            alScore2.add(Integer.parseInt(userScore2));

        }

    }


    @Override
    public int getItemCount() {
        String[] arrayUser = alUsers.toArray(new String[alUsers.size()]);
        System.out.println("heres lies" + Arrays.toString(arrayUser));

        Log.d("Entryadapter","third");
        return entryArrayList.size();


    }





}
