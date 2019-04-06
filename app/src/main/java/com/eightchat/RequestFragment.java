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
public class RequestFragment extends Fragment {

    private View requestView;
    private RecyclerView requestListView;
    private DatabaseReference friendsRequestDatabaseRef, userDatabaseRef;
    private FirebaseUser currentUser;
    private TextView notification;
    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestView = inflater.inflate(R.layout.fragment_request, container, false);
        notification = (TextView) requestView.findViewById(R.id.notification);
        requestListView = (RecyclerView) requestView.findViewById(R.id.rcvRequestList);
        requestListView.setLayoutManager(new LinearLayoutManager(getContext()));

        notification.setText("Hiện tại không có lời mời kết bạn");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        String currentUid = currentUser.getUid();
        friendsRequestDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Friends_req").child(currentUid);

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return requestView;
    }

    public static class RequestFriendsViewHolder extends RecyclerView.ViewHolder {

        View userView;
        TextView userName;
        TextView userStatus;
        CircleImageView profileImage;
        Button sendMess, btnOnlineStatus;
        public RequestFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userSingleName);
            userStatus = itemView.findViewById(R.id.userSingleStatus);
            profileImage = itemView.findViewById(R.id.userSingleImage);
            sendMess = itemView.findViewById(R.id.btnSendMessage);
            btnOnlineStatus = itemView.findViewById(R.id.btnOnlineStatus);
            btnOnlineStatus.setVisibility(View.INVISIBLE);
            userView = itemView;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(friendsRequestDatabaseRef, Users.class).build();

        FirebaseRecyclerAdapter <Users,RequestFriendsViewHolder> usersRequestFriendsViewHolderFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, RequestFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestFriendsViewHolder holder, int position, @NonNull Users model) {
                final String currentUserUid = getRef(position).getKey();

                friendsRequestDatabaseRef.child(currentUserUid).child("request_type").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                            if(dataSnapshot.getValue().toString().equals("received") ) {
                                    notification.setVisibility(View.INVISIBLE);
                                    userDatabaseRef.child(currentUserUid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String image = dataSnapshot.child("image").getValue().toString();
                                    holder.userStatus.setVisibility(View.INVISIBLE);
                                    holder.sendMess.setVisibility(View.INVISIBLE);
                                    holder.userName.setText(name);
                                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(holder.profileImage);

                                    holder.userView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id",currentUserUid);
                                            startActivity(profileIntent);
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else {
                                requestListView.setVisibility(View.INVISIBLE);
                                if(requestListView.getItemDecorationCount() == 0)
                                {
                                    notification.setVisibility(View.VISIBLE);
                                    notification.setText("No friend request");
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }

            @NonNull
            @Override
            public RequestFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_single, viewGroup, false);
                RequestFriendsViewHolder requestView = new RequestFriendsViewHolder(view);
                return  requestView;
            }
        };
        requestListView.setAdapter(usersRequestFriendsViewHolderFirebaseRecyclerAdapter);
        usersRequestFriendsViewHolderFirebaseRecyclerAdapter.startListening();
        if(requestListView.getItemDecorationCount() == 0)
        {
            notification.setVisibility(View.VISIBLE);
            notification.setText("No friend request");
        }
    }
}
