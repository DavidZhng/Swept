package com.dz.swept;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewEntriesActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "ViewEntriesActivity";
    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    public Entry blank;
    EntryAdapter adapter;
    SwipeRefreshLayout swipe;

    RecyclerView recyclerView;
    TextView tvNone,tvStakeTitle;
    ImageView imgHome,imgWinner;

    public ArrayList<Entry> entryArrayList;

    String stakeName, format;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entries);

        entryArrayList = new ArrayList<>();

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();

        swipe = findViewById(R.id.swipe);
        tvNone = findViewById(R.id.tvNone);
        tvStakeTitle = findViewById(R.id.tvStakeTitle);
        imgHome = findViewById(R.id.imgHome);
        imgWinner = findViewById(R.id.imgWinner);

        imgHome.setOnClickListener(this);
        imgWinner.setOnClickListener(this);

        setUpRecyclerView();
        getIncomingIntent();

        loadDataFromFireStore();


    }

    @Override
    public void onClick(View view) {
        if(view==imgHome){
            startActivity(new Intent(ViewEntriesActivity.this, MainActivity.class));
        }
        if(view==imgWinner){
            fStore.collection("stakes").document(stakeName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");

                            String status = document.getString("status");

                            if(status.equals("Finished")){

                                Intent intent = new Intent(ViewEntriesActivity.this,WinnerActivity.class);
                                intent.putExtra("stake_name", stakeName);
                                intent.putExtra("format", format);


                                ViewEntriesActivity.this.startActivity(intent);

                            }
                            else{
                                Toast.makeText(ViewEntriesActivity.this, "The creator of this stake has not published the result yet.", Toast.LENGTH_LONG).show();
                            }

                        }
                        else{
                            System.out.println("doc no exists");

                        }

                    }

                }
            });
        }
    }
    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void getIncomingIntent() {
            Log.d(TAG, "checking for incoming intent");
            if (getIntent().hasExtra("stake_name")&&getIntent().hasExtra("format")) {
                Log.d(TAG, "found intent extras");

                stakeName = getIntent().getStringExtra("stake_name");
                format = getIntent().getStringExtra("format");

                tvStakeTitle.setText(stakeName);


            }
    }
    public void loadDataFromFireStore() {

        recyclerView.getRecycledViewPool().clear();

        if (entryArrayList.size() > 0)
            entryArrayList.clear();
        fStore.collection("stakes").document(stakeName).collection("submits").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot querySnapshot : task.getResult()) {
                    blank = new Entry(querySnapshot.getId(),
                            querySnapshot.getString("email"),
                            querySnapshot.getString("score"),
                            querySnapshot.getString("score1"),
                            querySnapshot.getString("score2"),
                            querySnapshot.getString("winner"),
                            querySnapshot.getString("format"));

                    entryArrayList.add(blank);


                }
                adapter = new EntryAdapter(ViewEntriesActivity.this, entryArrayList, swipe);
                recyclerView.setAdapter(adapter);
                if(entryArrayList.size()==0){
                    tvNone.setVisibility(View.VISIBLE);
                }
                else{
                    tvNone.setVisibility(View.GONE);
                }


            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewEntriesActivity.this, "Problem - 1", Toast.LENGTH_LONG).show();
                        Log.d("Problem - 1", e.getMessage());

                    }
                });
    }

}
