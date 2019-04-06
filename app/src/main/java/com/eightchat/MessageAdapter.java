package com.eightchat;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessageList;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    public MessageAdapter (List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_mess_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String senderUid = mAuth.getCurrentUser().getUid();

        Messages message = userMessageList.get(i);

        String fromUserID = message.getFrom();
        String typeMess = message.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("image").getValue().toString();

                Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(messageViewHolder.receiverImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(typeMess.equals("Text")) {
            messageViewHolder.receiverMessage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessage.setVisibility(View.INVISIBLE);
            ///
            if(fromUserID.equals(senderUid)) {
                messageViewHolder.senderMessage.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessage.setBackgroundResource(R.drawable.sender_mess_layout);
                messageViewHolder.senderMessage.setTextColor(Color.WHITE);
                messageViewHolder.senderMessage.setText(message.getMessage());


            }
            else {
                messageViewHolder.senderMessage.setVisibility(View.INVISIBLE);

                messageViewHolder.receiverMessage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessage.setBackgroundResource(R.drawable.sender_mess_layout);
                messageViewHolder.receiverMessage.setTextColor(Color.WHITE);
                messageViewHolder.receiverMessage.setText(message.getMessage());
            }

        }


    }

    public class  MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, receiverMessage;
        public CircleImageView receiverImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessage = (TextView) itemView.findViewById(R.id.tvSenderMess);
            receiverMessage = (TextView) itemView.findViewById(R.id.tvReceiverMess);
            receiverImage = (CircleImageView) itemView.findViewById(R.id.civReceiverImageMess);
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

}


