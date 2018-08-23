package com.dz.swept;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ResolveActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "ResolveActivity";
    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    Boolean winnerChanged, singleScoreChanged, scoreVsScore1Changed, scoreVsScore2Changed, resultExists;
    String spinnerValue,filler, participants, format, singleScore, scoreVsScore1, scoreVsScore2;

    String stakeName;
    int spinnerPosition;

    TextView tvStakeName, tvTo;
    EditText etSingleScore, etScoreScore1, etScoreScore2;
    Spinner spnWinner;
    Button btnSubmitResult;

    ArrayAdapter<String> suggestions;
    ArrayList<String> out = new ArrayList<String>();

    ProgressDialog progSubmit;

    EditStakeActivity editStakeActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve);

        progSubmit= new ProgressDialog(this);

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();


        tvStakeName = findViewById(R.id.tvStakeName);
        tvTo = findViewById(R.id.tvTo);
        etSingleScore = findViewById(R.id.etSingleScore);
        etScoreScore1 = findViewById(R.id.etScoreScore1);
        etScoreScore2= findViewById(R.id.etScoreScore2);
        btnSubmitResult = findViewById(R.id.btnSubmitResult);
        spnWinner = findViewById(R.id.spnWinner);


        btnSubmitResult.setOnClickListener(this);

        reset();
        new IntentTask().execute();
        reset();




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
        spnWinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                spinnerValue = adapterView.getItemAtPosition(i).toString();
                System.out.println(spinnerValue);
                winnerChanged = true;
                System.out.println("boolean is changing");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }

    private void getIncomingIntent() {

        Log.d(TAG,"checking for incoming intent");
        if(getIntent().hasExtra("stake_name")) {
            Log.d(TAG, "found intent extras");


            stakeName = getIntent().getStringExtra("stake_name");
            tvStakeName.setText(stakeName);

            fStore.collection("stakes").document(stakeName).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        participants = documentSnapshot.getString("participants");
                        format = documentSnapshot.getString("format");

                        System.out.println(format);

                        if(format.equals("SingleScore")){
                            etSingleScore.setVisibility(View.VISIBLE);
                            etScoreScore1.setVisibility(View.GONE);
                            etScoreScore2.setVisibility(View.GONE);
                            tvTo.setVisibility(View.GONE);
                        }
                        if(format.equals("ScoreVsScore")){
                            etSingleScore.setVisibility(View.GONE);
                            etScoreScore1.setVisibility(View.VISIBLE);
                            etScoreScore2.setVisibility(View.VISIBLE);
                            tvTo.setVisibility(View.VISIBLE);
                        }

                        suggestions = new ArrayAdapter<String>(ResolveActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, separateString(participants));

                        System.out.println("this is " + Arrays.toString(separateString("part")));
                        spnWinner.setAdapter(suggestions);
                        new MyAsyncTask().execute();



                    }
                    else{
                        Toast.makeText(ResolveActivity.this, "Document does not exist", Toast.LENGTH_LONG).show();
                    }
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ResolveActivity.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                            Log.d("Failed to retrieve data", e.getMessage());

                        }
                    });
            fStore.collection("stakes").document(stakeName).collection("result").document("result").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");
                            resultExists = true;
                            if(format.equals("SingleScore")){
                                String winner = document.getString("winner");
                                String single = document.getString("score");

                                etSingleScore.setText(single);
                                spinnerPosition = suggestions.getPosition(winner);
                                spnWinner.setSelection(spinnerPosition);
                                new MyAsyncTask().execute();

                            }
                            if(format.equals("ScoreVsScore")){
                                String winner = document.getString("winner");
                                String vs1 = document.getString("score1");
                                String vs2 = document.getString("score2");

                                etScoreScore1.setText(vs1);
                                etScoreScore2.setText(vs2);
                                spinnerPosition = suggestions.getPosition(winner);
                                spnWinner.setSelection(spinnerPosition);
                                new MyAsyncTask().execute();
                            }


                        }
                        else{
                            System.out.println("doc no exists");
                            resultExists = false;
                        }

                    }

                }
            });

        }
    }
    public String[] separateString(String var){
        if(var.length()==0&&!filler.equals("")){
            out.add(filler);
            filler = "";
            String[] arraySeparated = out.toArray(new String[out.size()]);
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
    private void reset(){
        winnerChanged = false;
        singleScoreChanged = false;
        scoreVsScore1Changed = false;
        scoreVsScore2Changed = false;
        filler = "";
        out.clear();

    }

    @Override
    public void onClick(View view) {
        if(view==btnSubmitResult){

            if(resultExists){
                if(format.equals("SingleScore")){

                    if(winnerChanged || singleScoreChanged){
                        createDialog(1);
                    }
                    else{
                        submitResult(format);
                    }
                }
                if(format.equals("ScoreVsScore")){
                    if(winnerChanged || scoreVsScore1Changed || scoreVsScore2Changed) {
                        createDialog(1);
                    }
                    else{
                        submitResult(format);
                    }
                }
            }
            else{
                submitResult(format);
            }
        }
    }
    private void submitResult(String format) {


        progSubmit.setTitle("Submitting result");
        progSubmit.setMessage("Please wait...");
        progSubmit.show();

        if(format.equals("SingleScore")){
            System.out.println("tried");
            if (user != null) {
                singleScore = etSingleScore.getText().toString();
                Map<String, Object> note = new HashMap<>();
                System.out.println(user.getEmail());
                note.put("winner", spinnerValue);
                note.put("score", singleScore);

                fStore.collection("stakes").document(stakeName).collection("result").document("result").set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        updateStatus();

                        Log.d(TAG, "entry was submitted!");


                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progSubmit.dismiss();


                            }
                        });

            }
        }
        if(format.equals("ScoreVsScore")){
            if (user != null) {
                scoreVsScore1 = etScoreScore1.getText().toString();
                scoreVsScore2 = etScoreScore2.getText().toString();
                Map<String, Object> note = new HashMap<>();
                note.put("winner", spinnerValue);
                note.put("score1",scoreVsScore1);
                note.put("score2",scoreVsScore2);


                fStore.collection("stakes").document(stakeName).collection("result").document("result").set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        updateStatus();

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
    private void createDialog(int n){
        if(n==0){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You have unsaved changes. Are you sure you want to exit?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ResolveActivity.super.onBackPressed();
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
            alert.setMessage("By clicking yes, you are changing your previous result. Continue?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (format.equals("SingleScore")) {
                        singleScore = etSingleScore.getText().toString();
                        if (TextUtils.isEmpty(singleScore)) {
                            Toast.makeText(ResolveActivity.this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            try {
                                submitResult(format);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                    if(format.equals("ScoreVsScore")){
                        scoreVsScore1 = etScoreScore1.getText().toString();
                        scoreVsScore2 = etScoreScore2.getText().toString();

                        if ( TextUtils.isEmpty(scoreVsScore1) || TextUtils.isEmpty(scoreVsScore2)) {
                            Toast.makeText(ResolveActivity.this, "One of the fields is missing.", Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            try {
                                submitResult(format);
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

    @Override
    public void onBackPressed() {
        singleScore = etSingleScore.getText().toString();
        scoreVsScore1 = etScoreScore1.getText().toString();
        scoreVsScore2 = etScoreScore2.getText().toString();

        if(!resultExists){
            if(format.equals("SingleScore")){
                if(winnerChanged || !TextUtils.isEmpty(singleScore)){
                    createDialog(0);
                }
                else{
                    ResolveActivity.super.onBackPressed();
                }
            }
            if(format.equals("ScoreVsScore")){
                if(winnerChanged || !TextUtils.isEmpty(scoreVsScore1) || !TextUtils.isEmpty(scoreVsScore2)) {
                    if(winnerChanged){
                        System.out.println("winner changed");
                    }
                    createDialog(0);
                }
                else{
                    ResolveActivity.super.onBackPressed();
                }
            }
        }
        else{
            if(format.equals("SingleScore")){

                if(winnerChanged || singleScoreChanged){
                    createDialog(0);
                }
                else{
                    ResolveActivity.super.onBackPressed();
                }
            }
            if(format.equals("ScoreVsScore")){
                if(winnerChanged || scoreVsScore1Changed || scoreVsScore2Changed) {
                    createDialog(0);
                }
                else{
                    ResolveActivity.super.onBackPressed();
                }
            }
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

            reset();

        }
    }
    private void updateStatus(){

        Map<String, Object> note1 = new HashMap<>();
        System.out.println(user.getEmail());
        note1.put("status", "Finished");

        fStore.collection("stakes").document(stakeName).update(note1).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                progSubmit.dismiss();
                Toast.makeText(ResolveActivity.this, "Result Published!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ResolveActivity.this, MainActivity.class));
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

    private class IntentTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            System.out.println("finished");

            getIncomingIntent();





        }

    }

}
