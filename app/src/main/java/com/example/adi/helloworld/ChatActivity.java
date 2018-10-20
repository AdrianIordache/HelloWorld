package com.example.adi.helloworld;

import android.content.Context;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, userIntentName, userIntentImage, userIntentLastSeen;
    private String saveCurrentDate, saveCurrentTime;

    private TextView userName, userLastSeen;
    private CircleImageView userProfileImage;
    private ImageButton sendMessageButton;
    private EditText messageInputText;

    private RecyclerView  messageList;
    RecyclerView.Adapter adapter;
    List<Messages> list = new ArrayList<>();

    private Toolbar chatToolbar;

    FirebaseAuth mAuth;
    DatabaseReference usersReference, rootReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("VisitID").toString();

        userIntentImage = getIntent().getExtras().get("Image").toString();
        userIntentLastSeen = getIntent().getExtras().get("LastSeen").toString();
        userIntentName = getIntent().getExtras().get("Username").toString();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        rootReference = FirebaseDatabase.getInstance().getReference();


        InitializeFields();

        userName.setText(userIntentName);
        userLastSeen.setText(userIntentLastSeen);
        Picasso.get().load(userIntentImage).placeholder(R.drawable.profile_image).into(userProfileImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendMessageToDatabase();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserState("Online");
        displayAllMessages();
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

    private void displayAllMessages()
    {
        updateUserState("Online");
        rootReference.child("Messages").child(senderUserID).child(receiverUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            list.clear();
                            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                            {
                                final String from = dataSnapshot1.child("From").getValue().toString();
                                final String message = dataSnapshot1.child("Message").getValue().toString();
                                final String type = dataSnapshot1.child("Type").getValue().toString();
                                final String data = dataSnapshot1.child("Date").getValue().toString();
                                final String time = dataSnapshot1.child("Time").getValue().toString();

                                Messages newMessage = new Messages(message, from, type, data, time);
                                list.add(newMessage);
                            }

                            Collections.reverse(list);
                            adapter = new RecyclerViewAdapterMessages(ChatActivity.this, list);

                            adapter.notifyDataSetChanged();


                            messageList.setAdapter(adapter);


                            messageList.scrollToPosition(0);
                           // Log.d("ChatNew", " + " + adapter.getItemCount());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public class RecyclerViewAdapterMessages extends RecyclerView.Adapter<RecyclerViewAdapterMessages.ViewHolder>
    {

        Context context;
        List<Messages> Array;

        public RecyclerViewAdapterMessages(Context context, List<Messages> array)
        {
            this.context = context;
            this.Array = array;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
        {
            Messages model = Array.get(position);

            String fromUserID = model.getFrom();
            String messageType = model.getType();

            usersReference.child(fromUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        if(dataSnapshot.hasChild("Profile Image"))
                        {
                            String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                            holder.setProfileImage(profileImage);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(messageType.equals("Text"))
            {
                holder.receiverMessageText.setVisibility(View.INVISIBLE);

                holder.receiverProfileImage.setVisibility(View.INVISIBLE);
                holder.receiverMessageDate.setVisibility(View.INVISIBLE);

                holder.senderMessageText.setVisibility(View.INVISIBLE);
                holder.senderMessageDate.setVisibility(View.INVISIBLE);



                if(fromUserID.equals(senderUserID))
                {
                    holder.senderMessageText.setVisibility(View.VISIBLE);
                    holder.senderMessageDate.setVisibility(View.VISIBLE);

                    holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                    holder.senderMessageText.setText(model.getMessage());
                    holder.senderMessageDate.setText(model.getData() + " " + model.getTime());
                }
                else
                {

                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.receiverMessageText.setVisibility(View.VISIBLE);
                    holder.receiverMessageDate.setVisibility(View.VISIBLE);

                    holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                    holder.receiverMessageText.setText(model.getMessage());
                    holder.receiverMessageDate.setText(model.getData() + " " + model.getTime());
                }
            }

        }

        @Override
        public int getItemCount()
        {
            return Array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            public TextView senderMessageText, receiverMessageText, senderMessageDate, receiverMessageDate;
            public CircleImageView receiverProfileImage;

            public ViewHolder (@NonNull View itemView)
            {
                super(itemView);

                senderMessageText = (TextView) itemView.findViewById(R.id.custom_message_sender_text);
                receiverMessageText = (TextView) itemView.findViewById(R.id.custom_message_receiver_text);
                receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.custom_message_profile_image);

                senderMessageDate = (TextView) itemView.findViewById(R.id.custom_message_sender_date);
                receiverMessageDate = (TextView) itemView.findViewById(R.id.custom_message_receiver_date);
            }

            public void setProfileImage(String ProfileImage) {
                Picasso.get().load(ProfileImage).into(receiverProfileImage);
            }

        }
    }


    private void sendMessageToDatabase()
    {
        String messageText = messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please write a message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderReference = "Messages/" + senderUserID + "/" + receiverUserID;
            String messageReceiverReference = "Messages/" + receiverUserID + "/" + senderUserID;

            DatabaseReference userMessageKeyReference = rootReference.child("Messages").child(senderUserID).child(receiverUserID).push();

            String messagePushID = userMessageKeyReference.getKey();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd:MMMM:yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            Map<String, Object> messageTextBody = new HashMap<>();
            messageTextBody.put("Message", messageText);
            messageTextBody.put("Type", "Text");
            messageTextBody.put("Time", saveCurrentTime);
            messageTextBody.put("Date", saveCurrentDate);
            messageTextBody.put("From",  senderUserID);

            Map<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderReference + '/' + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverReference + '/'+ messagePushID, messageTextBody);

            rootReference.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful())
                    {
                        //Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(ChatActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
                    }

                    messageInputText.setText("");
                }
            });

        }
    }

    private void InitializeFields()
    {
        chatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName = (TextView) findViewById(R.id.custom_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custom_last_seen);

        sendMessageButton = (ImageButton) findViewById(R.id.chat_private_send_message_button);
        messageInputText = (EditText) findViewById(R.id.chat_private_message_input);

        messageList = (RecyclerView) findViewById(R.id.chat_private_messages_list);
        messageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(linearLayoutManager);

    }
}
