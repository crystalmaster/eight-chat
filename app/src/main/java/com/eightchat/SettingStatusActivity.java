package com.eightchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SettingStatusActivity extends AppCompatActivity {

    private Toolbar statusToolBar;
    private DatabaseReference ifoDatabase;
    private FirebaseUser sCurrentUser;
    private Button btnSave;
    private TextInputEditText displayName,status;
    private ProgressDialog sProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_status);

        sCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid =  sCurrentUser.getUid();

        ifoDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);


        statusToolBar = (Toolbar) findViewById(R.id.statusAppBar);
        setSupportActionBar(statusToolBar);
        getSupportActionBar().setTitle("Setting status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String statusValue = getIntent().getStringExtra("statusValue");
        String displayNameValue = getIntent().getStringExtra("displayName");
        displayName = (TextInputEditText) findViewById(R.id.newDisplayName);
        status = (TextInputEditText) findViewById(R.id.newStatus);
        btnSave = (Button) findViewById(R.id.btnSaveChange);
        status.setText(statusValue);
        displayName.setText(displayNameValue);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sProgressDialog = new ProgressDialog(SettingStatusActivity.this);
                sProgressDialog.setTitle("Saving changes");
                sProgressDialog.show();
                String sStatus = status.getText().toString();
                String sDisplayName = displayName.getText().toString();
                ifoDatabase.child("name").setValue(sDisplayName);
                ifoDatabase.child("status").setValue(sStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sProgressDialog.dismiss();
                            //Toast.makeText(SettingStatusActivity.this,"Successfully", Toast.LENGTH_LONG).show();
                            finish();
                        }//
                        else
                            Toast.makeText(SettingStatusActivity.this,"Lỗi", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }
}
