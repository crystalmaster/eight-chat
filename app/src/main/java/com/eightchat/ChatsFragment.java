package com.eightchat;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private  View itemView;
    private RecyclerView chatBoxList;
    private DatabaseReference messagesDatabaseRef;
    private DatabaseReference usersDatabaseRef;
    private FirebaseUser currentUser;
    private String currentUid;
    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        itemView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatBoxList = (RecyclerView) itemView.findViewById(R.id.rcvChatBoxList);
        chatBoxList.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = currentUser.getUid();
        messagesDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Message").child(currentUid);
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return itemView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(messagesDatabaseRef, Users.class).build();

        FirebaseRecyclerAdapter <Users, ChatBoxViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ChatBoxViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatBoxViewHolder holder, int position, @NonNull final Users model) {
                final String receiverUid = getRef(position).getKey();
                messagesDatabaseRef.child(receiverUid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            usersDatabaseRef.child(receiverUid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    final String nameRe = dataSnapshot.child("name").getValue().toString();
                                    final String imageRe = dataSnapshot.child("image").getValue().toString();
                                    holder.userName.setText(nameRe);
                                    if(dataSnapshot.hasChild("user_state"))
                                    {
                                        String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                                        String time = dataSnapshot.child("user_state").child("time").getValue().toString();
                                        String state = dataSnapshot.child("user_state").child("state").getValue().toString();

                                        if(state.equals("online"))
                                        {
                                            holder.userStatus.setText("Online");
                                            holder.btnOnline.setVisibility(View.VISIBLE);
                                        }
                                        else {
                                            holder.userStatus.setText("Last Seen:" + date+" "+ time);
                                            holder.btnOnline.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    else {
                                        holder.btnOnline.setVisibility(View.INVISIBLE);
                                        holder.userStatus.setText("Offline");
                                    }

                                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.default_avatar).into(holder.profileImage);
                                    holder.sendMess.setVisibility(View.INVISIBLE);

                                    holder.userView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getActivity(), ChatsActivity.class);
                                            chatIntent.putExtra("userReceiverID", receiverUid);
                                            chatIntent.putExtra("userSenderID",currentUid);
                                            chatIntent.putExtra("nameRe", nameRe);
                                            chatIntent.putExtra("imageRe",imageRe);
                                            startActivity(chatIntent);
                                        }
                                    });
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

            @NonNull
            @Override
            public ChatBoxViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_single, viewGroup, false);
                ChatBoxViewHolder chatBoxView = new ChatBoxViewHolder(view);
                return chatBoxView;
            }
        };

        chatBoxList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ChatBoxViewHolder extends RecyclerView.ViewHolder {

        View userView;
        TextView userName;
        TextView userStatus;
        CircleImageView profileImage;
        Button sendMess, btnOnline;

        public ChatBoxViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userSingleName);
            userStatus = itemView.findViewById(R.id.userSingleStatus);
            profileImage = itemView.findViewById(R.id.userSingleImage);
            sendMess = itemView.findViewById(R.id.btnSendMessage);
            btnOnline = (Button) itemView.findViewById(R.id.btnOnlineStatus);
            userView = itemView;
        }
    }

}
