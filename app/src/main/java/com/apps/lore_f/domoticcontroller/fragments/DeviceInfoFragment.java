package com.apps.lore_f.domoticcontroller.fragments;

import androidx.fragment.app.Fragment;

public class DeviceInfoFragment extends Fragment {

    /*
    private static final String TAG = "DeviceInfoFragment";

    public boolean viewCreated = false;
    private View fragmentView;

    private DeviceViewActivity parent;

    private DeviceDataParser deviceData = new DeviceDataParser();

    public DeviceDataParser getDeviceData() {
        return this.deviceData;
    }

    public RecyclerView logsRecyclerView;
    public LinearLayoutManager linearLayoutManager;
    public FirebaseRecyclerAdapter<LogEntry, LogsHolder> firebaseAdapter;
    private DatabaseReference logsNode;

    public static class LogsHolder extends RecyclerView.ViewHolder {

        public TextView logTimeStamp;
        public TextView logDescription;
        public ImageView logImage;

        public LogsHolder(View v) {

            // call the superclass constructor
            super(v);

            // initialize the handler to the drawables
            logTimeStamp = (TextView) itemView.findViewById(R.id.TXV___LOG___TIMESTAMP);
            logDescription = (TextView) itemView.findViewById(R.id.TXV___LOG___DESCRIPTION);
            logImage = (ImageView) itemView.findViewById(R.id.TXV___LOG___IMAGE);

        }

    }

    private void refreshLogsAdapter() {

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(false);

        firebaseAdapter = new FirebaseRecyclerAdapter<LogEntry, LogsHolder>(
                LogEntry.class,
                R.layout.row_holder_log_element,
                LogsHolder.class,
                logsNode) {

            @Override
            protected void populateViewHolder(LogsHolder holder, LogEntry log, int position) {

                String logTime = GeneralUtilitiesLibrary.getTimeElapsed(
                        Long.parseLong(
                                log.getDatetime()
                        ), getContext()
                );

                holder.logTimeStamp.setText(logTime);

                holder.logDescription.setText(log.getLogdesc());

                int logImageResId;
                switch (log.getLogtype()) {

                    case "INET_OUT":
                        logImageResId = R.drawable.log_ok;

                    case "INET_IN":
                        logImageResId = R.drawable.log_ko;

                    default:
                        logImageResId = R.drawable.info;
                }

                holder.logImage.setImageResource(logImageResId);

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
                    logsRecyclerView.scrollToPosition(positionStart);
                }

            }

        });

        logsRecyclerView.setLayoutManager(linearLayoutManager);
        logsRecyclerView.setAdapter(firebaseAdapter);

    }

    private ValueEventListener logsValueEventLister = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            if (fragmentView != null) {

                refreshLogsAdapter();

            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    public interface DeviceInfoFragmentListener{
        void onUpdateRequest();
    }

    private DeviceInfoFragmentListener listener;

    public void setListener(DeviceInfoFragmentListener listener){
        this.listener=listener;
    }

    // VPN management
    private boolean isVPNConnected;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {

                case R.id.BTN___DEVICEINFOFRAGMENT___REBOOT:

                    parent.rebootHost();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN:

                    parent.shutdownHost();
                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG:

                    DatabaseReference logs = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Logs/%s", parent.groupName, parent.remoteDeviceName));
                    logs.removeValue();

                    break;

                case R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN:

                    if (deviceData.isConnectedToVPN()) {
                        // il dispositivo remoto è connesso alla VPN.
                        // richiede all'Activity parent di mandare un comando per disconnettere dalla VPN

                        parent.sendCommandToDevice(
                                new MessageStructure(
                                        "__disconnect_vpn",
                                        null,
                                        parent.thisDevice
                                )
                        );

                    } else {

                        // il dispositivo remoto non è connesso alla VPN.
                        // richiede all'Activity parent di mandare un comando per connettere alla VPN

                        parent.sendCommandToDevice(
                                new MessageStructure(
                                        "__connect_vpn",
                                        null,
                                        parent.thisDevice
                                )
                        );

                    }

                    break;

            }

        }

    };

    public DeviceInfoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_device_info, container, false);

        parent=(DeviceViewActivity) getActivity();

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView = view;

        // assegna un OnClickListener ai pulsanti
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(onClickListener);
        view.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(onClickListener);

        logsRecyclerView = (RecyclerView) view.findViewById(R.id.RWV___DEVICEINFOFRAGMENT___LOG);

        logsNode = FirebaseDatabase.getInstance().getReference(String.format("/Groups/%s/Logs/%s", parent.groupName, parent.remoteDeviceName));
        logsNode.addValueEventListener(logsValueEventLister);

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        if(listener!=null)
            listener.onUpdateRequest();

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // rimuove il ValueEventListener dal nodo del Database di Firebase
        DatabaseReference vpnStatusNode = FirebaseDatabase.getInstance().getReference("/Groups/%s/devices/%s/VPNStatus");

        logsNode.removeEventListener(logsValueEventLister);

        // rimuove l'OnClickListener ai pulsanti
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___CLEARLOG).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___REBOOT).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___SHUTDOWN).setOnClickListener(null);
        fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN).setOnClickListener(null);

    }

    private void manageVPNStatus(String status) {

        if (fragmentView == null) {
            return;
        }


    }

    public void refreshView() {


        if (fragmentView != null) {

            if (deviceData.isStatusDataValidated()) {

                TextView systemLoadTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___SYSLOAD_VALUE);
                TextView diskStatusTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___DISKSTATUS_VALUE);
                TextView runningSinceTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___RUNNINGSINCE_VALUE);
                TextView lastUpdateTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___LASTUPDATE_VALUE);

                systemLoadTextView.setText(String.format("%d %%", deviceData.getAverageLoad()));
                diskStatusTextView.setText(deviceData.getDiskStatus());
                runningSinceTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getRunningSince(), getContext()));
                lastUpdateTextView.setText(GeneralUtilitiesLibrary.getTimeElapsed(deviceData.getLastUpdate(), getContext()));

            }

            if (deviceData.isNetworkDataValidated()) {

                TextView publicIPTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___PUBLICIP_VALUE);
                TextView localIPTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___PRIVATEIP_VALUE);

                publicIPTextView.setText(deviceData.getPublicIPAddress());
                localIPTextView.setText(deviceData.getLocalIPAddressesList());

                int drawableToShow;
                String textToShow;
                boolean switchButtonEnabled;

                switch (deviceData.getVpnIPAddress()) {

                    case "<not-available>":

                        // vpn is not available
                        drawableToShow = R.drawable.broken;
                        textToShow = getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_NOT_AVAILABLE);
                        switchButtonEnabled = false;
                        break;

                    case "<not-connected>":
                        drawableToShow = R.drawable.vpn_black;
                        textToShow = getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_NOT_CONNECTED);
                        switchButtonEnabled = true;
                        isVPNConnected = false;
                        break;

                    default:
                        drawableToShow = R.drawable.vpn_green;
                        textToShow = String.format("%s IP: %s.", getString(R.string.DEVICEVIEW_LABEL_VPN_STATUS_CONNECTED), deviceData.getVpnIPAddress());
                        switchButtonEnabled = true;
                        isVPNConnected = true;
                        break;
                }

                ImageButton switchButton = (ImageButton) fragmentView.findViewById(R.id.BTN___DEVICEINFOFRAGMENT___MANAGE_VPN);
                TextView vpnStatusTextView = (TextView) fragmentView.findViewById(R.id.TXV___DEVICEINFOFRAGMENT___VPN_STATUS_VALUE);

                switchButton.setEnabled(switchButtonEnabled);
                switchButton.setImageResource(drawableToShow);
                vpnStatusTextView.setText(textToShow);

            }

        }

    }
    */

}
