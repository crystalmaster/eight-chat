package com.eightchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {
    private Button btnSignUp;
    private TextInputEditText tiDisplayName;
    private TextInputEditText tiEmail;
    private TextInputEditText tiPassword;
    private TextInputEditText tiConfirmPassword;

    private FirebaseAuth mAuth;

    private Toolbar resToolBar;

    private ProgressDialog resProgessDialog;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgister);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        tiDisplayName = (TextInputEditText) findViewById(R.id.tiDisplayName);
        tiEmail = (TextInputEditText) findViewById(R.id.tiEmailRes);
        tiPassword = (TextInputEditText) findViewById(R.id.tiPasswordRes);
        tiConfirmPassword = (TextInputEditText) findViewById(R.id.tiConfirmPassword);

        mAuth = FirebaseAuth.getInstance();

        resToolBar = (Toolbar) findViewById(R.id.registerToolBar);
        setSupportActionBar(resToolBar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resProgessDialog = new ProgressDialog(this );


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName = tiDisplayName.getText().toString();
                String email = tiEmail.getText().toString();
                String password = tiPassword.getText().toString();
                String confirmPassword = tiConfirmPassword.getText().toString();

                if(TextUtils.isEmpty(displayName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(password.equals(confirmPassword)) {
                        resProgessDialog.setTitle("Registering User");
                        resProgessDialog.setMessage("Please wait while we create your account !");
                        resProgessDialog.setCanceledOnTouchOutside(false);
                        resProgessDialog.show();
                        resgister_user(displayName,email,password);
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Xác nhận mật khẩu không khớp",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void resgister_user(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                    String uid = current_user.getUid();

                    database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name", displayName);
                    userMap.put("status", "Hi");
                    userMap.put("image","default");

                    database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                resProgessDialog.dismiss();
                                Intent resToMainIntent =  new Intent(RegisterActivity.this, MainActivity.class);
                                resToMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(resToMainIntent);
                                finish();
                            }
                            else
                                Toast.makeText(RegisterActivity.this, "Email đã được sử dụng", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }
}
