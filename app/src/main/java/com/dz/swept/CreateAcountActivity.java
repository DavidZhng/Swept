package com.dz.swept;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;


public class CreateAcountActivity extends AppCompatActivity implements View.OnClickListener{

    Boolean UserChanged;
    EditText etUserCreate;
    Button btnConfirm;
    String username;
    ProgressDialog progressDialog;
    FirebaseAuth fAuth;
    private static final int RC_SIGN_IN = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_create_acount);

        fAuth = FirebaseAuth.getInstance();

        etUserCreate = findViewById(R.id.etUserCreate);
        btnConfirm = findViewById(R.id.btnConfirm);

        progressDialog = new ProgressDialog(this);

        btnConfirm.setOnClickListener(this);

    }

    private void ConfirmUser(){


        username = etUserCreate.getText().toString().trim();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter a valid username",Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Creating...");
        progressDialog.show();

        finish();
        startActivity(new Intent(CreateAcountActivity.this, MainActivity.class));
    }

    @Override
    public void onClick (View view){

        if(view==btnConfirm){
            ConfirmUser();
        }

    }
    private void loadUserInformation() {
        final FirebaseUser user = fAuth.getCurrentUser();

        if (user != null) {

            if (user.getDisplayName() != null) {
                etUserCreate.setText(user.getDisplayName());
            }

        }

    }

}
