package com.example.adi.helloworld;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView mChatsList;

    //RecyclerViewAdapterContacts adapter;
    //List<Friends> list = new ArrayList<>();

    DatabaseReference contactsReference, usersReference;
    FirebaseAuth mAuth;
    String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatsList = (RecyclerView) privateChatView.findViewById(R.id.chats_list);
        mChatsList.setLayoutManager(new LinearLayoutManager(getContext()));



        return privateChatView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //adapter = new RecyclerViewAdapterContacts(getActivity(), list);

        //displayContacts();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(contactsReference, Friends.class)
                .build();

        FirebaseRecyclerAdapter<Friends, ViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Friends model) {

                final String userID = getRef(position).getKey();

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

                            final String userName = dataSnapshot.child("Username").getValue().toString();
                            final String userID = dataSnapshot.child("UserID").getValue().toString();

                            String status = "Unknown";
                            String state  = "Unknown";

                            if(dataSnapshot.hasChild("State"))
                            {
                                status = "Last Seen: " + dataSnapshot.child("State").child("Date").getValue().toString() + ", " + dataSnapshot.child("State").child("Time").getValue().toString();
                                state  = "Current State: " + dataSnapshot.child("State").child("Type").getValue().toString();
                            }

                            holder.setUsername(userName);
                            holder.setProfileImage(profileImage);
                            holder.setStatus(status);
                            holder.setState(state);


                            final String finalProfileImage = profileImage;
                            final String finalStatus = status;
                            final String onlineTest = dataSnapshot.child("State").child("Type").getValue().toString();;

                            if(onlineTest.equals("Online"))
                            {
                                holder.onlineView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                holder.onlineView.setVisibility(View.INVISIBLE);
                            }


                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    sendUserToChatActivity(userID, userName, finalProfileImage, finalStatus);
                                }
                            });

                            holder.image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    String visitID = userID;
                                    Intent imageIntent = new Intent(getActivity(), ImageActivity.class);
                                    imageIntent.putExtra("VisitID", visitID);
                                    startActivity(imageIntent);
                                }
                            });

                        }
                    }



                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_private_layout, viewGroup, false);

                ViewHolder viewHolder = new ViewHolder(view);

                return viewHolder;

            }
        };

        mChatsList.setAdapter(adapter);
        adapter.startListening();

    }

    class ViewHolder extends RecyclerView.ViewHolder {


        View mView;
        public TextView username, userStatus, userState;
        public ImageView onlineView;
        public CircleImageView image;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            username = (TextView) mView.findViewById(R.id.private_user_profile_name);
            userStatus = (TextView) mView.findViewById(R.id.private_user_profile_last_seen);
            userState = (TextView) mView.findViewById(R.id.private_user_online_state);
            image = (CircleImageView) mView.findViewById(R.id.private_user_profile_image);
            onlineView = (ImageView) mView.findViewById(R.id.private_user_online_status);

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

        public void setState(String State) {
            userState.setText(State);
        }

    }

    private void sendUserToChatActivity(String userID, String username, String image, String seen)
    {
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra("VisitID", userID);
        chatIntent.putExtra("Username", username);
        chatIntent.putExtra("Image", image);
        chatIntent.putExtra("LastSeen", seen);
        startActivity(chatIntent);
    }

    //Personal Recycler Adapter (Refresh Problem)
    /*private void displayContacts()
    {

        contactsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        final String contactsUserID = dataSnapshot1.getKey();

                        usersReference.child(contactsUserID).addValueEventListener(new ValueEventListener() {
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
                                    String userID = dataSnapshot.child("UserID").getValue().toString();

                                    String status = "Unknown";
                                    String state  = "Unknown";

                                    if(dataSnapshot.hasChild("State"))
                                    {
                                        status = "Last Seen: " + dataSnapshot.child("State").child("Date").getValue().toString() + ", " + dataSnapshot.child("State").child("Time").getValue().toString();
                                        state  = "Current State: " + dataSnapshot.child("State").child("Type").getValue().toString();
                                    }


                                    Friends contact = new Friends(userName, status, profileImage, state, userID);

                                    if(!list.contains(contact)) list.add(contact);

                                    if (!list.isEmpty()) {
                                        Collections.sort(list, new Comparator<Friends>() {
                                            @Override
                                            public int compare(Friends c1, Friends c2) {
                                                //You should ensure that list doesn't contain null values!
                                                return c1.getUsername().compareTo(c2.getUsername());
                                            }
                                        });
                                    }

                                }
                                adapter.notifyDataSetChanged();
                                mChatsList.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class RecyclerViewAdapterContacts extends RecyclerView.Adapter<RecyclerViewAdapterContacts.ViewHolder>{


        Context context;
        List<Friends> Array;

        public RecyclerViewAdapterContacts(Context context, List<Friends> TempList) {
            this.Array = TempList;
            this.context = context;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_private_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            final Friends model = Array.get(position);

            holder.setUsername(model.getUsername());
            holder.setProfileImage(model.getProfileImage());
            holder.setStatus(model.getStatus());
            holder.setState(model.getState());

            Log.d("ClassFriends", model.getState());

            if((model.getState()).equals("Online"))
            {
                Log.d("ClassFriends", model.getState());
                holder.onlineView.setVisibility(View.VISIBLE);
            }


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    sendUserToChatActivity(model.getUserID(), model.getUsername(), model.profileImage, model.getStatus());
                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent imageIntent = new Intent(getActivity(), ImageActivity.class);
                    imageIntent.putExtra("VisitID", visitID);
                    startActivity(imageIntent);
                }
            });


        }

        @Override
        public int getItemCount() {
            return Array.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {


            View mView;
            public TextView username, userStatus, userState;
            public ImageView onlineView;
            public CircleImageView image;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                username = (TextView) mView.findViewById(R.id.private_user_profile_name);
                userStatus = (TextView) mView.findViewById(R.id.private_user_profile_last_seen);
                userState = (TextView) mView.findViewById(R.id.private_user_online_state);
                image = (CircleImageView) mView.findViewById(R.id.private_user_profile_image);
                onlineView = (ImageView) mView.findViewById(R.id.private_user_online_status);

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

            public void setState(String State) {
                userState.setText(State);
            }

        }

    }*/

}
