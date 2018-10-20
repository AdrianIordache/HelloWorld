package com.example.adi.helloworld;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private DatabaseReference usersReference;
    private FirebaseAuth mAuth;

    RecyclerView.Adapter adapter;
    List<Contacts> list = new ArrayList<>();
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        updateUserState("Online");

        retrieveUsers();
    }

    /*@Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersReference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userID = getRef(position).getKey();

                if(!currentUserID.equals(userID))
                {
                    usersReference.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.exists())
                            {
                                String profileImage = "Unknown";

                                if(dataSnapshot.hasChild("Profile Image"))
                                {
                                    profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                                }



                                String userName = dataSnapshot.child("Username").getValue().toString();
                                String status = dataSnapshot.child("Status").getValue().toString();
                                final String userID = dataSnapshot.child("UserID").getValue().toString();

                                holder.setUsername(userName);
                                holder.setProfileImage(profileImage);
                                holder.setStatus(status);

                                holder.image.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        String visitID = userID;
                                        Intent imageIntent = new Intent(SearchActivity.this, ImageActivity.class);
                                        imageIntent.putExtra("VisitID", visitID);
                                        startActivity(imageIntent);
                                    }
                                });

                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        String visitID = userID;
                                        Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                                        profileIntent.putExtra("VisitID", visitID);
                                        startActivity(profileIntent);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_layout, viewGroup, false);

                ViewHolder viewHolder = new ViewHolder(view);

                return viewHolder;
            }
        };

        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    class ViewHolder extends RecyclerView.ViewHolder {


        View mView;
        public TextView username, userStatus;
        public CircleImageView image;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            username = (TextView) mView.findViewById(R.id.user_profile_name);
            userStatus = (TextView) mView.findViewById(R.id.user_profile_status);
            image = (CircleImageView) mView.findViewById(R.id.user_profile_image);

        }

        public void setUsername(String FullName) {
            username.setText(FullName);
        }

        public void setProfileImage(String ProfileImage) {
            Picasso.get().load(ProfileImage).placeholder(R.drawable.profile_image).into(image);
        }

        public void setStatus(String Status) {
            userStatus.setText(Status);
        }

    }*/

    public void updateUserState(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("Time", saveCurrentTime);
        currentStateMap.put("Date", saveCurrentDate);
        currentStateMap.put("Type", state);

        usersReference.child(currentUserID).child("State").updateChildren(currentStateMap);


    }

    private void InitializeFields()
    {
        mRecyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.search_toolbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Friends");
    }

    //Personal Recycler Adapter
    private void retrieveUsers()
    {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String profileImage = "Unknown";

                        if(dataSnapshot1.hasChild("Profile Image"))
                        {
                            profileImage = dataSnapshot1.child("Profile Image").getValue().toString();
                        }

                       // Log.d("SearchClass", dataSnapshot1.child("Username").getValue().toString());

                        String userName = dataSnapshot1.child("Username").getValue().toString();
                        String status = dataSnapshot1.child("Status").getValue().toString();
                        String userID = dataSnapshot1.child("UserID").getValue().toString();

                        Contacts contact = new Contacts(userName, status, profileImage, userID);

                        if(!currentUserID.equals(userID))
                        {
                            list.add(contact);
                        }

                        if (!list.isEmpty()) {
                            Collections.sort(list, new Comparator<Contacts>() {
                                @Override
                                public int compare(Contacts c1, Contacts c2) {
                                    //You should ensure that list doesn't contain null values!
                                    return c1.getUsername().compareTo(c2.getUsername());
                                }
                            });
                        }

                    }

                    adapter = new RecyclerViewAdapterContacts(SearchActivity.this, list);
                    mRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class RecyclerViewAdapterContacts extends RecyclerView.Adapter<RecyclerViewAdapterContacts.ViewHolder>{


        Context context;
        List<Contacts> Array;

        public RecyclerViewAdapterContacts(Context context, List<Contacts> TempList) {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            final Contacts model = Array.get(position);

            holder.setUsername(model.getUsername());
            holder.setProfileImage(model.getProfileImage());
            holder.setStatus(model.getStatus());

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent imageIntent = new Intent(SearchActivity.this, ImageActivity.class);
                    imageIntent.putExtra("VisitID", visitID);
                    startActivity(imageIntent);
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent profileIntent = new Intent(SearchActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("VisitID", visitID);
                    startActivity(profileIntent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return Array.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {


            View mView;
            public TextView username, userStatus;
            public CircleImageView image;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                username = (TextView) mView.findViewById(R.id.user_profile_name);
                userStatus = (TextView) mView.findViewById(R.id.user_profile_status);
                image = (CircleImageView) mView.findViewById(R.id.user_profile_image);

            }

            public void setUsername(String FullName) {
                username.setText(FullName);
            }

            public void setProfileImage(String ProfileImage) {
                Picasso.get().load(ProfileImage).placeholder(R.drawable.profile_image).into(image);
            }

            public void setStatus(String Status) {
                userStatus.setText(Status);
            }

        }

    }

}
