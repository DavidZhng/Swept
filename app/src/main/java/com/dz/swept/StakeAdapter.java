package com.dz.swept;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.logging.Handler;

public class StakeAdapter extends RecyclerView.Adapter<StakeViewHolder> {

    MainActivity mainActivity;
    ViewEntriesActivity viewEntriesActivity;
    ArrayList<User> userArrayList;
    SwipeRefreshLayout swipe;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    String giveStakeName;
    View view;


    public StakeAdapter(MainActivity mainActivity, ArrayList<User> userArrayList, SwipeRefreshLayout swipe) {
        this.mainActivity = mainActivity;
        this.userArrayList = userArrayList;
        this.swipe = swipe;

    }

    @NonNull
    @Override
    public StakeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        Log.d("Stakeadapter","first");

        LayoutInflater layoutInflater = LayoutInflater.from(mainActivity.getBaseContext());
        view = layoutInflater.inflate(R.layout.stake_item,parent,false);



        return new StakeViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final StakeViewHolder holder, final int position) {

        giveStakeName = userArrayList.get(position).getStringStakeName();

        holder.tvStakeName.setText(userArrayList.get(position).getStringStakeName());
        holder.tvCreator.setText(userArrayList.get(position).getStringCreator());
        holder.tvParticipants.setText(userArrayList.get(position).getStringParticipants());
        holder.tvStatus.setText(userArrayList.get(position).getStringStatus());
        holder.tvFormat.setText(userArrayList.get(position).getStringFormat());
        holder.tvReward.setText(userArrayList.get(position).getStringReward());
        holder.btnEnterStake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.tvStatus.getText().toString().equals("Closed")){
                    Toast.makeText(mainActivity.getBaseContext(), "Entries for this stake have been closed.", Toast.LENGTH_SHORT).show();

                }
                if(holder.tvStatus.getText().toString().equals("Finished")){
                    Toast.makeText(mainActivity.getBaseContext(), "This stake has been finished.Check results now!", Toast.LENGTH_SHORT).show();
                }
                else{
                    enterStake(position);

                }
            }


        });
        holder.btnConfigStake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmCreator(position);
            }
        });
        holder.btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enterView(position);
            }
        });

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try{

                    mainActivity.loadDataFromFireStore();
                    swipe.setRefreshing(false);
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
        Log.d("Stakeadapter","second");
        System.out.println(holder.tvStakeName.getText().toString() + holder.tvStatus.getText().toString());
        if( holder.tvStatus.getText().toString().equals("Closed")){
            holder.tvStatus.setTextColor(Color.RED);
            holder.tvStatusLight.setBackground(ContextCompat.getDrawable(mainActivity.getBaseContext(), R.drawable.et_stylered));
        }
        if( holder.tvStatus.getText().toString().equals("Finished")){
            holder.tvStatus.setTextColor(Color.GRAY);
            holder.tvStatusLight.setBackground(ContextCompat.getDrawable(mainActivity.getBaseContext(), R.drawable.et_stylegray));
        }
        if( holder.tvStatus.getText().toString().equals("Ongoing")){
            holder.tvStatus.setTextColor(Color.GREEN);
            holder.tvStatusLight.setBackground(ContextCompat.getDrawable(mainActivity.getBaseContext(), R.drawable.et_stylegreen));
        }

    }

    private void enterView(final int position) {
        giveStakeName = userArrayList.get(position).getStringStakeName();

        Intent intent = new Intent(mainActivity.getBaseContext(),ViewEntriesActivity.class);
        intent.putExtra("stake_name", userArrayList.get(position).getStringStakeName());
        intent.putExtra("format", userArrayList.get(position).getStringFormat());

        mainActivity.getBaseContext().startActivity(intent);
    }


    @Override
    public int getItemCount() {

        Log.d("Stakeadapter","third");
        return userArrayList.size();


    }

    private void enterStake(final int position) {

        giveStakeName = userArrayList.get(position).getStringStakeName();
        /*
        mainActivity.fStore.collection("stakes").document(userArrayList.get(position).getUserId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(mainActivity.getBaseContext(),"entered!",Toast.LENGTH_SHORT).show();
                mainActivity.loadDataFromFireStore();
            }
        });
        */
        fStore.collection("stakes").document(giveStakeName).collection("submits").document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");
                        createDialog(position);
                    }

                    else {
                        System.out.println("doc no exists");

                        Intent intent = new Intent(mainActivity.getBaseContext(),GalleryActivity.class);
                        intent.putExtra("New/Existing", "New");
                        intent.putExtra("stake_name", userArrayList.get(position).getStringStakeName());
                        intent.putExtra("format", userArrayList.get(position).getStringFormat());
                        intent.putExtra("participants", userArrayList.get(position).getStringParticipants());


                        mainActivity.getBaseContext().startActivity(intent);



                        //The user doesn't exist...
                    }

                }
            }


        });



    }
    private void createDialog(final int position){

        AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
        alert.setMessage("You have already submitted an entry. Would you like to edit you entry?");
        alert.setCancelable(false);

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(mainActivity.getBaseContext(),GalleryActivity.class);
                intent.putExtra("New/Existing", "Existing");
                intent.putExtra("stake_name", userArrayList.get(position).getStringStakeName());
                intent.putExtra("format", userArrayList.get(position).getStringFormat());
                intent.putExtra("participants", userArrayList.get(position).getStringParticipants());


                mainActivity.getBaseContext().startActivity(intent);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }

        });
        alert.create().show();
    }
    private void confirmCreator(final int position) {

        giveStakeName = userArrayList.get(position).getStringStakeName();

        fStore.collection("stakes").document(giveStakeName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String stakeCreator = documentSnapshot.getString("creator");
                    if(user.getEmail().equals(stakeCreator)){
                        try{
                            enterEditStake(position);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }

                    }
                    else{
                        Toast.makeText(mainActivity.getBaseContext(), "Only the creator can update this stake.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mainActivity.getBaseContext(), "Failed to retrieve data", Toast.LENGTH_LONG).show();
                        Log.d("Failed to retrieve data", e.getMessage());
                    }
                });

    }

    private void enterEditStake(final int position) {

        giveStakeName = userArrayList.get(position).getStringStakeName();

        fStore.collection("stakes").document(giveStakeName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");
                        Intent intent = new Intent(mainActivity.getBaseContext(),EditStakeActivity.class);
                        intent.putExtra("stake_name", userArrayList.get(position).getStringStakeName());
                        intent.putExtra("status", userArrayList.get(position).getStringStatus());
                        intent.putExtra("invitees", userArrayList.get(position).getStringInvitees());
                        intent.putExtra("reward", userArrayList.get(position).getStringReward());


                        mainActivity.getBaseContext().startActivity(intent);
                    }

                    else {
                        System.out.println("doc no exists");





                        //The user doesn't exist...
                    }

                }

            }
        });
    }


}
