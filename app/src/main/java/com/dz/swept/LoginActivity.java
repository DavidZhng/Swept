package com.dz.swept;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etUser, etPass;
    Button btnLogin, btnForgotPass, btnCreateAccount;
    ProgressBar pbLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);

        btnForgotPass = findViewById(R.id.btnForgotPass);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnLogin = findViewById(R.id.btnLogin);

        pbLogin = findViewById(R.id.pbLogin);

        btnLogin.setOnClickListener(this);
        btnCreateAccount.setOnClickListener(this);
    }

        @Override
        public void onClick (View view){

        if(view==btnLogin){
            btnLogin.setVisibility(View.INVISIBLE);
            pbLogin.setVisibility(View.VISIBLE);
        }
        if(view==btnCreateAccount){
            startActivity(new Intent(LoginActivity.this, CreateAcountActivity.class));
        }

    }
}

