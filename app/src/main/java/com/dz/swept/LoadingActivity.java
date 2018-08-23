package com.dz.swept;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SnapshotMetadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadingActivity extends AppCompatActivity {

    FirebaseDatabase fDatabase;
    FirebaseUser user;
    DatabaseReference fRef;
    FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    DocumentReference fDoc;

    private static final int RC_SIGN_IN = 123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_loading);


            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());


            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.actualswept)
                            .setTheme(R.style.AppTheme)
                            .build(),
                    RC_SIGN_IN);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == RC_SIGN_IN) {
                IdpResponse response = IdpResponse.fromResultIntent(data);

                if (resultCode == RESULT_OK) {
                    // Successfully signed in


                    user = FirebaseAuth.getInstance().getCurrentUser();
                    fDatabase = FirebaseDatabase.getInstance();
                    fDoc = fStore.collection("user_account").document(user.getUid());

                    final FirebaseUserMetadata metadata = user.getMetadata();


                    fDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()) {
                                    System.out.println("doc exists");
                                    finish();
                                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                                }

                                else {
                                    System.out.println("doc no exists");
                                    Map<String, Object> note = new HashMap<>();
                                    note.put("username",user.getDisplayName());
                                    note.put("email",user.getEmail());
                                    note.put("user_id",user.getUid());

                                    try{
                                        note.put("profile_img_url",user.getPhotoUrl().toString());

                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    fStore.collection("user_account").document(user.getUid()).set(note);
                                    finish();
                                    startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                                    //The user doesn't exist...
                                }

                            }
                        }


                });



                        // The user is new, show them a fancy intro screen!



                        // This is an existing user, show them a welcome back screen.






                    // ...
                }
                else {
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());


                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setLogo(R.drawable.actualswept)
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                    // ...
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

    }

}
