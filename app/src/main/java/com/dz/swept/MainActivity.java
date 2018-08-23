package com.dz.swept;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    CircleImageView imgProfilePic;
    FirebaseAuth fAuth;
    TextView tvUsername, tvLoading, tvShrug;
    ImageView imgLul;
    Button btnNewStake;
    FirebaseFirestore fStore;
    RecyclerView recyclerMain;
    public ArrayList<User> userArrayList;
    public User blank;
    StakeAdapter adapter;
    SwipeRefreshLayout swipe;
    final String TAG = "MainActivity";
    Boolean emailExists;
    FirebaseUser user;
    ProgressBar progLoading;
    int counter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        userArrayList = new ArrayList<>();

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();



        imgProfilePic = findViewById(R.id.imgProfilePic);
        tvShrug = findViewById(R.id. tvShrug);
        imgLul = findViewById(R.id. imgLul);
        tvUsername = findViewById(R.id.tvUsername);
        btnNewStake = findViewById(R.id.btnNewStake);
        tvLoading = findViewById(R.id.tvLoading);
        progLoading = findViewById(R.id.progLoading);
        swipe = findViewById(R.id.swipe);


        tvUsername.setRotation(-90);

        LinearLayout.LayoutParams Params1 = new LinearLayout.LayoutParams(166,50);
        Params1.height = getResources().getDimensionPixelSize(R.dimen.text_view_height);
        Params1.width = getResources().getDimensionPixelSize(R.dimen.text_view_width);
        Params1.setMargins(0,150,0,0);
        tvUsername.setLayoutParams(Params1);


        imgProfilePic.setOnClickListener(this);
        btnNewStake.setOnClickListener(this);

        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());
        System.out.println(currentDate);



        loadUserInformation();




    }

    @Override
    public void onClick(View view) {

        if (view == imgProfilePic) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        if (view == btnNewStake) {
            try {
                startActivity(new Intent(MainActivity.this, CreateStakeActivity.class));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    private void loadUserInformation() {
        final FirebaseUser user = fAuth.getCurrentUser();

        emailExists = false;

        if (user != null) {
            setUpRecyclerView();
            setUpFireBase();
            loadDataFromFireStore();

            if (user.getPhotoUrl() != null) {
                Uri photoUrl = user.getPhotoUrl();
                Glide.with(getApplicationContext())
                        .load(photoUrl.toString())
                        .into(imgProfilePic);
            }

            if (user.getDisplayName() != null) {
                tvUsername.setText(Html.fromHtml("&ldquo; " + user.getDisplayName().toUpperCase() + " &rdquo;"));
            }

        }

    }

    public void loadDataFromFireStore() {

        try{
            recyclerMain.getRecycledViewPool().clear();

            if (userArrayList.size() > 0)
                userArrayList.clear();
            fStore.collection("stakes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    for (DocumentSnapshot querySnapshot : task.getResult()) {
                        blank = new User(querySnapshot.getId(),
                                querySnapshot.getString("creator"),
                                querySnapshot.getString("stake_name"),
                                querySnapshot.getString("status"),
                                querySnapshot.getString("participants"),
                                querySnapshot.getString("format"),
                                querySnapshot.getString("reward"),
                                querySnapshot.getString("invitees"));

                        counter++;

                    /*
                    userArrayList.add(blank);
                    */
                        checkIfEmailInvited(querySnapshot.getString("stake_name"),blank);
                        System.out.println(querySnapshot.getString("stake_name"));

                    }

                    new MyAsyncTask().execute();
                /*
                adapter = new StakeAdapter(MainActivity.this, userArrayList, swipe);
                recyclerMain.setAdapter(adapter);
                */

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Problem - 1", Toast.LENGTH_LONG).show();
                            Log.d("Problem - 1", e.getMessage());

                        }
                    });

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }


    private void setUpFireBase() {
        fStore = FirebaseFirestore.getInstance();
    }

    private void setUpRecyclerView() {
        recyclerMain = findViewById(R.id.recyclerMain);
        recyclerMain.setHasFixedSize(true);
        recyclerMain.setLayoutManager(new LinearLayoutManager(this));
    }

    public void checkIfEmailInvited(String stakeName, final User filler){

        fStore.collection("stakes").document(stakeName).collection("guests").document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");

                        userArrayList.add(filler);
                    }

                    else {
                        System.out.println("doc no exists");
                        emailExists = false;


                        //The user doesn't exist...
                    }

                }
            }


        });
    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            System.out.println("finished");
            progLoading.setVisibility(View.GONE);
            tvLoading.setVisibility(View.GONE);
            swipe.setVisibility(View.VISIBLE);
            System.out.println("0");
            /*

             */
            adapter = new StakeAdapter(MainActivity.this, userArrayList, swipe);

            if(userArrayList.size()==0){
                tvShrug.setVisibility(View.VISIBLE);
                imgLul.setVisibility(View.VISIBLE);
                recyclerMain.setAdapter(adapter);

            }
            else{
                tvShrug.setVisibility(View.GONE);
                imgLul.setVisibility(View.GONE);
                recyclerMain.setAdapter(adapter);
            }


        }
    }


}
