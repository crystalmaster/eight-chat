package com.eightchat;


import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity {
    private Toolbar chatToolBar;
    private String senderUid, receiverUid , nameRe, imageRe;
    private Button btnSendMess;
    private EditText edtMess;
    private TextView tvReceiverName, tvStatus;
    private CircleImageView civReceiverImage;
    private DatabaseReference roofRef, userDatabaseRef;
    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        messageAdapter = new MessageAdapter(messageList);
        userMessageList = (RecyclerView) findViewById(R.id.rcvListMess);
        linearLayoutManager = new LinearLayoutManager(this );
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);


        roofRef = FirebaseDatabase.getInstance().getReference();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnSendMess = (Button) findViewById(R.id.btnSendMess);
        edtMess = (EditText) findViewById(R.id.edtMessage);
        civReceiverImage = (CircleImageView) findViewById(R.id.civReceiverImage);
        tvReceiverName = (TextView) findViewById(R.id.tvDisplayNameUserChats);
        tvStatus = (TextView) findViewById(R.id.tvStatusUserChats);
        chatToolBar = (Toolbar) findViewById(R.id.chatsAppBar);

        setSupportActionBar(chatToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle("");

        senderUid = getIntent().getStringExtra("userSenderID");
        receiverUid = getIntent().getStringExtra("userReceiverID");
        nameRe = getIntent().getStringExtra("nameRe");
        imageRe = getIntent().getStringExtra("imageRe");


        tvReceiverName.setText(nameRe);

        Picasso.get().load(imageRe).placeholder(R.drawable.default_avatar).into(civReceiverImage);

        userDatabaseRef.child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                if(dataSnapshot.hasChild("user_state"))
                {


                    String state = dataSnapshot.child("user_state").child("state").getValue().toString();

                    if(state.equals("online"))
                    {
                        tvStatus.setText("Active");
                    }
                    else {
                        tvStatus.setText(" ");
                    }
                }
                else {
                    tvStatus.setText(" ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMess();

            }
        });
    }

    private void sendMess() {

        String messageText = edtMess.getText().toString();
        btnSendMess.setEnabled(false);
        if(!TextUtils.isEmpty(messageText))
        {
            String messageSenderRef = "Message/" + senderUid +"/"+receiverUid;
            String messageReceiverRef = "Message/" + receiverUid+"/"+senderUid;

            DatabaseReference userMessageKeyRef = roofRef.child("Message").child(senderUid).child(receiverUid).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "Text");
            messageTextBody.put("from", senderUid);

            Map messageBodyDetails = new HashMap();

            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            roofRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    edtMess.setText("");
                    btnSendMess.setEnabled(true);
                }
            });
        }
        else Toast.makeText(ChatsActivity.this, "Trống", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onStart() {
        super.onStart();

        roofRef.child("Message").child(senderUid).child(receiverUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chats_menu, menu);
        return  true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.itemDeleteConversation)
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            roofRef.child("Message").child(senderUid).child(receiverUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    roofRef.child("Message").child(receiverUid).child(senderUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            finish();
                                        }
                                    });

                                }
                            });
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this );
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }
        return  true;
    }

}
