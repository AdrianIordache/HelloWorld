package com.example.adi.helloworld;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupsView;
    private ListView listGroupView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String>  groupsList = new ArrayList<>();

    private DatabaseReference groupsReference;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public GroupsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        groupsView = inflater.inflate(R.layout.fragment_groups, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        groupsReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();

        displayGroups();

        listGroupView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
               String currentGroupName = adapterView.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("GroupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return groupsView;
    }

    private void displayGroups()
    {
        groupsReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Set<String> groups = new HashSet<>();

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    String currentGroupName = dataSnapshot1.getKey();
                    boolean isMember = false;
                    //Log.d("GroupClass", currentGroupName + " + currentName");

                    for(int i = 0; i < dataSnapshot1.child("Users").getChildrenCount(); ++i)
                    {
                        String groupMember = dataSnapshot1.child("Users").child("UserID").getValue().toString();

                        if(currentUserID.equals(groupMember)) isMember = true;
                    }

                    if(isMember) groups.add(currentGroupName);

                }

                groupsList.clear();
                groupsList.addAll(groups);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void InitializeFields()
    {
        listGroupView = (ListView) groupsView.findViewById(R.id.groups_list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_color_text, R.id.list_content, groupsList);

        listGroupView.setAdapter(arrayAdapter);


    }

}
