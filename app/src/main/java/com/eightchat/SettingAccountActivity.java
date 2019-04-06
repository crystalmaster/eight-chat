package com.eightchat;

import com.eightchat.MainActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingAccountActivity extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private CircleImageView displayImage;
    private TextView tvDisplayName;
    private TextView tvStatus;
    private Button btnLogOut;
    private Button btnchangeStatus;
    private ProgressDialog sUpLoadProgressDialog;
    private ProgressDialog mLogOutDialog;
    private static final int GALLERY_PICK = 1;
    private Toolbar sToolBar;


    //
    private StorageReference imageStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_account);
        mAuth = FirebaseAuth.getInstance();
        mLogOutDialog = new ProgressDialog(this);
        //ToolBar
        sToolBar = (Toolbar) findViewById(R.id.settingAcountAppbar);
        setSupportActionBar(sToolBar);
        getSupportActionBar().setTitle("Account Setting");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayImage = (CircleImageView) findViewById(R.id.avatar);
        tvDisplayName = (TextView) findViewById(R.id.tvDisplayName);
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        btnLogOut = (Button) findViewById(R.id.btnChangeAvatar);
        btnchangeStatus = (Button) findViewById(R.id.btnChangeStatus);

        imageStorage = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currenUuid = currentUser.getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currenUuid);
        userDatabase.keepSynced(true);
        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //get user information from firebase
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                //String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                tvDisplayName.setText(name);
                tvStatus.setText(status);

                //Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(displayImage);
                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(displayImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(displayImage);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnchangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String statusValue = tvStatus.getText().toString();
                String displayNameValue = tvDisplayName.getText().toString();
                Intent accountToStatusSettingIntent = new Intent(SettingAccountActivity.this, SettingStatusActivity.class);
                accountToStatusSettingIntent.putExtra("statusValue", statusValue);
                accountToStatusSettingIntent.putExtra("displayName", displayNameValue);
                startActivity(accountToStatusSettingIntent);
            }
        });
        displayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingAccountActivity.this);
            }
        });
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                mLogOutDialog.setTitle("Logging Out!");
                mLogOutDialog.setCanceledOnTouchOutside(false);
                mLogOutDialog.show();
                updateUserStatus("offline");
                finish();
            }
        });
    }
    private void updateUserStatus (String state) {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("DD MM yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state",state);

        userDatabase.child("user_state").updateChildren(onlineStateMap);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && requestCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .start(this);

            Toast.makeText(SettingAccountActivity.this, "Sucess",Toast.LENGTH_LONG).show();
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                sUpLoadProgressDialog = new ProgressDialog(this);
                sUpLoadProgressDialog.setTitle("Uploading Image ...");
                sUpLoadProgressDialog.setMessage("Please wait...");
                sUpLoadProgressDialog.setCanceledOnTouchOutside(false);
                sUpLoadProgressDialog.show();

                Uri resultUri = result.getUri();


                String currentUid = currentUser.getUid();


                final StorageReference filePath = imageStorage.child("profile_images").child(currentUid+".jpg");

//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if(task.isSuccessful()) {
//                            String downloadUrl = filePath.getDownloadUrl().toString();
//
//                            Toast.makeText(SettingAccountActivity.this,downloadUrl, Toast.LENGTH_LONG).show();
//                            sUpLoadProgressDialog.dismiss();
//                        }
//                        else {
//                            Toast.makeText(SettingAccountActivity.this,"Error", Toast.LENGTH_LONG).show();
//                            sUpLoadProgressDialog.dismiss();
//                        }
//
//                    }
//                });
                //Up load image and get download link stackOverflow
                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){

                            String downUrl = task.getResult().toString();

                            userDatabase.child("image").setValue(downUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        sUpLoadProgressDialog.dismiss();
                                        Toast.makeText(SettingAccountActivity.this,"Tải ảnh thành công",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}
