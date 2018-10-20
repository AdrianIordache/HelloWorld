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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView mContactsList;

    RecyclerView.Adapter adapter;
    List<Contacts> list = new ArrayList<>();

    DatabaseReference contactsReference, usersReference;
    FirebaseAuth mAuth;
    String currentUserID;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mContactsList = (RecyclerView) contactsView.findViewById(R.id.contacts_list);
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return contactsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        //displayContacts();

        Query query = contactsReference.child(currentUserID);

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsReference, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Contacts model)
            {
                final String userID = getRef(position).getKey();

                usersReference.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
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

                            holder.username.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view)
                                {
                                    String visitID = userID;
                                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                                    profileIntent.putExtra("VisitID", visitID);
                                    startActivity(profileIntent);
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
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_contacts_layout, viewGroup, false);

                ViewHolder viewHolder = new ViewHolder(view);

                return viewHolder;

            }
        };

        mContactsList.setAdapter(adapter);
        adapter.startListening();

    }

    class ViewHolder extends RecyclerView.ViewHolder {


        View mView;
        public TextView username, userStatus;
        public CircleImageView image;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            username = (TextView) mView.findViewById(R.id.user_contacts_profile_name);
            userStatus = (TextView) mView.findViewById(R.id.user_contacts_profile_status);
            image = (CircleImageView) mView.findViewById(R.id.user_contacts_profile_image);

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

    // Personal Recycler Adapter
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
                                    String status = dataSnapshot.child("Status").getValue().toString();
                                    String userID = dataSnapshot.child("UserID").getValue().toString();

                                    Contacts contact = new Contacts(userName, status, profileImage, userID);

                                    if(!list.contains(contact)) list.add(contact);

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

                                adapter = new RecyclerViewAdapterContacts(getActivity(), list);
                                adapter.notifyDataSetChanged();
                                mContactsList.setAdapter(adapter);

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
        List<Contacts> Array;

        public RecyclerViewAdapterContacts(Context context, List<Contacts> TempList) {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_contacts_layout, parent, false);

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

            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                    profileIntent.putExtra("VisitID", visitID);
                    startActivity(profileIntent);
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
            public TextView username, userStatus;
            public CircleImageView image;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                username = (TextView) mView.findViewById(R.id.user_contacts_profile_name);
                userStatus = (TextView) mView.findViewById(R.id.user_contacts_profile_status);
                image = (CircleImageView) mView.findViewById(R.id.user_contacts_profile_image);

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

    }*/
}

