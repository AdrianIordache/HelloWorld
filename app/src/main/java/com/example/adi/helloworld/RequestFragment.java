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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView mRequestList;

    private DatabaseReference chatRequestReference, usersReference, contactsReference;
    private FirebaseAuth mAuth;

    RecyclerView.Adapter adapter;
    List<Contacts> list = new ArrayList<>();

    private String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");


        mRequestList = (RecyclerView) requestFragmentView.findViewById(R.id.request_list);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //displayAllRequest();

        Query query = chatRequestReference.child(currentUserID);

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull Contacts model) {

                final String userID = getRef(position).getKey();

                DatabaseReference getTypeReference = getRef(position).child("Request Type").getRef();

                getTypeReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String requestType = dataSnapshot.getValue().toString();

                            if(requestType.equals("Received"))
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
                                            holder.setStatus("Wants to connect with you...");

                                            holder.acceptRequestButton.setVisibility(View.VISIBLE);
                                            holder.acceptRequestButton.setEnabled(true);

                                            holder.declineRequestButton.setVisibility(View.VISIBLE);
                                            holder.declineRequestButton.setEnabled(true);


                                            holder.acceptRequestButton.setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    contactsReference.child(currentUserID).child(userID).child("Contacts")
                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if(task.isSuccessful())
                                                            {
                                                                contactsReference.child(userID).child(currentUserID).child("Contacts")
                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                    {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            chatRequestReference.child(currentUserID).child(userID).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task)
                                                                                        {
                                                                                            if(task.isSuccessful())
                                                                                            {
                                                                                                chatRequestReference.child(userID).child(currentUserID).removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                                                            {
                                                                                                                Toast.makeText(getActivity(), "Contact Saved...", Toast.LENGTH_SHORT).show();

                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });


                                                }
                                            });

                                            holder.declineRequestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    chatRequestReference.child(currentUserID).child(userID).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task)
                                                                {
                                                                    if(task.isSuccessful())
                                                                    {
                                                                        chatRequestReference.child(userID).child(currentUserID).removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task)
                                                                                    {
                                                                                        Toast.makeText(getActivity(), "Request Declined...", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            });

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

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_layout, viewGroup, false);

                ViewHolder viewHolder = new ViewHolder(view);

                return viewHolder;
            }
        };

        mRequestList.setAdapter(adapter);
        adapter.startListening();

    }

    class ViewHolder extends RecyclerView.ViewHolder {


        View mView;
        public TextView username, userStatus;
        public CircleImageView image;
        public Button acceptRequestButton, declineRequestButton;


        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            username = (TextView) mView.findViewById(R.id.user_profile_name);
            userStatus = (TextView) mView.findViewById(R.id.user_profile_status);
            image = (CircleImageView) mView.findViewById(R.id.user_profile_image);
            acceptRequestButton = (Button) mView.findViewById(R.id.request_accept_button);
            declineRequestButton = (Button) mView.findViewById(R.id.request_decline_button);

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


    //Personal Recycler View Adapter
    /*private void displayAllRequest()
    {

        chatRequestReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                //Log.d("NewClass", "!!!!!");
                if(dataSnapshot.exists())
                {

                    list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String requestType = dataSnapshot1.child("Request Type").getValue().toString();
                        //Log.d("NewClass", requestType);

                        if(requestType.equals("Received"))
                        {
                            String userID = dataSnapshot1.getKey();
                            //Log.d("NewClass", userID);

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
                                        String userID = dataSnapshot.child("UserID").getValue().toString();

                                        Contacts contact = new Contacts(userName, status, profileImage, userID);

                                        if (!list.contains(contact)) list.add(contact);

                                        adapter = new RecyclerViewAdapterContacts(getActivity(), list);
                                        adapter.notifyDataSetChanged();
                                        mRequestList.setAdapter(adapter);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
            holder.setStatus("Wants to connect with you...");

            holder.acceptRequestButton.setVisibility(View.VISIBLE);
            holder.acceptRequestButton.setEnabled(true);

            holder.declineRequestButton.setVisibility(View.VISIBLE);
            holder.declineRequestButton.setEnabled(true);


            holder.acceptRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    contactsReference.child(currentUserID).child(model.getUserID()).child("Contacts")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                contactsReference.child(model.getUserID()).child(currentUserID).child("Contacts")
                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            chatRequestReference.child(currentUserID).child(model.getUserID()).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task)
                                                        {
                                                            if(task.isSuccessful())
                                                            {
                                                                chatRequestReference.child(model.getUserID()).child(currentUserID).removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task)
                                                                            {
                                                                                Toast.makeText(getActivity(), "Contact Saved...", Toast.LENGTH_SHORT).show();
                                                                                displayAllRequest();

                                                                            }
                                                                        });
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                            }
                        }
                    });


                }
            });

            holder.declineRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    chatRequestReference.child(currentUserID).child(model.getUserID()).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        chatRequestReference.child(model.getUserID()).child(currentUserID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        Toast.makeText(getActivity(), "Request Declined...", Toast.LENGTH_SHORT).show();
                                                        displayAllRequest();
                                                    }
                                                });
                                    }
                                }
                            });
                }
            });

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


        /*class ViewHolder extends RecyclerView.ViewHolder {


            View mView;
            public TextView username, userStatus;
            public CircleImageView image;
            public Button acceptRequestButton, declineRequestButton;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                username = (TextView) mView.findViewById(R.id.user_profile_name);
                userStatus = (TextView) mView.findViewById(R.id.user_profile_status);
                image = (CircleImageView) mView.findViewById(R.id.user_profile_image);
                acceptRequestButton = (Button) mView.findViewById(R.id.request_accept_button);
                declineRequestButton = (Button) mView.findViewById(R.id.request_decline_button);

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
