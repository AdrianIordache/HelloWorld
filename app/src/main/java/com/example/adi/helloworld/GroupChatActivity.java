package com.example.adi.helloworld;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference, groupsReference, groupMessageKeyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("GroupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        groupsReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName).child("Messages");

        InitializeFields();

        retrieveUserDetails();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                saveMessageToDatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        groupsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if(dataSnapshot.exists())
                {
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String data = (String) ((DataSnapshot) iterator.next()).getValue();
            String message = (String) ((DataSnapshot) iterator.next()).getValue();
            String time = (String) ((DataSnapshot) iterator.next()).getValue();
            String username = (String) ((DataSnapshot) iterator.next()).getValue();

            displayTextMessage.append(username + " :\n" + message + "\n" + time + "   " + data + "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void saveMessageToDatabase()
    {
        String inputMessage = userMessageInput.getText().toString();
        String messageKey = groupsReference.push().getKey();

        if(TextUtils.isEmpty(inputMessage))
        {
            Toast.makeText(this, "Please Write a message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(callForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupsReference.updateChildren(groupMessageKey);

            groupMessageKeyReference = groupsReference.child(messageKey);

            HashMap<String, Object> messageDetailsMap = new HashMap<>();
            messageDetailsMap.put("Username", currentUserName);
            messageDetailsMap.put("Time", currentTime);
            messageDetailsMap.put("Date", currentDate);
            messageDetailsMap.put("Message", inputMessage);

            groupMessageKeyReference.updateChildren(messageDetailsMap);




        }
    }

    private void retrieveUserDetails()
    {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("Username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void InitializeFields()
    {
        mToolBar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.group_chat_send_message_button);
        userMessageInput = (EditText) findViewById(R.id.group_chat_input);
        displayTextMessage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.group_chat_scroll_view);
    }
}
