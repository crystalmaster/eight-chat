package com.eightchat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private Toolbar pToolBar;
    private CircleImageView profileImage;
    private TextView tvProfileName,tvProfileStatus,tvProfileFriendCount;
    private Button btnSendRequest, btnDeclineRequest;

    private DatabaseReference databaseReferenceRef;
    private DatabaseReference friendRequestDatabaseRef;
    private DatabaseReference friendDatabaseRef;
    private DatabaseReference currentUserDatabaseRef;
    private FirebaseUser currentUser;
    private String currentState;
    private String name, status, image;
    private String currentName, currentStatus, currentImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user_id");

        databaseReferenceRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        friendRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends_req");
        friendDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        currentUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentName = dataSnapshot.child("name").getValue().toString();
                currentStatus = dataSnapshot.child("status").getValue().toString();
                currentImage = dataSnapshot.child("image").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        pToolBar = (Toolbar) findViewById(R.id.profileAppBar);
        setSupportActionBar(pToolBar);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvProfileName = (TextView) findViewById(R.id.tvProfileName);
        tvProfileStatus = (TextView) findViewById(R.id.tvProFileStatus);
        tvProfileFriendCount = (TextView) findViewById(R.id.tvProfileFriendCount);

        btnSendRequest = (Button) findViewById(R.id.btnSendRequest);
        btnDeclineRequest = (Button) findViewById(R.id.btnDeclineRequest);

        profileImage = (CircleImageView) findViewById(R.id.imProfileImage);

        currentState ="not_friends";
        //
        btnDeclineRequest.setVisibility(View.INVISIBLE);
        btnDeclineRequest.setEnabled(false);

        databaseReferenceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();

                tvProfileName.setText(name);
                tvProfileStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profileImage);

                //Friend list//request feature

                friendRequestDatabaseRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(userId))
                        {
                            String reqType = dataSnapshot.child(userId).child("request_type").getValue().toString();

                            if(reqType.equals("received"))
                            {
                                currentState="req_received";
                                btnSendRequest.setText("ACCEPT FRIEND REQUEST");

                                btnDeclineRequest.setVisibility(View.VISIBLE);
                                btnDeclineRequest.setEnabled(true);
                            }
                            else
                                if (reqType.equals("sent"))
                                {
                                    currentState="req_sent";
                                    btnSendRequest.setText("CANCEL FRIEND REQUEST");
                                }
                        } else {
                            friendDatabaseRef.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(userId)) {
                                        currentState = "friends";
                                        btnSendRequest.setText("UNFRIEND");

                                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                                        btnDeclineRequest.setEnabled(false);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentState ="not_friends";
                btnDeclineRequest.setEnabled(false);
                btnDeclineRequest.setVisibility(View.INVISIBLE);

                friendRequestDatabaseRef.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        friendRequestDatabaseRef.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                btnSendRequest.setEnabled(true);
                                currentState="not_friends";
                                btnSendRequest.setText("ADD FRIEND");
                                Toast.makeText(ProfileActivity.this, "Deline Successfully",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                btnSendRequest.setText("ADD FRIEND");
            }
        });
        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnSendRequest.setEnabled(false);
                if(currentState.equals("friends")) {
                    friendDatabaseRef.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabaseRef.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    currentState = "not_friend";
                                    btnSendRequest.setEnabled(true);
                                    btnSendRequest.setText("ADD FRIEND");
                                    Toast.makeText(ProfileActivity.this, "Unfriend Successfully",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
                //Not_friend--------
                if(currentState.equals("not_friends")){
                    friendRequestDatabaseRef.child(currentUser.getUid()).child(userId).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                friendRequestDatabaseRef.child(userId).child(currentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        currentState="req_sent";
                                        btnSendRequest.setText("CANCEL FRIEND REQUEST");

                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(ProfileActivity.this, "Failed Sending Request",Toast.LENGTH_LONG).show();
                            }
                            btnSendRequest.setEnabled(true);
                        }
                    });

                }
                //-----------------CANCE:------
                if(currentState.equals("req_sent")) {
                    friendRequestDatabaseRef.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendRequestDatabaseRef.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    btnSendRequest.setEnabled(true);
                                    currentState="not_friends";
                                    btnSendRequest.setText("ADD FRIEND");
                                    Toast.makeText(ProfileActivity.this, "Cancel Request Successfully",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                }
                if(currentState.equals("req_received"))    {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    HashMap<String,String> friendsMap = new HashMap<>();
                    friendsMap.put("name", name);
                    friendsMap.put("status", status);
                    friendsMap.put("image",image);
                    friendsMap.put("date",currentDate);

                    final HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("name",currentName);
                    userMap.put("status",currentStatus);
                    userMap.put("image",currentImage);
                    userMap.put("date",currentDate);

                    friendDatabaseRef.child(currentUser.getUid()).child(userId).setValue(friendsMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendDatabaseRef.child(userId).child(currentUser.getUid()).setValue(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    friendRequestDatabaseRef.child(currentUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            friendRequestDatabaseRef.child(userId).child(currentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    btnSendRequest.setEnabled(true);
                                                    currentState="friends";
                                                    btnSendRequest.setText("UNFRIEND");

                                                    btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                    btnDeclineRequest.setEnabled(false);
                                                    Toast.makeText(ProfileActivity.this, "You are friend now",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    });

                                }
                            });

                        }
                    });
                }
            }
        });

    }
}
