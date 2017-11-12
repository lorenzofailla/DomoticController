package com.apps.lore_f.domoticcontroller;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.method.KeyListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class DeviceSSHFragment extends Fragment {

    public boolean viewCreated = false;
    private View fragmentView;

    private SSHView sshOutput;

    private DatabaseReference sshOutputNode;

    public DeviceViewActivity parent;

    private Handler handler;

    private long sshInputStreamCheckTimeout = 250L;

    private ByteArrayOutputStream sshInputStream = new ByteArrayOutputStream();
    private boolean sshInputStreamChanged = false;

    private Runnable manageSSHInputStream = new Runnable() {
        @Override
        public void run() {

            if (sshInputStreamChanged) {

                sendSSHInputStream();

            }

            handler.postDelayed(this, sshInputStreamCheckTimeout);

        }

    };

    private View.OnClickListener sshKeysListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String result=null;

            switch (v.getId()) {

                case R.id.BTN___DEVICESSH___KEYUP:
                    result="keyUp";
                    break;

                case R.id.BTN___DEVICESSH___KEYDOWN:
                    result="keyDown";
                    break;

                case R.id.BTN___DEVICESSH___KEYRIGHT:
                    result="keyRight";
                    break;

                case R.id.BTN___DEVICESSH___KEYLEFT:
                    result="keyLeft";
                    break;

            }

            if(result!=null){
                parent.sendCommandToDevice(new Message("__ssh_special",result,parent.thisDevice));
            }

        }
    };

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

        sshOutput = (SSHView) view.findViewById(R.id.TXV___DEVICESSH___SSH);
        sshOutput.setMovementMethod(new ScrollingMovementMethod());

        // inizializzo la referenza al nodo del databas
        sshOutputNode = FirebaseDatabase.getInstance().getReference("Users/lorenzofailla/Devices/" + parent.remoteDeviceName + "/SSHShells/"+parent.thisDevice);
        sshOutputNode.addChildEventListener(sshOutputChange);

        // assegna un OnClickListener ai pulsanti
        ImageButton sendCommandButton = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___SENDCOMMAND);
        sendCommandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText sshCommandToSend = new EditText(getContext());

                new AlertDialog.Builder(getContext())
                        .setMessage(R.string.ALERTDIALOG_MESSAGE_ENTER_SSH_COMMAND)
                        .setView(sshCommandToSend)
                        .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                parent.sendCommandToDevice(new Message("__ssh_input_command", sshCommandToSend.getText().toString() + "\n", parent.thisDevice));

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


        ImageButton showKeyBoardButton = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
        showKeyBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "Showing keyboard");
                sshOutput.showKeyboard();

            }

        });

        ImageButton sendKeyUp = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYUP);
        sendKeyUp.setOnClickListener(sshKeysListener);

        ImageButton sendKeyDown = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYDOWN);
        sendKeyDown.setOnClickListener(sshKeysListener);

        ImageButton sendKeyRight = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYRIGHT);
        sendKeyRight.setOnClickListener(sshKeysListener);

        ImageButton sendKeyLeft = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYLEFT);
        sendKeyLeft.setOnClickListener(sshKeysListener);

        // inizializza l'handler
        handler = new Handler();

        // esegue il callback "manageSSHInputStream"
        handler.postDelayed(manageSSHInputStream, 0L);

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
        ImageButton sendCommandButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
        sendCommandButton.setOnClickListener(null);

        ImageButton showKeyBoardButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
        showKeyBoardButton.setOnClickListener(null);

        ImageButton sendKeyUp = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYUP);
        sendKeyUp.setOnClickListener(null);

        ImageButton sendKeyDown = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYDOWN);
        sendKeyDown.setOnClickListener(null);

        ImageButton sendKeyRight = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYRIGHT);
        sendKeyRight.setOnClickListener(null);

        ImageButton sendKeyLeft = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYLEFT);
        sendKeyLeft.setOnClickListener(null);


        // rimuove il ChildEventListener ai nodi del database
        sshOutputNode.removeEventListener(sshOutputChange);

        // rimuove l'esecuzione del callback "manageSSHInputStream"
        handler.removeCallbacks(manageSSHInputStream);

        // comunica al dispositivo remoto di disconnettere la sessione ssh
        parent.sendCommandToDevice(new Message("__close_ssh", "null", parent.thisDevice));

    }

    public void updateView(@NonNull DataSnapshot dataSnapshot) {

        try {
            TextView sshOutput = (TextView) fragmentView.findViewById(R.id.TXV___DEVICESSH___SSH);
            sshOutput.setText(dataSnapshot.getValue().toString());

            final int scrollAmount = sshOutput.getLayout().getLineTop(sshOutput.getLineCount()) - sshOutput.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                sshOutput.scrollTo(0, scrollAmount);
            else
                sshOutput.scrollTo(0, 0);

        } catch (NullPointerException e) {

        }

    }


    private void refreshAdapter() {


    }

    private void sendSSHInputStream() {

        try {

            sshInputStream.flush();
            parent.sendCommandToDevice(new Message("__ssh_input_command", sshInputStream.toString(), parent.thisDevice));
            clearInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void addCharacterToBuffer(int unicodeChar) {

        sshInputStream.write(unicodeChar);
        sshInputStreamChanged = true;

    }

    public void sendBackSpace(){

        parent.sendCommandToDevice(new Message("__ssh_special","keyBackspace",parent.thisDevice));

    }

    private void clearInputStream() {

        sshInputStream = new ByteArrayOutputStream();
        sshInputStreamChanged = false;
    }

}
