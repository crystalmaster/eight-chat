package com.eightchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    private Button sBtnRegister;
    private Button sBtnSignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sBtnRegister = (Button) findViewById(R.id.btnRegister);
        sBtnSignIn = (Button) findViewById(R.id.btnSignIn);

        sBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startToResIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(startToResIntent);
                //finish();
            }
        });

        sBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startToSignInIntent = new Intent(StartActivity.this, SignInActivity.class);
                startActivity(startToSignInIntent);
                //finish();
            }
        });
    }
}
