package com.dz.swept;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateStakeActivity extends AppCompatActivity implements View.OnClickListener  {

    Button btnSaveStake, btnNewInvitee;
    ToggleButton tglFormat;
    EditText etParticipants, etStakeName, etReward;
    ImageView imgHome;
    RecyclerView recyclerInvitee;
    ArrayAdapter<String> adapter;
    Spinner spnInvitees;

    public Spinner spnNewInvitee;
    public TextView tvFillerSpace;

    List<String> list = new ArrayList<>();
    List<String> usernameList;
    ArrayList<String> alUsers;
    ArrayAdapter<String> suggestions;
    String[] arrayUsers;


    String[] arrayParticipants, arrayInvitees;
    String stakeName, participants, invitees, format, reward, filler, cleanedString, invites, spinnerValue;
    public int numberInvitees = 0;
    Boolean nameChanged, participantsChanged, inviteesChanged, formatChanged, rewardChanged, emailExists, emailChecked;
    final String TAG = "CreateStakeActivity";

    ProgressDialog progSave, progCreate;

    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    ArrayList<String> out = new ArrayList<String>();
    public ArrayList<String> items = new ArrayList<String>();

    Map<String, Spinner> mapSpn = new HashMap<>();
    Map<String, String> mapSpnValue = new HashMap<>();


    RecyclerView.Adapter mAdapter;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_stake);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line);

        usernameList = new ArrayList<>();
        alUsers = new ArrayList<>();

        usernameList.clear();
        alUsers.clear();

        alUsers.add("");

        btnSaveStake = findViewById(R.id.btnSaveStake);
        btnNewInvitee = findViewById(R.id.btnNewInvitee);
        tglFormat = findViewById(R.id.tglFormat);
        spnInvitees = findViewById(R.id.spnInvitees);
        etParticipants = findViewById(R.id.etParticipants);
        etStakeName = findViewById(R.id.etStakeName);
        etReward= findViewById(R.id.etReward);
        imgHome= findViewById(R.id.imgHome);


        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();


        getUserDocuments();

        new MyAsyncTask2().execute();




        progSave = new ProgressDialog(this);
        progCreate = new ProgressDialog(this);

        btnSaveStake.setOnClickListener(this);
        btnNewInvitee.setOnClickListener(this);
        imgHome.setOnClickListener(this);
        tglFormat.setOnClickListener(this);

        spnInvitees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                spinnerValue = adapterView.getItemAtPosition(i).toString();
                System.out.println(spinnerValue );
                inviteesChanged= true;
                System.out.println("we changed");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        etReward.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                rewardChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etStakeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nameChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        etParticipants.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                participantsChanged = true;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        reset();
        setUpRecyclerView();

    }

    @Override
    public void onClick(View view) {

        if(view==btnSaveStake){


            format = tglFormat.getText().toString();
            stakeName = etStakeName.getText().toString();
            participants = etParticipants.getText().toString();
            invitees = spinnerValue;
            reward = etReward.getText().toString();

                if(TextUtils.isEmpty(stakeName)||TextUtils.isEmpty(participants)||TextUtils.isEmpty(invitees)||TextUtils.isEmpty(reward)){
                    Toast.makeText(this, "One of the fields is missing.",Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    try{
                        saveStake();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                }




        }
        if(view==imgHome){
            format = tglFormat.getText().toString();
            stakeName = etStakeName.getText().toString();
            participants = etParticipants.getText().toString();
            invitees = spinnerValue;
            reward = etReward.getText().toString();


                if(items.size()>0){
                    for(int n = 1;n<=items.size();n++){
                        System.out.println(mapSpnValue.get("spn"+Integer.toString(n)));
                        if(!mapSpnValue.get("spn"+Integer.toString(n)).equals("")||
                                !TextUtils.isEmpty(stakeName)||!TextUtils.isEmpty(participants)||!TextUtils.isEmpty(invitees)||!TextUtils.isEmpty(reward)||formatChanged){
                            System.out.println("trial0");
                            createDialog();

                        }
                        if(n==items.size()&&mapSpnValue.get("spn"+Integer.toString(n)).equals("")&&
                                TextUtils.isEmpty(stakeName)&&TextUtils.isEmpty(participants)&&TextUtils.isEmpty(invitees)&&TextUtils.isEmpty(reward)&&!formatChanged){
                            System.out.println("trial1");
                            startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));
                        }

                    }
                }
                else{
                    if((!TextUtils.isEmpty(stakeName)||!TextUtils.isEmpty(participants)||!TextUtils.isEmpty(invitees)||!TextUtils.isEmpty(reward)||formatChanged)){
                        createDialog();
                    }
                    else{
                        System.out.println("trial2");
                        startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));
                    }
                }



        }
        if(view==tglFormat){
            formatChanged = true;
        }
        if(view==btnNewInvitee){
           addInvitee();


        }

    }
    private void saveStake(){
        progCreate.setTitle("Creating Stake");
        progCreate.setMessage("Please wait...");
        progCreate.show();



        Map<String, Object> note = new HashMap<>();
        if(user!=null){
            note.put("stake_name",stakeName);
            note.put("format", format);
            note.put("creator", user.getEmail());
            note.put("status", "Ongoing");
            note.put("participants", removeSpaces(participants));
            note.put("reward",reward);



            fStore.collection("stakes").document(stakeName).set(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {


                    Log.d(TAG,stakeName + " was saved!");
                    createStake();

                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                            Log.d(stakeName + " failed to save!", e.toString());
                        }
                    });
        }


        reset();


    }
    private void reset(){

        nameChanged = false;
        participantsChanged = false;
        inviteesChanged = false;
        formatChanged = false;
        rewardChanged = false;
        emailExists = false;
        emailChecked = false;
        filler = "";
        cleanedString = "";


    }
    public void createDialog() {


            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You have unsaved changes. Are you sure you want to exit?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }

            });
            alert.create().show();

    }
    @Override
    public void onBackPressed() {
        format = tglFormat.getText().toString();
        stakeName = etStakeName.getText().toString();
        participants = etParticipants.getText().toString();
        invitees = spinnerValue;
        reward = etReward.getText().toString();


        if(items.size()>0){
            for(int n = 1;n<=items.size();n++){
                System.out.println(mapSpnValue.get("spn"+Integer.toString(n)));
                if(!mapSpnValue.get("spn"+Integer.toString(n)).equals("")||
                        !TextUtils.isEmpty(stakeName)||!TextUtils.isEmpty(participants)||!TextUtils.isEmpty(invitees)||!TextUtils.isEmpty(reward)||formatChanged){
                    System.out.println("trial0");
                    createDialog();

                }
                if(n==items.size()&&mapSpnValue.get("spn"+Integer.toString(n)).equals("")&&
                        TextUtils.isEmpty(stakeName)&&TextUtils.isEmpty(participants)&&TextUtils.isEmpty(invitees)&&TextUtils.isEmpty(reward)&&!formatChanged){
                    System.out.println("trial1");
                    startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));
                }

            }
        }
        else{
            if((!TextUtils.isEmpty(stakeName)||!TextUtils.isEmpty(participants)||!TextUtils.isEmpty(invitees)||!TextUtils.isEmpty(reward)||formatChanged)){
                createDialog();
            }
            else{
                System.out.println("trial2");
                startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));
            }
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
        if(!var.substring(0,1).equals(",")&&!var.substring(0,1).equals(" ")){
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
    public String addressSeparations(final String[] var, int index, int n){
        emailExists = false;
        if(n==0){
            if(index<var.length){
              checkIfEmailExists(var[index]);
              return "" + addressSeparations(var,index +1,n);
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
    private void checkIfEmailExists(final String emailToCompare) {


            //----------------------------------------------------------------
            final Query mQuery = fStore.collection("user_account").whereEqualTo("email", emailToCompare);
            mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.d(TAG, "checking if " + emailToCompare + " already exists.");

                    if (task.isSuccessful()) {
                        for (DocumentSnapshot ds : task.getResult()) {
                            String emails = ds.getString("email");
                            if (emails.equals(emailToCompare)) {
                                Log.d(TAG, emailToCompare + " exists.");
                                System.out.println(emailToCompare + " email found");
                                emailExists = true;
                                System.out.println(emailToCompare + " invited!");

                                Map<String, Object> note2 = new HashMap<>();
                                fStore.collection("stakes").document(stakeName).collection("guests").document(emailToCompare).set(note2);

                            }
                        }
                    }
                    //checking if task contains any payload. if no, then update
                    if (task.getResult().size() == 0) {
                        emailExists = false;
                        try {

                            Log.d(TAG, "onComplete: MATCH NOT FOUND - email not found");
                            System.out.println(emailToCompare + " email not found");
                            //Updating new username............




                        } catch (NullPointerException e) {
                            Log.e(TAG, "NullPointerException: " + e.getMessage());
                        }
                    }
                }
            });




    }
    private void checkIfUsernameExists(final String usernameToCompare) {


        //----------------------------------------------------------------
        final Query mQuery = fStore.collection("user_account").whereEqualTo("username", usernameToCompare);
        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.d(TAG, "checking if " + usernameToCompare + " already exists.");

                if (task.isSuccessful()) {
                    for (DocumentSnapshot ds : task.getResult()) {
                        String un = ds.getString("email");
                        if (un.equals(usernameToCompare)) {
                            Log.d(TAG, usernameToCompare + " exists.");
                            System.out.println(usernameToCompare + " username found");
                            System.out.println(usernameToCompare + " invited!");

                            Map<String, Object> note2 = new HashMap<>();
                            fStore.collection("stakes").document(stakeName).collection("guests").document(usernameToCompare).set(note2);

                        }
                    }
                }
                //checking if task contains any payload. if no, then update
                if (task.getResult().size() == 0) {
                    try {

                        Log.d(TAG, "onComplete: MATCH NOT FOUND - email not found");
                        System.out.println(usernameToCompare + " email not found");
                        //Updating new username............




                    } catch (NullPointerException e) {
                        Log.e(TAG, "NullPointerException: " + e.getMessage());
                    }
                }
            }
        });




    }
    private void createStake(){


        /*
        addressSeparations(separateString(invitees),0,0);

        */
        /*
        checkIfEmailExists(etInvitees.getText().toString());

        for(int n = 1;n<=items.size();n++){
            System.out.println(mapEt.get("et"+Integer.toString(n)).getText().toString());
            checkIfEmailExists(mapEt.get("et"+Integer.toString(n)).getText().toString());

        }
        */
        Map<String, Object> note2 = new HashMap<>();
        fStore.collection("stakes").document(stakeName).collection("guests").document(spinnerValue).set(note2);
        for(int n = 1;n<=items.size();n++){
            if(!mapSpnValue.get("spn"+Integer.toString(n)).equals("")){
                System.out.println(mapSpnValue.get("spn"+Integer.toString(n)));
                Map<String, Object> note3 = new HashMap<>();
                fStore.collection("stakes").document(stakeName).collection("guests").document((mapSpnValue.get("spn"+Integer.toString(n)))).set(note3);
            }

        }



        new MyAsyncTask().execute();


        Toast.makeText(CreateStakeActivity.this, stakeName + " was created!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(CreateStakeActivity.this, MainActivity.class));

        progCreate.dismiss();

        finish();



    }
    private String removeSpaces(String var){
        if(var.length()==0){
            String idk = cleanedString;
            cleanedString = "";
            return idk;
        }
        if(var.substring(0,1).equals(" ")){
            return removeSpaces(var.substring(1));
        }
        else{
            cleanedString += var.substring(0,1);
            return removeSpaces(var.substring(1));
        }

    }

    private void addInvitee(){

        new mapTask().execute();



    }
    private void setUpRecyclerView() {
        recyclerInvitee = findViewById(R.id.recyclerInvitee);
        recyclerInvitee.setHasFixedSize(true);
        recyclerInvitee.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RecyclerView.Adapter<CustomViewHolder>() {
            @Override
            public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invitee_item
                        , viewGroup, false);
                return new CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(CustomViewHolder viewHolder, int i) {

            }

            @Override
            public int getItemCount() {
                return items.size();
            }

        };
        recyclerInvitee.setAdapter(mAdapter);


    }
    private class CustomViewHolder extends RecyclerView.ViewHolder {




        public CustomViewHolder(View itemView) {
            super(itemView);

            spnNewInvitee = itemView.findViewById(R.id.spnNewInvitee);
            spnNewInvitee.setAdapter(suggestions);
            spnNewInvitee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    String newSpinnerValue = adapterView.getItemAtPosition(i).toString();
                    System.out.println(newSpinnerValue);
                    mapSpnValue.put("spn"+Integer.toString(items.size()), newSpinnerValue);


                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            tvFillerSpace = itemView.findViewById(R.id.tvFillerSpace);
            spnNewInvitee.setId(items.size());
            mapSpn.put("spn"+Integer.toString(items.size()),spnNewInvitee);


        }
    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
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

            fStore
                    .collection("stakes").document(stakeName).collection("guests")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                list.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    list.add(document.getId());
                                    System.out.println(list);
                                }
                                String[] array = list.toArray(new String[list .size()]);
                                invites = Arrays.toString(array).substring(1,Arrays.toString(array).length()-1);
                                System.out.println("testing this" + invites);


                                Map<String, Object> note = new HashMap<>();
                                if(user!=null){
                                    note.put("invitees",invites);

                                    fStore.collection("stakes").document(stakeName).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {


                                            Log.d(TAG,"invitees saved!");


                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {


                                                    Log.d("invitee failed to save!", e.toString());
                                                }
                                            });
                                }
                            }
                        }
                    });

        }
    }
    private class mapTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            items.add("Item");
            mAdapter.notifyItemInserted(items.size() - 1);


        }
    }
    private void getUserDocuments(){
        fStore.collection("user_account").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        usernameList.add(document.getId());


                    }
                    arrayUsers = usernameList.toArray(new String[usernameList.size()]);
                    System.out.println(Arrays.toString(arrayUsers));
                    System.out.println(arrayUsers.length);
                    addressUsers(arrayUsers);
                    Log.d(TAG, usernameList.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    public void addressUsers(String[] var){

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
                            String newUserEmail = document.getString("email");
                            System.out.println(newUserEmail);
                            alUsers.add(newUserEmail);
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
    private class MyAsyncTask2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            suggestions = new ArrayAdapter<String>(CreateStakeActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, alUsers.toArray(new String[alUsers.size()]));
            System.out.println("thy " +Arrays.toString(alUsers.toArray(new String[alUsers.size()])));

            System.out.println("this is " +  alUsers.toArray(new String[alUsers.size()]));
            spnInvitees.setAdapter(suggestions);
        }
    }





}
