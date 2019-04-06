package com.eightchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private TextInputEditText sEmail;
    private TextInputEditText sPassword;

    private FirebaseAuth mAuth;

    private Button btnSignIn;
    private ProgressDialog sProgressDialog;
    private Toolbar sToolBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        sEmail = (TextInputEditText) findViewById(R.id.tiEmailSi);
        sPassword = (TextInputEditText) findViewById(R.id.tiPasswordSi);
        btnSignIn = (Button) findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        sProgressDialog = new ProgressDialog(this);

        //
        sToolBar = (Toolbar) findViewById(R.id.signInToolBar);
        setSupportActionBar(sToolBar);
        getSupportActionBar().setTitle("Sign In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = sEmail.getText().toString();
                String pasword = sPassword.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pasword))
                    Toast.makeText(SignInActivity.this,"Vui lòng điền đầy đủ thông tin",Toast.LENGTH_SHORT).show();
                else {
                    sProgressDialog.setTitle("Logging In");
                    sProgressDialog.setMessage("Please wait");
                    sProgressDialog.setCanceledOnTouchOutside(false);
                    sProgressDialog.show();

                    loginUser(email,pasword);
                }
            }
        });
    }

    private void loginUser(String email, String pasword) {
        mAuth.signInWithEmailAndPassword(email,pasword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    sProgressDialog.dismiss();

                    Intent signIntoMainIntent = new Intent(SignInActivity.this, MainActivity.class);
                    signIntoMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(signIntoMainIntent);
                    finish();
                }
                else {
                    sProgressDialog.hide();
                    Toast.makeText(SignInActivity.this, "Có lỗi xảy ra, vui lòng kiểm tra lại", Toast.LENGTH_SHORT ).show();
                }

            }
        });
    }
}
