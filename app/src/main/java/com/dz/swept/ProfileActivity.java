package com.dz.swept;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.dz.swept.models.User;
import com.firebase.client.Firebase;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_IMAGE = 101;
    final String TAG = "ProfileActivity";

    ImageView imgEditUser, imgEditProf, imgHome, imgCancelEditUser;
    CircleImageView imgUserProf;
    TextView tvViewUser, tvSignOut, tvUserNotAvailable;
    EditText etChangeUser;
    Button btnSaveProfile;
    Uri uriProf;
    ProgressBar pbChangeProf;
    String profPicurl,displayName;
    Boolean UserChanged, ProfChanged, ProfSaved, DuplicateUsername;

    FirebaseAuth fAuth;
    FirebaseAuth.AuthStateListener fAuthListener;
    FirebaseDatabase fDatabase;
    FirebaseUser user;
    DatabaseReference fRef;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_profile);

        pbChangeProf = findViewById(R.id.pbChangeProf);
        imgHome= findViewById(R.id.imgHome);
        imgUserProf = findViewById(R.id.imgUserProf);
        imgEditUser = findViewById(R.id.imgEditUser);
        imgCancelEditUser = findViewById(R.id.imgCancelEditUser);
        imgEditProf = findViewById(R.id.imgEditProf);
        tvViewUser = findViewById(R.id. tvViewUser);
        tvUserNotAvailable = findViewById(R.id. tvUserNotAvailable);
        tvSignOut = findViewById(R.id. tvSignOut);
        etChangeUser = findViewById(R.id.etChangeUser);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        tvSignOut.setPaintFlags(tvSignOut.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        fRef = fDatabase.getReference();
        user = fAuth.getCurrentUser();

        tvSignOut.setOnClickListener(this);
        imgCancelEditUser.setOnClickListener(this);
        imgEditUser.setOnClickListener(this);
        imgEditProf.setOnClickListener(this);
        btnSaveProfile.setOnClickListener(this);
        imgHome.setOnClickListener(this);

        progress = new ProgressDialog(this);

        loadUserInformation();

        etChangeUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                etChangeUser.setTextColor(Color.BLACK);
                etChangeUser.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.black), PorterDuff.Mode.SRC_ATOP);
                tvUserNotAvailable.setVisibility(View.GONE);
                DuplicateUsername = false;
            }

            @Override
            public void afterTextChanged(Editable editable) {

                checkIfusernameExist(etChangeUser.getText().toString());
                Log.d(TAG,"testing");
                UserChanged = true;

            }

        });



        fAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    finish();
                    Intent home = new Intent(ProfileActivity.this, LoadingActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(home);
                }
            }
        };

    }
    @Override
    public void onClick (View view){


        if(view==imgEditUser){
            tvViewUser.setVisibility(View.GONE);
            etChangeUser.setVisibility(View.VISIBLE);
            imgEditUser.setVisibility(View.GONE);
            imgCancelEditUser.setVisibility(View.VISIBLE);
        }
        if(view==imgCancelEditUser){
            tvViewUser.setVisibility(View.VISIBLE);
            etChangeUser.setVisibility(View.GONE);
            imgEditUser.setVisibility(View.VISIBLE);
            imgCancelEditUser.setVisibility(View.GONE);
        }
        if(view==imgEditProf){
            showChangeProf();
        }
        if(view==btnSaveProfile){
            if(DuplicateUsername){
                Toast.makeText(ProfileActivity.this, "Username already exists. Please create a different one.", Toast.LENGTH_SHORT).show();
                etChangeUser.setTextColor(Color.RED);
                etChangeUser.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                tvUserNotAvailable.setVisibility(View.VISIBLE);
            }
            else{
                saveUserInfo();
                updateFireStore();
            }

        }
        if(view==imgHome){
            if(!ProfSaved) {
                if (!TextUtils.isEmpty(etChangeUser.getText().toString()) || ProfChanged) {
                    createDialog();
                }
                else {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                }
            }
            else {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            }


        }
        if(view==tvSignOut){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
            finish();
            startActivity(new Intent(ProfileActivity.this, LoadingActivity.class));

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CHOOSE_IMAGE && resultCode==RESULT_OK && data != null && data.getData() != null){
            uriProf = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProf);
                imgUserProf.setImageBitmap(bitmap);

                uploadImageToFire();


            }
           catch(IOException e){
                e.printStackTrace();
           }
        }
    }

    private void showChangeProf(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select profile picture"), CHOOSE_IMAGE);

    }
    private void uploadImageToFire(){


        final StorageReference profRef = FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");


        if(uriProf != null){

            pbChangeProf.setVisibility(View.VISIBLE);
            profRef.putFile(uriProf).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    pbChangeProf.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return profRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        profPicurl = downloadUri.toString();
                        ProfChanged = true;


                    } else { Toast.makeText(ProfileActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        // Handle failures
                        // ...
                    }
                }
            });

        }

    }
    private void saveUserInfo(){

        displayName = etChangeUser.getText().toString().trim();

        if(!DuplicateUsername){
            if(user!=null && UserChanged && ProfChanged) {
                progress.setTitle("Saving Profile");
                progress.setMessage("Please wait...");
                progress.show();

                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(displayName).setPhotoUri(Uri.parse(profPicurl)).build();

                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ProfSaved = true;
                            progress.dismiss();
                            tvViewUser.setText(displayName);
                            tvViewUser.setVisibility(View.VISIBLE);
                            etChangeUser.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            if(user!=null && UserChanged) {
                progress.setTitle("Saving Profile");
                progress.setMessage("Please wait...");
                progress.show();

                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();

                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ProfSaved = true;
                            progress.dismiss();
                            tvViewUser.setText(displayName);
                            tvViewUser.setVisibility(View.VISIBLE);
                            etChangeUser.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            if(user!=null && ProfChanged) {
                progress.setTitle("Saving Profile");
                progress.setMessage("Please wait...");
                progress.show();

                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(profPicurl)).build();

                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progress.dismiss();
                        ProfSaved = true;
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            if(user!=null && !ProfChanged && !UserChanged) {
                Toast.makeText(ProfileActivity.this, "Bro, there's nothing to save. Just click home man if you can't think of a name or a profile picture.", Toast.LENGTH_LONG).show();
            }
        }



    }
    private void loadUserInformation() {

        reset();

       FirebaseUser user = fAuth.getCurrentUser();

        if (user != null) {
            if (user.getPhotoUrl() != null) {
                    Uri photoUrl = user.getPhotoUrl();
                    Glide.with(getApplicationContext())
                            .load(photoUrl.toString())
                            .into(imgUserProf);


            }

            if (user.getDisplayName() != null) {
                tvViewUser.setText(user.getDisplayName());
            }

        }

    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(etChangeUser.getText().toString()) || ProfChanged) {
            createDialog();
        }
        else{
            ProfileActivity.super.onBackPressed();
        }


    }
    public void createDialog() {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("You have unsaved changes. Are you sure you want to exit?");
            alert.setCancelable(false);

            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }

            });
            alert.create().show();

    }
    private void reset(){
        DuplicateUsername = false;
        UserChanged = false;
        ProfChanged = false;
        ProfSaved = false;
    }
    private void checkIfusernameExist(final String usernameToCompare) {

        if(UserChanged){
            //----------------------------------------------------------------
            final Query mQuery = fStore.collection("user_account").whereEqualTo("username", usernameToCompare);
            mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.d(TAG, "checking if" + usernameToCompare + "already exists.");

                    if (task.isSuccessful()) {
                        for (DocumentSnapshot ds : task.getResult()) {
                            String userNames = ds.getString("username");
                            if (userNames.equals(usernameToCompare)) {
                                Log.d(TAG, usernameToCompare + "already exists.");
                                DuplicateUsername = true;


                            }
                        }
                    }
                    //checking if task contains any payload. if no, then update
                    if (task.getResult().size() == 0) {
                        try {

                            Log.d(TAG, "onComplete: MATCH NOT FOUND - username is available");
                            //Updating new username............


                        } catch (NullPointerException e) {
                            Log.e(TAG, "NullPointerException: " + e.getMessage());
                        }
                    }
                }
            });
        }


    }
    private void updateFireStore(){



        Map<String, Object> note = new HashMap<>();

        if(!DuplicateUsername){


            if(UserChanged&&ProfChanged){
                try{
                    note.put("username",etChangeUser.getText().toString());
                    note.put("profile_img_url",user.getPhotoUrl().toString());

                    fStore.collection("user_account").document(user.getUid()).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Save name+pic failed!", e.toString());
                                }
                            });
                }
                catch(Exception e){
                    e.printStackTrace();
                }


            }
            if(UserChanged){

                note.put("username",etChangeUser.getText().toString());


                fStore.collection("user_account").document(user.getUid()).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Save username success!",etChangeUser.getText().toString());


                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Save username failed!", e.toString());
                            }
                        });

            }
            if(ProfChanged){
                try{
                    note.put("profile_img_url",user.getPhotoUrl().toString());

                    fStore.collection("user_account").document(user.getUid()).update(note).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Save profurl success!", user.getPhotoUrl().toString());
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Save profileurl failed!", e.toString());
                                }
                            });
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }

            reset();
        }

    }
}


