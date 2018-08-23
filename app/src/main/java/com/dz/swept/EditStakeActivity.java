package com.dz.swept;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStakeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditStakeActivity";

    MainActivity mainActivity;
    ProgressDialog progUpdate;
    ProgressDialog progSetup;


    String stakeName, status, invitees, reward,filler, invites,spinnerValue;
    Boolean rewardChanged,statusChanged,finished, finishedChanged, inviteesChanged;

    List<String> list = new ArrayList<>();
    List<String> usernameList;
    ArrayList<String> alUsers;
    ArrayAdapter<String> suggestions;
    String[] arrayUsers;

    public Spinner spnNewInvitee;
    public TextView tvFillerSpace;

    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    TextView tvStakeName, tvFinished;
    ToggleButton tglStatus;
    Button btnNewInvitee, btnUpdateStake, btnComplete, btnUnfinish;
    ImageView imgHome, imgTrash;
    EditText etReward;
    Spinner spnInvitees;
    RecyclerView recyclerInvitee;

    public ArrayList<String> items = new ArrayList<String>();
    ArrayList<String> out = new ArrayList<String>();

    Map<String, Spinner> mapSpn = new HashMap<>();
    Map<String, String> mapSpnValue = new HashMap<>();



    RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stake);
        Log.d(TAG,"oncreate started");

        progSetup = new ProgressDialog(this);

        progSetup.setTitle("Retrieving stake");
        progSetup.setMessage("Please wait...");
        progSetup.show();

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        user = fAuth.getCurrentUser();


        usernameList = new ArrayList<>();
        alUsers = new ArrayList<>();

        usernameList.clear();
        alUsers.clear();

        alUsers.add("");

        tglStatus = findViewById(R.id.tglStatus);
        btnNewInvitee = findViewById(R.id.btnNewInvitee);
        btnUpdateStake = findViewById(R.id.btnUpdateStake);
        btnComplete = findViewById(R.id.btnComplete);
        btnUnfinish = findViewById(R.id.btnUnfinish);
        imgHome = findViewById(R.id.imgHome);
        imgTrash = findViewById(R.id.imgTrash);
        tvFinished = findViewById(R.id.tvFinished);
        spnInvitees = findViewById(R.id.spnInvitees);

        getUserDocuments();

        new MyAsyncTask2().execute();

        imgHome.setOnClickListener(this);
        btnNewInvitee.setOnClickListener(this);
        btnUpdateStake.setOnClickListener(this);
        imgTrash.setOnClickListener(this);
        btnComplete .setOnClickListener(this);
        tglStatus.setOnClickListener(this);
        btnUnfinish.setOnClickListener(this);

        progUpdate = new ProgressDialog(this);


        finished = false;
        finishedChanged = false;
        reset();
        getIncomingIntent();
        setUpRecyclerView();
        checkStatus();

        addressSeparations(separateString(invitees),0,0);
        filler = "";
        out.clear();


        System.out.println(items.size()+"");

        new MyAsyncTask().execute();


    }

    private void setUpRecyclerView() {
        recyclerInvitee = findViewById(R.id.recyclerInvitee);
        recyclerInvitee.setHasFixedSize(true);
        recyclerInvitee.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new RecyclerView.Adapter<EditStakeActivity.CustomViewHolder>() {
            @Override
            public EditStakeActivity.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.invitee_item
                        , viewGroup, false);
                return new EditStakeActivity.CustomViewHolder(view);
            }

            @Override
            public void onBindViewHolder(EditStakeActivity.CustomViewHolder viewHolder, int i) {

            }

            @Override
            public int getItemCount() {
                return items.size();
            }

        };
        recyclerInvitee.setAdapter(mAdapter);


    }

    @Override
    public void onClick(View view) {
        if(view==imgHome){
            if(finished&&!finishedChanged){
                if(rewardChanged){
                    createDialog(0);
                }
                else{
                    startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                    finish();
                }
            }
            else{
                if(statusChanged||rewardChanged||finishedChanged){
                    createDialog(0);
                }
                else{
                    startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                    finish();
                }
            }
        }
        if(view==btnComplete){
            Intent intent = new Intent(this,ResolveActivity.class);
            intent.putExtra("stake_name",stakeName);

            EditStakeActivity.this.startActivity(intent);

        }
        if(view==btnUpdateStake){
            updateStake();

        }
        if(view==btnNewInvitee){
            addInvitee();

        }
        if(view==imgTrash){
            createDialog(1);
        }
        if(view==tglStatus){
            statusChanged = true;
        }
        if(view==btnUnfinish){
            finishedChanged = true;
            btnUnfinish.setVisibility(View.GONE);
            tvFinished.setVisibility(View.GONE);
            tglStatus.setVisibility(View.VISIBLE);
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {


        public CustomViewHolder(View itemView) {
            super(itemView);

            /*
            etNewInvitee = itemView.findViewById(R.id.etNewInvitee);
            tvFillerSpace = itemView.findViewById(R.id.tvFillerSpace);
            etNewInvitee.setId(items.size());
            System.out.println(items.size());
            mapEt.put("et"+Integer.toString(items.size()),etNewInvitee);
            System.out.println("map updated" + mapEt.size());
            */

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

    private void getIncomingIntent(){
        Log.d(TAG,"checking for incoming intent");
        if(getIntent().hasExtra("stake_name")&&getIntent().hasExtra("status")&&
                getIntent().hasExtra("invitees")&&getIntent().hasExtra("reward")){

            Log.d(TAG, "found intent extras");

            stakeName = getIntent().getStringExtra("stake_name");
            status = getIntent().getStringExtra("status");
            invitees = getIntent().getStringExtra("invitees");
            reward = getIntent().getStringExtra("reward");




            new MyAsyncTask3().execute();

        }


    }
    private void setIntent(String stakeName, String status, String invitees, String reward){
        Log.d(TAG, "setting intent");

        tvStakeName = findViewById(R.id.tvStakeName);
        etReward = findViewById(R.id.etReward);



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

        int spinnerPosition = suggestions.getPosition(separateString(invitees)[0]);
        System.out.println("tho changed");
        spnInvitees.setSelection(spinnerPosition);

        spnInvitees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                spinnerValue = adapterView.getItemAtPosition(i).toString();
                System.out.println(spinnerValue );
                inviteesChanged = true;
                System.out.println("we changed");


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        out.clear();
        filler = "";

        tvStakeName.setText(stakeName);
        etReward.setText(reward);
        rewardChanged = false;

        if(status.equals("Ongoing")){
            tglStatus.setChecked(false);
        }
        else{
            tglStatus.setChecked(true);
        }

    }
    public void createDialog(int n) {

        if(n==0){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You have unsaved changes. Are you sure you want to exit?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                    finish();


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
            alert.setMessage("Are you sure you want to delete this stake?It will not be recoverable.");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    fStore.collection("stakes").document(stakeName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditStakeActivity.this,stakeName + " was deleted!",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                            finish();


                        }
                    });
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
    private void addInvitee() {
        new mapTask().execute();

    }
    private void reset(){
        statusChanged = false;
        rewardChanged = false;
        inviteesChanged = false;
        filler="";
    }
    private void updateStake() {
        progUpdate.setTitle("Updating stake");
        progUpdate.setMessage("Please wait...");
        progUpdate.show();

        if(finished&&!finishedChanged){
            Map<String, Object> note = new HashMap<>();
            System.out.println(user.getEmail());
            note.put("reward", etReward.getText().toString());

            fStore.collection("stakes").document(stakeName).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Map<String, Object> note2 = new HashMap<>();
                    fStore.collection("stakes").document(stakeName).collection("guests").document(spinnerValue).set(note2);
                    for(int n = 1;n<=items.size();n++){
                        if(!mapSpnValue.get("spn"+Integer.toString(n)).equals("")){
                            System.out.println(mapSpnValue.get("spn"+Integer.toString(n)));
                            Map<String, Object> note3 = new HashMap<>();
                            fStore.collection("stakes").document(stakeName).collection("guests").document((mapSpnValue.get("spn"+Integer.toString(n)))).set(note3);
                        }

                    }


                    new updateInviteesTask().execute();

                    Toast.makeText(EditStakeActivity.this, stakeName + " was updated!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                    progUpdate.dismiss();
                    finish();



                    Log.d(TAG, "stake updated!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progUpdate.dismiss();


                            Log.d( " failed 2 update stake!", e.toString());
                        }
                    });
        }
        else{
            Map<String, Object> note = new HashMap<>();
            System.out.println(user.getEmail());
            note.put("reward", etReward.getText().toString());
            note.put("status", tglStatus.getText().toString());

            fStore.collection("stakes").document(stakeName).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Map<String, Object> note2 = new HashMap<>();
                    fStore.collection("stakes").document(stakeName).collection("guests").document(spinnerValue).set(note2);
                    for(int n = 1;n<=items.size();n++){
                        if(!mapSpnValue.get("spn"+Integer.toString(n)).equals("")){
                            System.out.println(mapSpnValue.get("spn"+Integer.toString(n)));
                            Map<String, Object> note3 = new HashMap<>();
                            fStore.collection("stakes").document(stakeName).collection("guests").document((mapSpnValue.get("spn"+Integer.toString(n)))).set(note3);
                        }

                    }


                    new updateInviteesTask().execute();

                    Toast.makeText(EditStakeActivity.this, stakeName + " was updated!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                    progUpdate.dismiss();
                    finish();



                    Log.d(TAG, "stake updated!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progUpdate.dismiss();


                            Log.d( " failed 2 update stake!", e.toString());
                        }
                    });

        }




    }

    @Override
    public void onBackPressed() {
        if(finished&&!finishedChanged){
            if(rewardChanged){
                createDialog(0);
            }
            else{
                startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                finish();
            }
        }
        else{
            if(statusChanged||rewardChanged||finishedChanged){
                createDialog(0);
            }
            else{
                startActivity(new Intent(EditStakeActivity.this, MainActivity.class));
                finish();
            }
        }

    }
    public String addressSeparations(final String[] var, int index, int n){
        if(n==0){
            if(index<var.length-1){
                addInvitee();
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
    public String[] separateString(String var){
        if(var.length()==0&&!filler.equals("")){
            out.add(filler);
            filler = "";
            String[] arraySeparated = out.toArray(new String[out.size()]);
            System.out.println("1" +Arrays.toString(arraySeparated));
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
    private void setUpInvites(){

        /*
        if(items.size()>0){
            for(int n = 1;n<=items.size();n++){
                mapSpn.get("spn"+Integer.toString(n)).setText(separateString(invitees)[n]);



            }
        }
        else{
            System.out.println("there");
        }
        */
        if(items.size()>0){
            for(int n = 1;n<=items.size();n++){
                int spinnerPosition2 = suggestions.getPosition(separateString(invitees)[n]);
                mapSpn.get("spn"+Integer.toString(n)).setSelection(spinnerPosition2);
            }
        }
        else{
            System.out.println("there");
        }




    }
    private class MyAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < 1; i++) {
                try {
                    Thread.sleep((mapSpn.size()+1)*200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            setUpInvites();
            progSetup.dismiss();


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

                            System.out.println(emailToCompare + " invited!");

                            Map<String, Object> note2 = new HashMap<>();
                            fStore.collection("stakes").document(stakeName).collection("guests").document(emailToCompare).set(note2);

                        }
                    }
                }
                //checking if task contains any payload. if no, then update
                if (task.getResult().size() == 0) {

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

    private class updateInviteesTask extends AsyncTask<Void, Void, Void> {
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

            fStore.collection("stakes").document(stakeName).collection("guests")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {

                                list.clear();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    list.add(document.getId());
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
    public void checkStatus(){
        fStore.collection("stakes").document(stakeName).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");

                            String status = document.getString("status");

                            if(status.equals("Finished")){
                                finished = true;
                                tglStatus.setVisibility(View.GONE);
                                tvFinished.setVisibility(View.VISIBLE);
                                btnUnfinish.setVisibility(View.VISIBLE);
                            }
                            else{
                                tglStatus.setVisibility(View.VISIBLE);
                                tvFinished.setVisibility(View.GONE);
                                btnUnfinish.setVisibility(View.GONE);
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
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            suggestions = new ArrayAdapter<String>(EditStakeActivity.this,
                    android.R.layout.simple_spinner_dropdown_item, alUsers.toArray(new String[alUsers.size()]));
            System.out.println("thy " +Arrays.toString(alUsers.toArray(new String[alUsers.size()])));

            System.out.println("this is " +  alUsers.toArray(new String[alUsers.size()]));
            spnInvitees.setAdapter(suggestions);
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
        /*
        if(n==0){
            if(index<var.length){
                System.out.println("here we go");
                getUserName(var[index]);
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
        */
        for (int i = 0; i < var.length; i++)
        {
            System.out.println("here we go");
            getUserName(var[i]);
        }



    }
    private void getUserName(final String documentname){
        fStore.collection("user_account").document(documentname).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()) {
                        System.out.println("doc exists");
                        try{
                            String newUserName = document.getString("email");
                            System.out.println(newUserName);
                            alUsers.add(newUserName);
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
    private class MyAsyncTask3 extends AsyncTask<Void, Void, Void> {
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

            setIntent(stakeName, status, invitees, reward);

        }
    }



}
