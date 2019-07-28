package com.apps.lore_f.domoticcontroller.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.apps.lore_f.domoticcontroller.activities.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.generic.dataadapters.WOLDevicesListAdapter;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.WOLDeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WakeOnLanFragment extends Fragment {

    public boolean viewCreated = false;
    private View fragmentView;

    private static final String TAG = "WakeOnLanFragment";

    public DeviceViewActivity parent;

    ListView devicesListView;

    public WakeOnLanFragment() {
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
        View view = inflater.inflate(R.layout.fragment_wol_devices, container, false);

        // assegna l'handler ai controlli
        devicesListView = (ListView) view.findViewById(R.id.LWV___WOLDEVICESFRAGMENT___DEVICES);

        // inizializza l'handler alla view, in questo modo i componenti possono essere ritrovati
        fragmentView = view;

        // aggiorna il flag e effettua il trigger del metodo nel listener
        viewCreated = true;

        // chiama il metodo di aggiornamento dal parent
        parent.updateWOLFragment();

        return view;

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
    }

    @Override
    public void onDetach() {

        super.onDetach();

    }

    public void updateContent(String dataJSON) {

        if (!viewCreated)
            return;

        try {

            JSONObject data = new JSONObject(dataJSON);

            JSONArray devicesData = data.getJSONObject("WakeOnLan").getJSONArray("Devices");

            List<WOLDeviceInfo> devicesList = new ArrayList<WOLDeviceInfo>();

            for (int i=0; i< devicesData.length(); i++){
                devicesList.add(new WOLDeviceInfo(devicesData.getString(i)));
            }

            WOLDevicesListAdapter adapter = new WOLDevicesListAdapter(getContext(), R.layout.row_holder_wol_device_element, devicesList, this.parent);
            devicesListView.setAdapter(adapter);


        } catch (JSONException e) {
            Log.e(TAG, "Unable to update the view. JSON data: "+ dataJSON);
        }


    }

}
