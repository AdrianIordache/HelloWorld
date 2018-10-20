package com.example.adi.helloworld;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userStatus, userName;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private DatabaseReference rootReference;
    private StorageReference userProfileImageReference;
    private String currentUserID;
    private ProgressDialog loadingBar;

    private static final int galleryPick = 1;
    String profileImageLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Image");

        InitializeFields();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                updateSettings();
            }
        });

        retrieveUsersDetails();

        userProfileImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUserState("Online");
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

        usersReference.child("State").updateChildren(currentStateMap);


    }

    private void retrieveUsersDetails()
    {
        usersReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String username = "Unknown";
                String status   = "Hello, I am online!";
                String image    = "Unknown";

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("Username"))
                    {
                        username = dataSnapshot.child("Username").getValue().toString();
                        userName.setText(username);
                    }

                    if(dataSnapshot.hasChild("Profile Image"))
                    {
                        image = dataSnapshot.child("Profile Image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile_image).into(userProfileImage);
                    }

                    if(dataSnapshot.hasChild("Status"))
                    {
                        status = dataSnapshot.child("Status").getValue().toString();
                        userStatus.setText(status);
                    }
                }

                /*if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Username")) && (dataSnapshot.hasChild("Profile Image")))
                {
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String status = dataSnapshot.child("Status").getValue().toString();
                    String image = dataSnapshot.child("Profile Image").getValue().toString();

                    userName.setText(username);
                    userStatus.setText(status);
                    Picasso.get().load(image).into(userProfileImage);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Username")))
                {
                    String username = dataSnapshot.child("Username").getValue().toString();
                    String status = dataSnapshot.child("Status").getValue().toString();

                    userName.setText(username);
                    userStatus.setText(status);
                }
                else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("Profile Image")))
                {
                    String image = dataSnapshot.child("Profile Image").getValue().toString();
                    Picasso.get().load(image).into(userProfileImage);
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Please update your details...", Toast.LENGTH_SHORT).show();
                }*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        //Storing The Image to FirebaseStorage
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {

                loadingBar.setTitle("Updating Account Details");
                loadingBar.setMessage("Please wait...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageReference.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile Image Stored Successfully...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            profileImageLink = downloadUrl;

                            usersReference.child("Profile Image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {

                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully to Database...", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }

                                }
                            });

                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        }
    }

    private void updateSettings()
    {
        String username = userName.getText().toString();
        String status = userStatus.getText().toString();

        if(TextUtils.isEmpty(status))
        {
            status = "Hello World, I am online!";
        }

        if(TextUtils.isEmpty(username))
        {
            Toast.makeText(this, "Please Write your username...", Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingBar.setTitle("Updating Account Details");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            HashMap<String, Object> detailsMap = new HashMap<>();

            detailsMap.put("UserID", currentUserID);
            detailsMap.put("Username", username);
            detailsMap.put("Status", status);


            usersReference.updateChildren(detailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        sendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Details Updated...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void InitializeFields()
    {
        updateAccountSettings = (Button) findViewById(R.id.settings_update_button);
        userName = (EditText) findViewById(R.id.settings_user_name);
        userStatus = (EditText) findViewById(R.id.settings_user_status);
        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);
    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
