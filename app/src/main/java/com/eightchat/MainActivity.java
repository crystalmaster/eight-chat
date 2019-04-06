package com.eightchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    //private Toolbar mToolBar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabs;
    private DatabaseReference userDatabaseRef;
    private TextView tvdislayUserName;
    private CircleImageView civProfileImage;
    private String image, name;
    private Button btnSearchUser;
    private String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        tvdislayUserName = (TextView) findViewById(R.id.tvDisplayNameMain) ;
        civProfileImage = (CircleImageView) findViewById(R.id.civProfileMain);
        btnSearchUser = (Button) findViewById(R.id.btnSearch);
//        mToolBar = (Toolbar) findViewById(R.id.main_page_toolbar);
//        setSupportActionBar(mToolBar);
//        getSupportActionBar().setTitle("");

        userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        if(currentUser != null) {
            uid = currentUser.getUid();

            userDatabaseRef.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = dataSnapshot.child("name").getValue().toString();
                    image= dataSnapshot.child("image").getValue().toString();

                    tvdislayUserName.setText(name);

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_avatar).into(civProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(civProfileImage);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.mainTabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabs = (TabLayout) findViewById(R.id.mainTabs);
        mTabs.setupWithViewPager(mViewPager);
        setupTabIcon();
        sendToProfileEvent(civProfileImage);
        sendToAllUserEvent(btnSearchUser);
        mViewPager.setCurrentItem(1);
    }

    private void sendToAllUserEvent(Button btnSearchUser) {
        btnSearchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainToAllIntent = new Intent(MainActivity.this, AllUsersActivity.class);
                startActivity(mainToAllIntent);
            }
        });
    }

    private void sendToProfileEvent(CircleImageView civProfileImage) {
        civProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainToSettingIntent = new Intent(MainActivity.this, SettingAccountActivity.class);
                startActivity(mainToSettingIntent);
            }
        });
    }

    private void setupTabIcon() {
        mTabs.getTabAt(0).setIcon(R.drawable.discover_icon);
        mTabs.getTabAt(1).setIcon(R.drawable.message_icon);
        mTabs.getTabAt(2).setIcon(R.drawable.friends_icon);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null)
        {
            sendToStart();
            finish();
        }
        else {
            updateUserStatus("online");
        }
    }

    private void sendToStart() {
        Intent  mainIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(mainIntent);
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

        userDatabaseRef.child(uid).child("user_state").updateChildren(onlineStateMap);
    }
//Only use with app bar
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//
//        return  true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//        if(item.getItemId() == R.id.mainLogOutBtn) {
//            FirebaseAuth.getInstance().signOut();
//            mLogOutDialog.setTitle("Logging Out!");
//            mLogOutDialog.setCanceledOnTouchOutside(false);
//            mLogOutDialog.show();
//            sendToStart();
//        }
//        if(item.getItemId() == R.id.mainAccountSettingBtn) {
//            Intent mainToSettingIntent = new Intent(MainActivity.this, SettingAccountActivity.class);
//            startActivity(mainToSettingIntent);
//        }
//        if(item.getItemId() == R.id.mainAllUserBtn) {
//            Intent mainToAllIntent = new Intent(MainActivity.this, AllUsersActivity.class);
//            startActivity(mainToAllIntent);
//        }
//        return  true;
//    }
}
