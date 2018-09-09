package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.GroupToConnect;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class GroupSelection extends AppCompatActivity {

    private FirebaseRecyclerAdapter<GroupToConnect, GroupsHolder> firebaseAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView groupsRVW;

    private Query availableGroups;
    private String firebaseUserID;

    private final static String TAG = "GroupSelection";

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if(dataSnapshot!=null) {
                Log.d(TAG, dataSnapshot.toString());

                String message;
                long childrenCount=dataSnapshot.getChildrenCount();

                if(childrenCount>0){
                    // esiste almeno un gruppo a cui l'utente è abilitato
                    message=String.format(getString(R.string.GROUP_SELECTION_EXPLANATION).replace("!!!username!!!", firebaseUserID),dataSnapshot.getChildrenCount());

                } else {
                    // l'utente non è abilitato a nessun gruppo
                    message=getString(R.string.GROUP_SELECTION_EXPLANATION_NOGROUP).replace("!!!username!!!", firebaseUserID);
                }

                TextView explanationTXV = (TextView) findViewById(R.id.TXV___GROUPSSELECTION___EXPLANATION);
                explanationTXV.setText(message);
            }

            // aggiorna l'adapter per visualizzare gli eventuali gruppi disponibili
            refreshAdapter();


        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    public static class GroupsHolder extends RecyclerView.ViewHolder {

        public TextView groupNameTXV;
        public TextView groupDescriptionTXV;
        public ImageButton connectBTN;

        public GroupsHolder(View itemView) {
            super(itemView);

            groupNameTXV = (TextView) itemView.findViewById(R.id.TXV___ROWGROUP___GROUPNAME);
            groupDescriptionTXV = (TextView) itemView.findViewById(R.id.TXV___ROWGROUP___GROUPDESCRIPTION);
            connectBTN = (ImageButton) itemView.findViewById(R.id.BTN___ROWGROUP___CONNECT);

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        groupsRVW = (RecyclerView) findViewById(R.id.RWV___GROUPSSELECTION___GROUPS);

        firebaseUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, String.format("Firebase user ID: %s", firebaseUserID));

    }

    @Override
    protected void onResume() {

        super.onResume();

        availableGroups = FirebaseDatabase.getInstance().getReference("Groups").orderByChild(String.format("validUser_%s",firebaseUserID)).equalTo(true);
        availableGroups.addValueEventListener(valueEventListener);

    }

    @Override
    protected void onPause() {

        super.onPause();
        availableGroups.removeEventListener(valueEventListener);
    }


    private void refreshAdapter() {

        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<GroupToConnect, GroupsHolder>(
                GroupToConnect.class,
                R.layout.row_holder_group_element,
                GroupsHolder.class,
                availableGroups) {

            @Override
            protected void populateViewHolder(GroupsHolder holder, final GroupToConnect group, int position) {

                holder.groupNameTXV.setText(group.getGroupName());
                holder.groupDescriptionTXV.setText(group.getGroupDescription());

                holder.connectBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        connectToGroup(group.getGroupName());

                    }
                });

            }

        };

        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int mediaCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (mediaCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    groupsRVW.scrollToPosition(positionStart);
                }

            }

        });

        groupsRVW.setLayoutManager(linearLayoutManager);
        groupsRVW.setAdapter(firebaseAdapter);

    }

    private void connectToGroup(String groupName){

        // imposta il nome del gruppo [R.string.data_group_name] nelle shared preferences
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.data_group_name), groupName);
        editor.commit();

        // lancia l'Activity MainActivity
        Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

        // termina l'Activity corrente
        finish();
        return;

    }

}
