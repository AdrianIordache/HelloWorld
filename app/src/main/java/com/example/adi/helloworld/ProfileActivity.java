package com.example.adi.helloworld;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, currentState, senderUserID;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendUserRequestButton, declineRequestButton;

    private DatabaseReference usersReference, chatRequestReference, contactsReference, notificationReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("VisitID").toString();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestReference = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");

        InitializeFields();

        retrieveUserInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserState("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserState("Offline");
    }

    private void retrieveUserInfo()
    {
        usersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("Profile Image"))
                    {
                        String userImage = dataSnapshot.child("Profile Image").getValue().toString();
                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }

                    String userName = dataSnapshot.child("Username").getValue().toString();
                    String userStatus = dataSnapshot.child("Status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest()
    {
        chatRequestReference.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(receiverUserID))
                {
                    String request_type = dataSnapshot.child(receiverUserID).child("Request Type").getValue().toString();

                    if(request_type.equals("Sent"))
                    {
                        currentState = "Request Sent";
                        sendUserRequestButton.setText("Cancel Request");
                    }
                    else if(request_type.equals("Received"))
                    {
                        currentState = "Request Received";
                        sendUserRequestButton.setText("Accept Request");

                        declineRequestButton.setVisibility(View.VISIBLE);
                        declineRequestButton.setEnabled(true);

                        declineRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                cancelChatRequest();
                            }
                        });
                    }
                }
                else
                {
                    contactsReference.child(senderUserID)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.hasChild(receiverUserID))
                                    {
                                        currentState = "Friends";
                                        sendUserRequestButton.setText("Remove Contact");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendUserRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserRequestButton.setEnabled(false);

                if(currentState.equals("New"))
                {
                    sendChatRequest();
                }
                if(currentState.equals("Request Sent"))
                {
                    cancelChatRequest();
                }
                if(currentState.equals("Request Received"))
                {
                    acceptChatRequest();
                }
                if(currentState.equals("Friends"))
                {
                    removeContact();
                }
            }
        });
    }

    private void removeContact()
    {
        contactsReference.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            contactsReference.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                sendUserRequestButton.setEnabled(true);
                                                currentState = "New";
                                                sendUserRequestButton.setText("Send Request");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest()
    {
        /*final HashMap<String, Object> senderDetails = new HashMap<>();
        final HashMap<String, Object> receiverDetails = new HashMap<>();


        usersReference.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String image = dataSnapshot.child("Profile Image").getValue().toString();

                    senderDetails.put("Username", username);
                    senderDetails.put("Profile Image", image);
                    senderDetails.put("Contacts", "Saved");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String image = dataSnapshot.child("Profile Image").getValue().toString();

                    receiverDetails.put("Username", username);
                    receiverDetails.put("Profile Image", image);
                    receiverDetails.put("Contacts", "Saved");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

        contactsReference.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    contactsReference.child(receiverUserID).child(senderUserID)
                            .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                chatRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            chatRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        sendUserRequestButton.setEnabled(true);
                                                        currentState = "Friends";
                                                        sendUserRequestButton.setText("Remove Contact");

                                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                                        declineRequestButton.setEnabled(false);
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
            }
        });
    }

    private void cancelChatRequest()
    {
        chatRequestReference.child(senderUserID).child(receiverUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            chatRequestReference.child(receiverUserID).child(senderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                sendUserRequestButton.setEnabled(true);
                                                currentState = "New";
                                                sendUserRequestButton.setText("Send Request");

                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

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

        usersReference.child(senderUserID).child("State").updateChildren(currentStateMap);


    }

    private void sendChatRequest()
    {



        chatRequestReference.child(senderUserID).child(receiverUserID).child("Request Type").setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatRequestReference.child(receiverUserID).child(senderUserID).child("Request Type").setValue("Received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {

                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                chatNotificationMap.put("From", senderUserID);
                                chatNotificationMap.put("Type", "Request");

                                sendUserRequestButton.setEnabled(true);
                                currentState = "Request Sent";
                                sendUserRequestButton.setText("Cancel Request");

                                /*notificationReference.child(receiverUserID).push().setValue(chatNotificationMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {

                                            }
                                        });*/


                            }
                        }
                    });
                }
            }
        });
    }


    private void InitializeFields()
    {
        userProfileImage = (CircleImageView)findViewById(R.id.profile_user_image);
        userProfileName = (TextView) findViewById(R.id.profile_user_name);
        userProfileStatus = (TextView) findViewById(R.id.profile_user_status);
        sendUserRequestButton = (Button) findViewById(R.id.profile_send_request);
        declineRequestButton = (Button) findViewById(R.id.profile_decline_request);

        currentState = "New";
    }
}
