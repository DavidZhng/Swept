package com.dz.swept;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WinnerActivity extends AppCompatActivity {
    private static final String TAG = "WinnerActivity";
    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    ProgressDialog progRandom;

    public Entry blank;
    WinnerAdapter adapter;

    String stakeName, format, resultWinner, resultScore, resultScore1, resultScore2, reward, existingWinnerEmail, existingWinnerScore,existingWinnerWinner,existingWinnerScore1,existingWinnerScore2;
    int tieCounter, randomedUser;
    Boolean collectionExists;

    TextView tvStakeName,tvSingleScore,tvScoreScore1,tvScoreScore2,tvTo,tvWinner,tvUserEmail,tvReward,tvUserSingleScore,tvUserScoreScore1,tvUserScoreScore2,tvUserTo,tvUserWinner,tvRandom;
    RecyclerView recyclerView;

    public ArrayList<Entry> entryArrayList;
    List<String> usernameList;
    List<String> documentList;
    ArrayList<String> alUsers;
    ArrayList<String> alUserEmails;
    ArrayList<String> alWinners;
    ArrayList<Integer> alScore;
    ArrayList<Integer> alScore1;
    ArrayList<Integer> alScore2;
    ArrayList<Integer> resultList;
    ArrayList<Integer> alOgScore;
    ArrayList<Integer> alOgScore1;
    ArrayList<Integer> alOgScore2;
    ArrayList<Integer> tieList;

    String[] arrayUserEmails;
    String[] arrayUsers;
    String[] arrayWinners;
    String[] arrayDocuments;
    int[] arrayScores;
    int[] arrayScores1;
    int[] arrayScores2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        collectionExists = false;

        usernameList = new ArrayList<>();
        usernameList.clear();

        documentList = new ArrayList<>();
        documentList.clear();

        entryArrayList = new ArrayList<>();
        tieList = new ArrayList<>();
        alWinners = new ArrayList<>();
        alUsers = new ArrayList<>();
        alUserEmails = new ArrayList<>();
        alScore = new ArrayList<>();
        alScore1 = new ArrayList<>();
        alScore2 = new ArrayList<>();
        resultList = new ArrayList<>();
        alOgScore = new ArrayList<>();
        alOgScore1 = new ArrayList<>();
        alOgScore2= new ArrayList<>();

        progRandom = new ProgressDialog(this);



        tvStakeName = findViewById(R.id.tvStakeName);
        tvSingleScore = findViewById(R.id.tvSingleScore);
        tvScoreScore1 = findViewById(R.id.tvScoreScore1);
        tvScoreScore2 = findViewById(R.id.tvScoreScore2);
        tvTo= findViewById(R.id.tvTo);
        tvReward= findViewById(R.id.tvReward);
        tvWinner = findViewById(R.id.tvWinner);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserSingleScore = findViewById(R.id.tvUserSingleScore);
        tvUserScoreScore1 = findViewById(R.id.tvUserScoreScore1);
        tvUserScoreScore2 = findViewById(R.id.tvUserScoreScore2);
        tvUserTo = findViewById(R.id.tvUserTo);
        tvUserWinner = findViewById(R.id.tvUserWinner);
        tvRandom = findViewById(R.id.tvRandom);

        alUsers.clear();
        alUserEmails.clear();
        alScore.clear();
        alScore1.clear();
        alScore2.clear();
        alWinners.clear();
        alOgScore.clear();
        alOgScore1.clear();
        alOgScore2.clear();

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();

        setUpRecyclerView();


        getIncomingIntent();
        getUsers();



    }
    private void getIncomingIntent() {
        Log.d(TAG, "checking for incoming intent");
        if (getIntent().hasExtra("stake_name")&&getIntent().hasExtra("format")) {
            Log.d(TAG, "found intent extras");


            stakeName = getIntent().getStringExtra("stake_name");
            format = getIntent().getStringExtra("format");
            System.out.println(format);
            tvStakeName.setText(stakeName);
            if(format.equals("SingleScore")){

                tvSingleScore.setVisibility(View.VISIBLE);
                tvScoreScore1.setVisibility(View.GONE);
                tvScoreScore2.setVisibility(View.GONE);
                tvTo.setVisibility(View.GONE);
                tvUserSingleScore.setVisibility(View.VISIBLE);
                tvUserScoreScore1.setVisibility(View.GONE);
                tvUserScoreScore2.setVisibility(View.GONE);
                tvUserTo.setVisibility(View.GONE);


            }
            if(format.equals("ScoreVsScore")){

                tvSingleScore.setVisibility(View.GONE);
                tvScoreScore1.setVisibility(View.VISIBLE);
                tvScoreScore2.setVisibility(View.VISIBLE);
                tvTo.setVisibility(View.VISIBLE);
                tvUserSingleScore.setVisibility(View.GONE);
                tvUserScoreScore1.setVisibility(View.VISIBLE);
                tvUserScoreScore2.setVisibility(View.VISIBLE);
                tvUserTo.setVisibility(View.VISIBLE);

            }


        }
        getResult();
        checkForExistingWinner();
        getReward();

    }

    private void checkForExistingWinner() {

        getUserDocuments();
        new CollectionTask().execute();


        if(format.equals("SingleScore")){
            fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");

                            String winnerEmail = document.getString("winner_email");
                            String winnerWinner = document.getString("winner_winner");
                            String winnerScore = document.getString("winner_score");


                            tvUserEmail.setText(winnerEmail);
                            tvUserWinner.setText(winnerWinner);
                            tvUserSingleScore.setText(winnerScore);



                        }
                        else{
                            System.out.println("doc no exists");
                            new MyAsyncTask().execute();


                        }

                    }

                }
            });
        }
        if(format.equals("ScoreVsScore")){
            fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");

                            String winnerEmail = document.getString("winner_email");
                            String winnerWinner = document.getString("winner_winner");
                            String winnerScore1 = document.getString("winner_score1");
                            String winnerScore2 = document.getString("winner_score2");


                            tvUserEmail.setText(winnerEmail);
                            tvUserWinner.setText(winnerWinner);
                            tvUserScoreScore1.setText(winnerScore1);
                            tvUserScoreScore2.setText(winnerScore2);


                        }
                        else{
                            System.out.println("doc no exists");
                            new MyAsyncTask().execute();


                        }

                    }

                }
            });
        }

    }

    private void getResult(){
        if(format.equals("SingleScore")){
            fStore.collection("stakes").document(stakeName).collection("result").document("result").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");

                            resultWinner = document.getString("winner");
                            resultScore = document.getString("score");

                            tvWinner.setText(resultWinner);
                            tvSingleScore.setText(resultScore);


                        }
                        else{
                            System.out.println("doc no exists");

                        }

                    }

                }
            });
        }
        if(format.equals("ScoreVsScore")){
            fStore.collection("stakes").document(stakeName).collection("result").document("result").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document.exists()) {
                            System.out.println("doc exists");

                            resultWinner = document.getString("winner");
                            resultScore1 = document.getString("score1");
                            resultScore2 = document.getString("score2");

                            tvWinner.setText(resultWinner);
                            tvScoreScore1.setText(resultScore1);
                            tvScoreScore2.setText(resultScore2);


                        }
                        else{
                            System.out.println("doc no exists");

                        }

                    }

                }
            });
        }

    }
    private void getReward(){
        fStore.collection("stakes").document(stakeName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");

                        reward = document.getString("reward");

                        tvReward.setText(reward);


                    }
                    else{
                        System.out.println("doc no exists");

                    }

                }

            }
        });
    }
    private void getSubmits(){
        /*

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
                adapter = new WinnerAdapter(WinnerActivity.this, entryArrayList);
                recyclerView.setAdapter(adapter);
                new MyAsyncTask().execute();



            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WinnerActivity.this, "Problem - 1", Toast.LENGTH_LONG).show();
                        Log.d("Problem - 1", e.getMessage());

                    }
                });
                */
    }
    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            System.out.println("finished");

            arrayUsers = alUsers.toArray(new String[alUsers.size()]);
            arrayWinners = alWinners.toArray(new String[alUsers.size()]);
            arrayScores = convertIntegers(alScore);
            arrayScores1 = convertIntegers(alScore1);
            arrayScores2 = convertIntegers(alScore2);

            System.out.println("this is the array" + Arrays.toString(arrayWinners));
            checkIfPredictedWinner(alWinners,0);

            if(alUsers.size()==0){
                Toast.makeText(WinnerActivity.this, "No one won the stake", Toast.LENGTH_LONG).show();
                tvUserEmail.setText("N/A");
                tvUserWinner.setText("N/A");
                if(format.equals("SingleScore")){
                    tvUserSingleScore.setText("N/A");
                }
                if(format.equals("ScoreVsScore")){
                    tvUserScoreScore1.setText("N/A");
                    tvUserScoreScore2.setText("N/A");
                }
            }
            else{

                arrayUsers = alUsers.toArray(new String[alUsers.size()]);
                arrayWinners = alWinners.toArray(new String[alUsers.size()]);

                System.out.println("heres1" + Arrays.toString(arrayUsers));
                System.out.println("heres2" + Arrays.toString(arrayWinners));


                if(format.equals("SingleScore")){
                    arrayScores = convertIntegers(alScore);
                    System.out.println("heres3" + Arrays.toString(arrayScores));
                    calcDifference(alScore,0);
                    arrayScores = convertIntegers(alScore);
                    System.out.println("heres3" + Arrays.toString(arrayScores));

                    resultList = alScore;
                    tieCounter = 0;
                    tieList.clear();
                    int minIndex =  resultList.indexOf(Collections.min( resultList));
                    System.out.println("the list is " + resultList);

                    checkIfTie(resultList,resultList.get(minIndex));

                    if(tieCounter>1){
                        Toast.makeText(WinnerActivity.this, "There was a tie!", Toast.LENGTH_SHORT).show();
                        progRandom.setTitle("Randomly choosing winner");
                        progRandom.setMessage("Please wait...");
                        progRandom.show();

                        Random r = new Random();
                        int randomedUser = tieList.get(r.nextInt(tieList.size()));
                        System.out.println("random is"+ randomedUser);
                        tvRandom.setText(randomedUser+"");

                        new TieTask().execute();
                    }
                    else{
                        tvUserEmail.setText(alUsers.get(minIndex));
                        tvUserSingleScore.setText(""+alOgScore.get(minIndex));
                        tvUserWinner.setText(alWinners.get(minIndex));

                        Toast.makeText(WinnerActivity.this, "Congratulations "+alUsers.get(minIndex)+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                    }



                }
                if(format.equals("ScoreVsScore")){
                    arrayScores1 = convertIntegers(alScore1);
                    arrayScores2 = convertIntegers(alScore2);
                    System.out.println("heres4" + Arrays.toString(arrayScores1));
                    System.out.println("heres5" + Arrays.toString(arrayScores2 ));

                    calcDifference(alScore1,1);
                    calcDifference(alScore2,2);

                    arrayScores1 = convertIntegers(alScore1);
                    arrayScores2 = convertIntegers(alScore2);
                    System.out.println("heres4" + Arrays.toString(arrayScores1));
                    System.out.println("heres5" + Arrays.toString(arrayScores2 ));

                    for (int i = 0; i < alScore1.size(); i++) {
                        resultList.add(alScore1.get(i) + alScore2.get(i));
                    }

                    arrayScores1 = convertIntegers(alScore1);
                    arrayScores2 = convertIntegers(alScore2);
                    System.out.println("testing" + Arrays.toString(arrayScores1));
                    System.out.println("testing2" + Arrays.toString(arrayScores2 ));

                    tieCounter = 0;
                    tieList.clear();
                    int minIndex =  resultList.indexOf(Collections.min( resultList));

                    checkIfTie(resultList,resultList.get(minIndex));

                    if(tieCounter>1){
                        Toast.makeText(WinnerActivity.this, "There was a tie!", Toast.LENGTH_SHORT).show();
                        progRandom.setTitle("Randomly choosing winner");
                        progRandom.setMessage("Please wait...");
                        progRandom.show();

                        Random r = new Random();
                        int randomedUser = tieList.get(r.nextInt(tieList.size()));
                        tvRandom.setText(randomedUser+"");

                        new TieTask().execute();

                    }
                    else{
                        tvUserEmail.setText(alUsers.get(minIndex));
                        tvUserScoreScore1.setText(""+alOgScore1.get(minIndex));
                        tvUserScoreScore2.setText(""+alOgScore2.get(minIndex));
                        tvUserWinner.setText(alWinners.get(minIndex));

                        Toast.makeText(WinnerActivity.this, "Congratulations "+alUsers.get(minIndex)+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                    }


                }
            }






        }

    }
    private void getUsers(){
        fStore.collection("stakes").document(stakeName).collection("submits").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        usernameList.add(document.getId());

                    }
                    arrayUsers = usernameList.toArray(new String[usernameList.size()]);
                    System.out.println(Arrays.toString(arrayUsers));
                    addressUsers(arrayUsers,0,0);

                    Log.d(TAG, usernameList.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    public String addressUsers(String[] var, int index, int n){
        if(n==0){
            if(index<var.length){
                getUserInformation(var[index]);
                return "" + addressUsers(var,index +1,n);
            }
            else{
                System.out.println("finished");
                return "";
            }
        }
        else{
            System.out.println("6");
            return "";
        }


    }
    private void getUserInformation(final String useremail){
        fStore.collection("stakes").document(stakeName).collection("submits").document(useremail).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");
                        if(format.equals("SingleScore")){
                            System.out.println("over here");
                            String userWinner = document.getString("winner");
                            System.out.println(userWinner);
                            int userScore = Integer.parseInt(document.getString("score"));
                            alUsers.add(useremail);
                            alWinners.add(userWinner);
                            alScore.add(userScore);
                            alOgScore.add(userScore);

                        }
                        if(format.equals("ScoreVsScore")){
                            System.out.println("over there");
                            String userWinner = document.getString("winner");
                            System.out.println(userWinner);
                            int userScore1 = Integer.parseInt(document.getString("score1"));
                            int userScore2 = Integer.parseInt(document.getString("score2"));
                            alUsers.add(useremail);
                            alWinners.add(userWinner);

                            alScore1.add(userScore1);
                            alScore2.add(userScore2);
                            alOgScore1.add(userScore1);
                            alOgScore2.add(userScore2);
                        }
                        else{
                            System.out.println("over where");
                        }



                    }
                    else{
                        System.out.println("doc no exists");

                    }

                }

            }
        });

    }
    public int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    private String checkIfPredictedWinner(List<String> var, int n){

        System.out.println("this is the 3rd array" + Arrays.toString( alWinners.toArray(new String[alUsers.size()])));

        if(n<=var.size()-1){
            if (!var.get(n).equals(resultWinner)) {
                System.out.println(alUsers.get(n) + "'s" + var.get(n) + " does not equal " + resultWinner);
                alUsers.remove(n);
                alWinners.remove(n);

                if (format.equals("SingleScore")) {
                    alScore.remove(n);
                    alOgScore.remove(n);
                }
                if (format.equals("ScoreVsScore")) {
                    alScore1.remove(n);
                    alScore2.remove(n);
                    alOgScore1.remove(n);
                    alOgScore2.remove(n);
                }
                return "" + checkIfPredictedWinner(var,n);
            }
            else{
                System.out.println(alUsers.get(0) + "'s" + var.get(0)+" does equal " + resultWinner);
                return "" + checkIfPredictedWinner(var,n+1);

            }
        }
        else{
            return "";
        }




        /*
        System.out.println("this is the 2nd array" + Arrays.toString( alWinners.toArray(new String[alWinners.size()])));
        for (int n = 0; n <= var.size()-1; n++) {

            System.out.println("the size is" + (var.size()-1) + "");
            System.out.println("n is" + (n) + "");
            System.out.println("this is the 3rd array" + Arrays.toString( alWinners.toArray(new String[alWinners.size()])));
            System.out.println(alUsers.get(n) + "'s" + var.get(n) + " and " + resultWinner);

            if (!var.get(n).equals(resultWinner)) {

                System.out.println(alUsers.get(n) + "'s" + var.get(n) + " does not equal " + resultWinner);

                alUsers.remove(n);

                if (format.equals("SingleScore")) {
                    alScore.remove(n);
                    alOgScore.remove(n);
                }
                if (format.equals("ScoreVsScore")) {
                    alScore1.remove(n);
                    alScore2.remove(n);
                    alOgScore1.remove(n);
                    alOgScore2.remove(n);
                }

            }
            else{

                System.out.println(alUsers.get(n) + "'s" + var.get(n)+" does equal " + resultWinner);


            }
        }
        */



    }
    private void calcDifference(List<Integer> var,int n){
        System.out.println("we got here");
        if(n==0){
            for (int i = 0; i <= var.size()-1; i++) {
                int val = var.get(i);//get the value
                var.set(i, (Math.abs(Integer.parseInt(resultScore)-val)));
            }
        }
        if(n==1){
            System.out.println("we got here2");
            for (int i = 0; i <= var.size()-1; i++) {
                System.out.println("we got here3");
                int val = var.get(i);//get the value
                var.set(i, (Math.abs(Integer.parseInt(resultScore1)-val)));
                System.out.println(Math.abs(Integer.parseInt(resultScore1)-val));
            }
        }
        if(n==2){
            for (int i = 0; i <= var.size()-1; i++) {
                int val = var.get(i);//get the value
                var.set(i, (Math.abs(Integer.parseInt(resultScore2)-val)));
                System.out.println(Math.abs(Integer.parseInt(resultScore2)-val));
            }
        }

    }

    private void checkIfTie(List<Integer> var, int valueToCompare){
        for (int i = 0; i <= var.size()-1; i++) {
            if(var.get(i)==(valueToCompare)){
                tieCounter++;
                tieList.add(i);
            }

        }
    }
    private class TieTask extends AsyncTask<Void, Void, Void> {
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
            if(collectionExists){
                System.out.println("collection exists");
                if(format.equals("SingleScore")){
                    tvUserEmail.setText(existingWinnerEmail);
                    tvUserSingleScore.setText(existingWinnerScore);
                    tvUserWinner.setText(existingWinnerWinner);

                    Map<String, Object> note = new HashMap<>();
                    if(user!=null){
                        note.put("winner_email",existingWinnerEmail);
                        note.put("winner_winner",existingWinnerWinner);
                        note.put("winner_score",existingWinnerScore);


                        fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG,stakeName + " was saved!");

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                        Log.d(stakeName + " failed to save!", e.toString());
                                    }
                                });
                    }

                    progRandom.dismiss();
                    Toast.makeText(WinnerActivity.this, "Congratulations "+existingWinnerEmail+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                }
                if(format.equals("ScoreVsScore")){
                    tvUserEmail.setText(existingWinnerEmail);
                    tvUserScoreScore1.setText(existingWinnerScore1);
                    tvUserScoreScore2.setText(existingWinnerScore2);
                    tvUserWinner.setText(existingWinnerWinner);

                    Map<String, Object> note = new HashMap<>();
                    if(user!=null){
                        note.put("winner_email",existingWinnerEmail);
                        note.put("winner_winner", existingWinnerWinner);
                        note.put("winner_score1", existingWinnerScore1);
                        note.put("winner_score2", existingWinnerScore2);


                        fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG,stakeName + " was saved!");

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                        Log.d(stakeName + " failed to save!", e.toString());
                                    }
                                });
                    }

                    progRandom.dismiss();

                    Toast.makeText(WinnerActivity.this, "Congratulations "+existingWinnerEmail+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                }


            }
            else{
                System.out.println("collection no exists");
                if(format.equals("SingleScore")){
                    System.out.println("random again is"+ Integer.parseInt(tvRandom.getText().toString()));
                    tvUserEmail.setText(alUsers.get(Integer.parseInt(tvRandom.getText().toString())));
                    System.out.println("grats "+ alUsers.get(Integer.parseInt(tvRandom.getText().toString())));
                    tvUserSingleScore.setText(""+alOgScore.get(Integer.parseInt(tvRandom.getText().toString())));
                    tvUserWinner.setText(alWinners.get(Integer.parseInt(tvRandom.getText().toString())));

                    Map<String, Object> note = new HashMap<>();
                    if(user!=null){
                        note.put("winner_email",alUsers.get(Integer.parseInt(tvRandom.getText().toString())));
                        note.put("winner_winner", alWinners.get(Integer.parseInt(tvRandom.getText().toString())));
                        note.put("winner_score", alOgScore.get(Integer.parseInt(tvRandom.getText().toString()))+"");


                        fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG,stakeName + " was saved!");

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                        Log.d(stakeName + " failed to save!", e.toString());
                                    }
                                });
                    }

                    progRandom.dismiss();
                    Toast.makeText(WinnerActivity.this, "Congratulations "+alUsers.get(Integer.parseInt(tvRandom.getText().toString()))+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                }
                if(format.equals("ScoreVsScore")){
                    tvUserEmail.setText(alUsers.get(Integer.parseInt(tvRandom.getText().toString())));
                    tvUserScoreScore1.setText(""+alOgScore1.get(Integer.parseInt(tvRandom.getText().toString())));
                    tvUserScoreScore2.setText(""+alOgScore2.get(Integer.parseInt(tvRandom.getText().toString())));
                    tvUserWinner.setText(alWinners.get(Integer.parseInt(tvRandom.getText().toString())));

                    Map<String, Object> note = new HashMap<>();
                    if(user!=null){
                        note.put("winner_email",alUsers.get(Integer.parseInt(tvRandom.getText().toString())));
                        note.put("winner_winner", alWinners.get(Integer.parseInt(tvRandom.getText().toString())));
                        note.put("winner_score1", alOgScore1.get(Integer.parseInt(tvRandom.getText().toString()))+"");
                        note.put("winner_score2", alOgScore2.get(Integer.parseInt(tvRandom.getText().toString()))+"");


                        fStore.collection("stakes").document(stakeName).collection("winner").document(user.getEmail()).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Log.d(TAG,stakeName + " was saved!");

                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {


                                        Log.d(stakeName + " failed to save!", e.toString());
                                    }
                                });
                    }

                    progRandom.dismiss();

                    Toast.makeText(WinnerActivity.this, "Congratulations "+alUsers.get(Integer.parseInt(tvRandom.getText().toString()))+",you won "+ reward + "!", Toast.LENGTH_LONG).show();
                }





            }

        }

    }
    private void getUserDocuments(){
        fStore.collection("user_account").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        documentList.add(document.getId());


                    }
                    arrayDocuments = documentList.toArray(new String[documentList.size()]);
                    System.out.println(Arrays.toString( arrayDocuments));
                    System.out.println( arrayDocuments.length);
                    addressUserDocuments( arrayDocuments);
                    Log.d(TAG, documentList.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    public void addressUserDocuments(String[] var){

        for (int i = 0; i < var.length; i++)
        {
            System.out.println("here we go");
            getUserEmail(var[i]);
        }



    }
    private void getUserEmail(final String documentname){
        fStore.collection("user_account").document(documentname).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");
                        try{
                            String thisUserEmail = document.getString("email");
                            System.out.println(thisUserEmail);
                            alUserEmails.add(thisUserEmail);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }


                    }
                    else{
                        System.out.println("doc no exists");

                    }

                }

            }
        });

    }
    public void addressUserEmails(String[] var){

        for (int i = 0; i < var.length; i++)
        {
            System.out.println("here we go");
            checkIfDocumentExists(var[i]);
        }



    }

    private void checkIfDocumentExists(String s) {
        fStore.collection("stakes").document(stakeName).collection("winner").document(s).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        collectionExists = true;
                        if(format.equals("SingleScore")){
                            existingWinnerEmail = document.getString("winner_email");
                            existingWinnerWinner = document.getString("winner_winner");
                            existingWinnerScore = document.getString("winner_score");




                        }
                        if(format.equals("ScoreVsScore")){
                            existingWinnerEmail = document.getString("winner_email");
                            existingWinnerWinner = document.getString("winner_winner");
                            existingWinnerScore1 = document.getString("winner_score1");
                            existingWinnerScore2 = document.getString("winner_score2");



                        }


                    }
                    else{
                        System.out.println("doc no exists");

                    }

                }

            }
        });
    }

    private class CollectionTask extends AsyncTask<Void, Void, Void> {
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

            arrayUserEmails = alUserEmails.toArray(new String[alUserEmails.size()]);
            addressUserEmails(arrayUserEmails);
        }

    }

}
