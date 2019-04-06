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
public class FriendsFragment extends Fragment {

    private  View friendsView;
    private RecyclerView friendsList;
    private DatabaseReference friendsDatabaseRef, userDataBaseRef;
    private FirebaseUser currentUser;
    private String currentUserUid;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        friendsView = inflater.inflate(R.layout.fragment_friends, container, false);
        friendsList = (RecyclerView) friendsView.findViewById(R.id.rcvFriendsList);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        //

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserUid = currentUser.getUid();
        friendsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserUid);
        userDataBaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return friendsView;
    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(friendsDatabaseRef, Users.class).build();

        FirebaseRecyclerAdapter <Users,FriendsViewHolder> friendsViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Users model) {
                final String userID = getRef(position).getKey();
                holder.userName.setText(model.getName());
                holder.userStatus.setVisibility(View.INVISIBLE);
                Picasso.get().load(model.getImage()).placeholder(R.drawable.default_avatar).into(holder.profileImage);
                userDataBaseRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("user_state"))
                        {
                            String state = dataSnapshot.child("user_state").child("state").getValue().toString();

                            if(state.equals("online"))
                            {
                                holder.btnOnline.setVisibility(View.VISIBLE);
                            }
                            else {
                                holder.btnOnline.setVisibility(View.INVISIBLE);
                            }
                        }
                        else {
                            holder.btnOnline.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                holder.btnStartChatBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       Intent chatIntent = new Intent(getActivity(), ChatsActivity.class);
                        chatIntent.putExtra("userReceiverID", userID);
                        chatIntent.putExtra("userSenderID",currentUserUid);
                        chatIntent.putExtra("nameRe", model.getName());
                        chatIntent.putExtra("imageRe",model.getImage());
                        startActivity(chatIntent);

                    }
                });
                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                        profileIntent.putExtra("user_id",userID);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_single, viewGroup, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);

                return viewHolder;
            }
        };
        friendsList.setAdapter(friendsViewHolderFirebaseRecyclerAdapter);
        friendsViewHolderFirebaseRecyclerAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View userView;
        TextView userName;
        TextView userStatus;
        CircleImageView profileImage;
        Button btnStartChatBox, btnOnline;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userSingleName);
            userStatus = itemView.findViewById(R.id.userSingleStatus);
            profileImage = itemView.findViewById(R.id.userSingleImage);
            btnStartChatBox = itemView.findViewById(R.id.btnSendMessage);
            btnOnline = itemView.findViewById(R.id.btnOnlineStatus);
            userView = itemView;
        }
    }
}
