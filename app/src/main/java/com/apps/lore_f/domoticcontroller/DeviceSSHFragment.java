package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

public class DeviceSSHFragment extends Fragment {

    public boolean viewCreated = false;
    private View fragmentView;

    private DatabaseReference sshOutputNode;

    public DeviceViewActivity parent;

    private ChildEventListener sshOutputChange = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            if (dataSnapshot != null) {

                Log.i(TAG, "onChildAdded :: datasnapshot lenght: " + dataSnapshot.getValue().toString().length());

                if (dataSnapshot.getKey().equals("Output"))
                    updateView(dataSnapshot);

            } else {

                Log.i(TAG, "onChildAdded :: cannot show datasnapshot :(");
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            if (dataSnapshot != null)
                if (dataSnapshot.getKey().equals("Output"))
                    updateView(dataSnapshot);

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
    };


    public DeviceSSHFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_ssh, container, false);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView = view;

        TextView sshOutput = (TextView) view.findViewById(R.id.TXV___DEVICESSH___SSH);
        sshOutput.setMovementMethod(new ScrollingMovementMethod());

        // inizializzo la referenza al nodo del databas
        sshOutputNode = FirebaseDatabase.getInstance().getReference("Users/lorenzofailla/Devices/" + parent.thisDevice + "/SSHShell/");
        sshOutputNode.addChildEventListener(sshOutputChange);

        // assegna un OnClickListener ai pulsanti
        ImageButton keyboardButton = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
        keyboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText sshCommandToSend = new EditText(getContext());

                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.ALERTDIALOG_MESSAGE_ENTER_SSH_COMMAND)
                        .setView(sshCommandToSend)
                        .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                parent.sendCommandToDevice(new Message("__ssh_input_command", sshCommandToSend.getText().toString()+"\n", parent.thisDevice));

                            }

                        })

                        .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        .setTitle(R.string.ALERTDIALOG_TITLE_ENTER_SSH_COMMAND)
                        .create()
                        .show();

            }
        });

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onDetach() {
        super.onDetach();

        // rimuove l'OnClickListener ai pulsanti

        // rimuove il ChildEventListener ai nodi del database
        sshOutputNode.removeEventListener(sshOutputChange);

    }

    public void updateView(@NonNull DataSnapshot dataSnapshot) {

        TextView sshOutput = (TextView) fragmentView.findViewById(R.id.TXV___DEVICESSH___SSH);
        sshOutput.setText(dataSnapshot.getValue().toString());

        final int scrollAmount = sshOutput.getLayout().getLineTop(sshOutput.getLineCount()) - sshOutput.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            sshOutput.scrollTo(0, scrollAmount);
        else
            sshOutput.scrollTo(0, 0);

        }

    private void refreshAdapter() {


    }

}
