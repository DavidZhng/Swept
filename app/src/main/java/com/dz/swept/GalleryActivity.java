package com.dz.swept;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "GalleryActivity";

    TextView tvStakeName,tvFormat,tvParticipants,tvStatus,tvTo,tvNote,tvSpace, tvPartNonexistent, tvSpace2;
    EditText etSingleScore, etScoreScore1, etScoreScore2;
    Spinner spnWinner;
    ImageView imgHome;
    Button btnSubmitStake;
    ProgressDialog progSubmit;

    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    String winner, singleScore, scoreVsScore1, scoreVsScore2, stakeTitle, filler, stakeName, format, participants, condition, spinnerValue;
    int spinnerPosition;
    Boolean winnerChanged, singleScoreChanged, scoreVsScore1Changed, scoreVsScore2Changed, partExists;

    ArrayList<String> out = new ArrayList<String>();
    ArrayList<String> sugs = new ArrayList<String>();
    ArrayAdapter<String> suggestions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Log.d(TAG,"oncreate started");

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();

        etSingleScore = findViewById(R.id. etSingleScore);
        tvStakeName = findViewById(R.id. tvStakeName);
        tvSpace = findViewById(R.id.tvSpace);
        tvPartNonexistent = findViewById(R.id.tvPartNonexistent);
        tvSpace2 = findViewById(R.id. tvSpace2);
        etScoreScore1 = findViewById(R.id.etScoreScore1);
        etScoreScore2 = findViewById(R.id. etScoreScore2);
        spnWinner = findViewById(R.id.spnWinner);
        tvTo = findViewById(R.id.tvTo);
        tvNote = findViewById(R.id.tvNote);
        imgHome = findViewById(R.id.imgHome);
        btnSubmitStake = findViewById(R.id.btnSubmitStake);

        imgHome.setOnClickListener(this);
        btnSubmitStake.setOnClickListener(this);
        progSubmit= new ProgressDialog(this);
        spnWinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    spinnerValue = adapterView.getItemAtPosition(i).toString();
                    System.out.println(spinnerValue );
                    winnerChanged = true;
                    System.out.println("we changed");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        stakeTitle = tvStakeName.getText().toString();
        reset();
        getIncomingIntent();


        /*
        acWinner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                winnerChanged = true;
                partExists = false;
                acWinner.setTextColor(Color.BLACK);
                tvSpace2.setVisibility(View.VISIBLE);
                tvPartNonexistent.setVisibility(View.GONE);
                filler = "";
                out.clear();


            }

            @Override
            public void afterTextChanged(Editable editable) {

                try{
                    addressSeparations(separateString(tvParticipants.getText().toString()),0,0);

                }
                catch(Exception e){
                    e.printStackTrace();
                }


            }

        });
        */
        etSingleScore.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                singleScoreChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etScoreScore1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                scoreVsScore1Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etScoreScore2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                scoreVsScore2Changed = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getIncomingIntent(){
        Log.d(TAG,"checking for incoming intent");
        if(getIntent().hasExtra("stake_name")&&getIntent().hasExtra("New/Existing")&&
                getIntent().hasExtra("format")&&getIntent().hasExtra("participants")){
            Log.d(TAG, "found intent extras");

            stakeName = getIntent().getStringExtra("stake_name");
            format = getIntent().getStringExtra("format");
            participants = getIntent().getStringExtra("participants");
            condition = getIntent().getStringExtra("New/Existing");

            separateString(participants);
            sugs.add("");
            suggestions = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_dropdown_item, sugs.toArray(new String[sugs.size()]));

            spnWinner.setAdapter(suggestions);

            if(condition.equals("New")){
                int spinnerPos = suggestions.getPosition(sugs.toArray(new String[sugs.size()])[sugs.size()-1]);
                System.out.println("look it changed");
                spnWinner.setSelection(spinnerPos);
                winnerChanged = false;
            }


            setIntent(stakeName,format, participants,condition);

        }
    }
    private void setIntent(String stakeName, String format, String participants, String condition){
        Log.d(TAG, "setting intent");

        tvStakeName = findViewById(R.id.tvStakeName);
        tvFormat = findViewById(R.id.tvFormat);
        tvParticipants = findViewById(R.id.tvParticipants);

        tvStakeName.setText(stakeName);
        tvFormat.setText(format);
        tvParticipants.setText(participants);

        if(condition.equals("New")){
            if(tvFormat.getText().toString().equals("SingleScore")) {
                etSingleScore.setVisibility(View.VISIBLE);
                etScoreScore1.setVisibility(View.GONE);
                etScoreScore2.setVisibility(View.GONE);
                tvTo.setVisibility(View.GONE);
            }

            if(tvFormat.getText().toString().equals("ScoreVsScore")) {
                etSingleScore.setVisibility(View.GONE);
                etScoreScore1.setVisibility(View.VISIBLE);
                etScoreScore2.setVisibility(View.VISIBLE);
                tvTo.setVisibility(View.VISIBLE);
                tvNote.setVisibility(View.VISIBLE);
                tvSpace.setVisibility(View.GONE);
            }
        }
        else{
            if(tvFormat.getText().toString().equals("SingleScore")){
                etSingleScore.setVisibility(View.VISIBLE);
                etScoreScore1.setVisibility(View.GONE);
                etScoreScore2.setVisibility(View.GONE);
                tvTo.setVisibility(View.GONE);

                fStore.collection("stakes").document(stakeName).collection("submits").document(user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String userWinner = documentSnapshot.getString("winner");
                            String userSingleScore = documentSnapshot.getString("score");

                            spinnerPosition = suggestions.getPosition( userWinner );
                            System.out.println("tho changed");
                            spnWinner.setSelection(spinnerPosition);
                            new MyAsyncTask().execute();
                            etSingleScore.setText(userSingleScore);
                            reset();
                        }
                        else{
                            Toast.makeText(GalleryActivity.this, "Document does not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GalleryActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                                Log.d("Failed to retrieve data", e.getMessage());

                            }
                        });


            }
            if(tvFormat.getText().toString().equals("ScoreVsScore")){
                etSingleScore.setVisibility(View.GONE);
                etScoreScore1.setVisibility(View.VISIBLE);
                etScoreScore2.setVisibility(View.VISIBLE);
                tvTo.setVisibility(View.VISIBLE);
                tvNote.setVisibility(View.VISIBLE);
                tvSpace.setVisibility(View.GONE);

                fStore.collection("stakes").document(stakeName).collection("submits").document(user.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String userWinner = documentSnapshot.getString("winner");
                            String userScore1 = documentSnapshot.getString("score1");
                            String userScore2 = documentSnapshot.getString("score2");

                            spinnerPosition = suggestions.getPosition(userWinner);
                            System.out.println("tho changed");
                            spnWinner.setSelection(spinnerPosition);
                            new MyAsyncTask().execute();
                            etScoreScore1.setText(userScore1);
                            etScoreScore2.setText(userScore2);
                            reset();

                        }
                        else{
                            Toast.makeText(GalleryActivity.this, "Document does not exist", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(GalleryActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                                Log.d("Failed to retrieve data", e.getMessage());

                            }
                        });
            }
        }
        winnerChanged = false;



    }

    @Override
    public void onClick(View view) {
        if (view == imgHome) {
            singleScore = etSingleScore.getText().toString();
            scoreVsScore1 = etScoreScore1.getText().toString();
            scoreVsScore2 = etScoreScore2.getText().toString();

            if(condition.equals("New")){
                System.out.println("1 changed");
                if(tvFormat.getText().toString().equals("SingleScore")){

                    if(!TextUtils.isEmpty(spinnerValue)|| !TextUtils.isEmpty(singleScore)){
                        if(winnerChanged){
                            System.out.println("it changed");
                        }

                        createDialog(0);
                    }
                    else{
                        GalleryActivity.super.onBackPressed();
                    }
                }
                if(tvFormat.getText().toString().equals("ScoreVsScore")){
                    if(winnerChanged || !TextUtils.isEmpty(scoreVsScore1) || !TextUtils.isEmpty(scoreVsScore2)) {
                        createDialog(0);
                    }
                    else{
                        GalleryActivity.super.onBackPressed();
                    }
                }
            }
            else{
                if(tvFormat.getText().toString().equals("SingleScore")){

                    if(winnerChanged || singleScoreChanged){
                        if(winnerChanged){
                            System.out.println("winnerchanged");
                        }
                        createDialog(0);
                    }
                    else{
                        GalleryActivity.super.onBackPressed();
                    }
                }
                if(tvFormat.getText().toString().equals("ScoreVsScore")){
                    if(winnerChanged || scoreVsScore1Changed || scoreVsScore2Changed) {
                        createDialog(0);
                    }
                    else{
                        GalleryActivity.super.onBackPressed();
                    }
                }
            }

        }
        if (view == btnSubmitStake) {
            if(condition.equals("New")){
                    if (tvFormat.getText().toString().equals("SingleScore")) {
                        singleScore = etSingleScore.getText().toString();
                        if (TextUtils.isEmpty(singleScore)) {
                            Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            try {
                                submitStake("formatSingleScore");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    if(tvFormat.getText().toString().equals("ScoreVsScore")){
                        scoreVsScore1 = etScoreScore1.getText().toString();
                        scoreVsScore2 = etScoreScore2.getText().toString();

                        if (TextUtils.isEmpty(scoreVsScore1) || TextUtils.isEmpty(scoreVsScore2)) {
                            Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            try {
                                submitStake("formatScoreVsScore");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

            }
            else{
                if(tvFormat.getText().toString().equals("SingleScore")){

                    if(winnerChanged || singleScoreChanged){
                        createDialog(1);
                    }
                    else{

                            if (tvFormat.getText().toString().equals("SingleScore")) {
                                singleScore = etSingleScore.getText().toString();

                                if (TextUtils.isEmpty(singleScore)) {
                                    Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    try {
                                        submitStake("formatSingleScore");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                            if(tvFormat.getText().toString().equals("ScoreVsScore")){
                                scoreVsScore1 = etScoreScore1.getText().toString();
                                scoreVsScore2 = etScoreScore2.getText().toString();

                                if (TextUtils.isEmpty(scoreVsScore1) || TextUtils.isEmpty(scoreVsScore2)) {
                                    Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    try {
                                        submitStake("formatScoreVsScore");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                    }
                }
                if(tvFormat.getText().toString().equals("ScoreVsScore")){
                    if(winnerChanged || scoreVsScore1Changed || scoreVsScore2Changed) {
                        createDialog(1);
                    }
                    else{
                            if (tvFormat.getText().toString().equals("SingleScore")) {
                                singleScore = etSingleScore.getText().toString();
                                if (TextUtils.isEmpty(singleScore)) {
                                    Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    try {
                                        submitStake("formatSingleScore");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                            if(tvFormat.getText().toString().equals("ScoreVsScore")){
                                scoreVsScore1 = etScoreScore1.getText().toString();
                                scoreVsScore2 = etScoreScore2.getText().toString();

                                if (TextUtils.isEmpty(scoreVsScore1) || TextUtils.isEmpty(scoreVsScore2)) {
                                    Toast.makeText(this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    try {
                                        submitStake("formatScoreVsScore");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                    }
                }
            }


        }

    }
        private void submitStake(String format) {
            progSubmit.setTitle("Submitting entry");
            progSubmit.setMessage("Please wait...");
            progSubmit.show();

            if(format.equals("formatSingleScore")){
                System.out.println("tried");
                if (user != null) {
                    Map<String, Object> note = new HashMap<>();
                    System.out.println(user.getEmail());
                    note.put("winner", spinnerValue);
                    note.put("score", singleScore);
                    note.put("format", format);
                    note.put("email", user.getEmail());

                    fStore.collection("stakes").document( tvStakeName.getText().toString()).collection("submits").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progSubmit.dismiss();
                            Toast.makeText(GalleryActivity.this, "Entry submitted!", Toast.LENGTH_SHORT).show();
                            finish();



                            Log.d(TAG, "entry was submitted!");


                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                }
                            });
                }
            }
            if(format.equals("formatScoreVsScore")){
                if (user != null) {
                    Map<String, Object> note = new HashMap<>();
                    note.put("winner", spinnerValue);
                    note.put("format", format);
                    note.put("score1",scoreVsScore1);
                    note.put("score2",scoreVsScore2);
                    note.put("email", user.getEmail());


                    fStore.collection("stakes").document( tvStakeName.getText().toString()).collection("submits").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            progSubmit.dismiss();
                            Toast.makeText(GalleryActivity.this, "Entry submitted!", Toast.LENGTH_SHORT).show();
                            finish();


                            Log.d(TAG, "entry was submitted!");


                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    progSubmit.dismiss();


                                    Log.d(" failed to submit!", e.toString());
                                }
                            });
                }
            }


        }

    @Override
    public void onBackPressed() {
        singleScore = etSingleScore.getText().toString();
        scoreVsScore1 = etScoreScore1.getText().toString();
        scoreVsScore2 = etScoreScore2.getText().toString();

        if(condition.equals("New")){
            System.out.println("1 changed");
            if(tvFormat.getText().toString().equals("SingleScore")){

                if(!TextUtils.isEmpty(spinnerValue)|| !TextUtils.isEmpty(singleScore)){
                    if(winnerChanged){
                        System.out.println("it changed");
                    }

                    createDialog(0);
                }
                else{
                    GalleryActivity.super.onBackPressed();
                }
            }
            if(tvFormat.getText().toString().equals("ScoreVsScore")){
                if(winnerChanged || !TextUtils.isEmpty(scoreVsScore1) || !TextUtils.isEmpty(scoreVsScore2)) {
                    createDialog(0);
                }
                else{
                    GalleryActivity.super.onBackPressed();
                }
            }
        }
        else{
            if(tvFormat.getText().toString().equals("SingleScore")){

                if(winnerChanged || singleScoreChanged){
                    if(winnerChanged){
                        System.out.println("winnerchanged");
                    }
                    createDialog(0);
                }
                else{
                    GalleryActivity.super.onBackPressed();
                }
            }
            if(tvFormat.getText().toString().equals("ScoreVsScore")){
                if(winnerChanged || scoreVsScore1Changed || scoreVsScore2Changed) {
                    createDialog(0);
                }
                else{
                    GalleryActivity.super.onBackPressed();
                }
            }
        }


    }
    private void reset(){
        winnerChanged = false;
        singleScoreChanged = false;
        scoreVsScore1Changed = false;
        scoreVsScore2Changed = false;
        partExists = false;
        filler = "";
        out.clear();

    }
    private void createDialog(int n){
        if(n==0){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You have unsaved changes. Are you sure you want to exit?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(GalleryActivity.this, MainActivity.class));
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }

            });
            alert.create().show();
        }
        if(n==1){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("By clicking yes, you are changing your previous entry. Continue?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                        if (tvFormat.getText().toString().equals("SingleScore")) {
                            singleScore = etSingleScore.getText().toString();
                            if (TextUtils.isEmpty(singleScore)) {
                                Toast.makeText(GalleryActivity.this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                try {
                                    submitStake("formatSingleScore");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                        if(tvFormat.getText().toString().equals("ScoreVsScore")){
                            scoreVsScore1 = etScoreScore1.getText().toString();
                            scoreVsScore2 = etScoreScore2.getText().toString();

                            if ( TextUtils.isEmpty(scoreVsScore1) || TextUtils.isEmpty(scoreVsScore2)) {
                                Toast.makeText(GalleryActivity.this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                try {
                                    submitStake("formatScoreVsScore");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }

            });
            alert.create().show();
        }

    }

    public String[] separateString(String var){
        if(var.length()==0&&!filler.equals("")){
            out.add(filler);
            filler = "";
            String[] arraySeparated = out.toArray(new String[out.size()]);
            sugs = out;
            System.out.println(Arrays.toString(arraySeparated));
            return arraySeparated;
        }
        if(var.length()==0){
            String[] arraySeparated = out.toArray(new String[out.size()]);
            System.out.println(Arrays.toString(arraySeparated));
            return arraySeparated;
        }
        if(!var.substring(0,1).equals(",")&&!var.substring(0,1).equals("")){
            filler += var.substring(0,1);
            return separateString(var.substring(1));
        }
        if(var.substring(0,1).equals(",")&&!filler.equals("")){
            out.add(filler);
            filler = "";
            return separateString(var.substring(1));
        }
        else{
            return separateString(var.substring(1));
        }

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

            System.out.println("0");
            /*

             */




        }
    }

    /*
    public String addressSeparations(final String[] var, int index, int n){
        if(n==0){
            if(index<var.length){
                if(index>0){
                    if(var[index].equals(acWinner.getText().toString())){
                        System.out.println(var[index]);
                        System.out.println("part exists");
                        partExists = true;
                        return "";
                    }
                    else{
                        System.out.println(var[index]);
                        System.out.println("part does not exist");
                        return "" + addressSeparations(var,index +1,n);
                    }
                }
                else{
                    if(var[index].equals(acWinner.getText().toString())){
                        System.out.println(var[index]);
                        System.out.println("part exists");
                        partExists = true;
                        return "";
                    }
                    else{
                        System.out.println(var[index]);
                        System.out.println("part does not exist");
                        return "" + addressSeparations(var,index +1,n);
                    }
                }

            }
            else{
                return "";
            }
        }
        else{
            return "";
        }


    }
    */
}

