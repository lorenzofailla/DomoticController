package com.apps.lore_f.domoticcontroller;

import androidx.fragment.app.Fragment;

public class DeviceSSHFragment extends Fragment {

//    public boolean viewCreated = false;
//    private View fragmentView;
//
//    private SSHView sshOutput;
//
//    private DatabaseReference sshOutputNode;
//
//    public DeviceViewActivity parent;
//
//    private Handler handler;
//
//    private long sshInputStreamCheckTimeout = 250L;
//
//    private ByteArrayOutputStream sshInputStream = new ByteArrayOutputStream();
//    private boolean sshInputStreamChanged = false;
//
//    private Runnable manageSSHInputStream = new Runnable() {
//        @Override
//        public void run() {
//
//            if (sshInputStreamChanged) {
//
//                sendSSHInputStream();
//
//            }
//
//            handler.postDelayed(this, sshInputStreamCheckTimeout);
//
//        }
//
//    };
//
//    private View.OnClickListener sshKeysListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//            String result = null;
//
//            switch (v.getId()) {
//
//                case R.id.BTN___DEVICESSH___KEYUP:
//                    result = "keyUp";
//                    break;
//
//                case R.id.BTN___DEVICESSH___KEYDOWN:
//                    result = "keyDown";
//                    break;
//
//                case R.id.BTN___DEVICESSH___KEYRIGHT:
//                    result = "keyRight";
//                    break;
//
//                case R.id.BTN___DEVICESSH___KEYLEFT:
//                    result = "keyLeft";
//                    break;
//
//            }
//
//            if (result != null) {
//                parent.sendCommandToDevice(new MessageStructure("__ssh_special", result, parent.thisDevice));
//            }
//
//        }
//    };
//
//
//
//    public DeviceSSHFragment() {
//        // Required empty public constructor
//
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_device_ssh, container, false);
//
//        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
//        fragmentView = view;
//
//        sshOutput = (SSHView) view.findViewById(R.id.TXV___DEVICESSH___SSH);
//        //sshOutput.setMovementMethod(add_new ScrollingMovementMethod());
//
//
//        // assegna un OnClickListener ai pulsanti
//        ImageButton sendCommandButton = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___SENDCOMMAND);
//        sendCommandButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final EditText sshCommandToSend = new EditText(getContext());
//
//                new AlertDialog.Builder(getContext())
//                        .setMessage(R.string.ALERTDIALOG_MESSAGE_ENTER_SSH_COMMAND)
//                        .setView(sshCommandToSend)
//                        .setPositiveButton(R.string.ALERTDIALOG_YES, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                parent.sendCommandToDevice(new MessageStructure("__ssh_input_command", sshCommandToSend.getText().toString() + "\n", parent.thisDevice));
//
//                            }
//
//                        })
//
//                        .setNegativeButton(R.string.ALERTDIALOG_NO, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//
//                        .setTitle(R.string.ALERTDIALOG_TITLE_ENTER_SSH_COMMAND)
//                        .create()
//                        .show();
//
//            }
//        });
//
//
//        ImageButton showKeyBoardButton = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
//        showKeyBoardButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Log.i(TAG, "Showing keyboard");
//                sshOutput.showKeyboard();
//
//            }
//
//        });
//
//        ImageButton sendKeyUp = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYUP);
//        sendKeyUp.setOnClickListener(sshKeysListener);
//
//        ImageButton sendKeyDown = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYDOWN);
//        sendKeyDown.setOnClickListener(sshKeysListener);
//
//        ImageButton sendKeyRight = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYRIGHT);
//        sendKeyRight.setOnClickListener(sshKeysListener);
//
//        ImageButton sendKeyLeft = (ImageButton) view.findViewById(R.id.BTN___DEVICESSH___KEYLEFT);
//        sendKeyLeft.setOnClickListener(sshKeysListener);
//
//        // inizializza l'handler
//        handler = new Handler();
//
//        // esegue il callback "manageSSHInputStream"
//        handler.postDelayed(manageSSHInputStream, 0L);
//
//        // aggiorna il flag e effettua il trigger del metodo nel listener
//        viewCreated = true;
//
//        return view;
//
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//
//        // rimuove l'OnClickListener ai pulsanti
//        ImageButton sendCommandButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
//        sendCommandButton.setOnClickListener(null);
//
//        ImageButton showKeyBoardButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYBOARD);
//        showKeyBoardButton.setOnClickListener(null);
//
//        ImageButton sendKeyUp = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYUP);
//        sendKeyUp.setOnClickListener(null);
//
//        ImageButton sendKeyDown = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYDOWN);
//        sendKeyDown.setOnClickListener(null);
//
//        ImageButton sendKeyRight = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYRIGHT);
//        sendKeyRight.setOnClickListener(null);
//
//        ImageButton sendKeyLeft = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICESSH___KEYLEFT);
//        sendKeyLeft.setOnClickListener(null);
//
//
//        // rimuove l'esecuzione del callback "manageSSHInputStream"
//        handler.removeCallbacks(manageSSHInputStream);
//
//        // comunica al dispositivo remoto di disconnettere la sessione ssh
//        parent.sendCommandToDevice(new MessageStructure("__close_ssh", "null", parent.thisDevice));
//
//    }
//
//
//    private void refreshAdapter() {
//
//    }
//
//    private void sendSSHInputStream() {
//
//        try {
//
//            sshInputStream.flush();
//            parent.sendCommandToDevice(new MessageStructure("__ssh_input_command", sshInputStream.toString(), parent.thisDevice));
//            clearInputStream();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void addCharacterToBuffer(int unicodeChar) {
//
//        sshInputStream.write(unicodeChar);
//        sshInputStreamChanged = true;
//
//    }
//
//    public void sendBackSpace() {
//
//        parent.sendCommandToDevice(new MessageStructure("__ssh_special", "keyBackspace", parent.thisDevice));
//
//    }
//
//    private void clearInputStream() {
//
//        sshInputStream = new ByteArrayOutputStream();
//        sshInputStreamChanged = false;
//    }

}
