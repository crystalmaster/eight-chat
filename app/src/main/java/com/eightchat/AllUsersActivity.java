package com.eightchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersActivity extends AppCompatActivity {
    private Toolbar aToolBar;
    private RecyclerView aUsersList;
    private DatabaseReference aUserDatabase;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        //ToolBar
        aToolBar = (Toolbar) findViewById(R.id.userAppBar);
        setSupportActionBar(aToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //database
        aUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        aUserDatabase.keepSynced(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        //RecyclerView
        aUsersList = (RecyclerView) findViewById(R.id.userList);

        //aUsersList.setHasFixedSize(true);
        aUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(aUserDatabase, Users.class).build();

        FirebaseRecyclerAdapter <Users,UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                    final String currentID = currentUser.getUid();
                    final String userID = getRef(position).getKey();
                    if(currentID.equals(userID)) {
                            holder.userName.setText(model.getName() + " ( ME )");
                            holder.userView.setEnabled(false);
                    }
                    else
                        holder.userName.setText(model.getName());
                    holder.userStatus.setText(model.getStatus());
                    Picasso.get().load(model.getImage()).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(holder.profileImage);
                    holder.userView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profileIntent = new Intent(AllUsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",userID);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_single, viewGroup, false);
                UserViewHolder viewHolder = new UserViewHolder(view);

                return viewHolder;
            }
        };
       aUsersList.setAdapter(firebaseRecyclerAdapter);

       firebaseRecyclerAdapter.startListening();

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View userView;
        TextView userName;
        TextView userStatus;
        CircleImageView profileImage;
        Button btnSendMess,btnOnlineStatus;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

           userName = itemView.findViewById(R.id.userSingleName);
           userStatus = itemView.findViewById(R.id.userSingleStatus);
           profileImage = itemView.findViewById(R.id.userSingleImage);
           btnSendMess = itemView.findViewById(R.id.btnSendMessage);
           btnOnlineStatus = itemView.findViewById(R.id.btnOnlineStatus);
           btnSendMess.setVisibility(View.INVISIBLE);
           btnOnlineStatus.setVisibility(View.INVISIBLE);
           userView = itemView;
        }
    }
}