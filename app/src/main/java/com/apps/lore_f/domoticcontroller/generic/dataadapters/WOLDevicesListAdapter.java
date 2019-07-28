package com.apps.lore_f.domoticcontroller.generic.dataadapters;

import android.content.Context;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.activities.DeviceViewActivity;
import com.apps.lore_f.domoticcontroller.R;
import com.apps.lore_f.domoticcontroller.generic.classes.Message;
import com.apps.lore_f.domoticcontroller.generic.dataobjects.WOLDeviceInfo;

import java.util.List;

/**
 * Created by 105053228 on 20/apr/2017.
 */

public class WOLDevicesListAdapter extends ArrayAdapter<WOLDeviceInfo> {

    public WOLDevicesListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<WOLDeviceInfo> objects, final DeviceViewActivity parentDeviceViewActivity) {
        super(context, resource, objects);

        parentDVA = parentDeviceViewActivity;

    }

    final DeviceViewActivity parentDVA;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_holder_wol_device_element, null);

        // inizializza l'handler ai vari controlli
        TextView deviceName = (TextView) convertView.findViewById(R.id.TXV___ROWWOLDEVICE___DEVICENAME);
        ImageButton wakeDevice = (ImageButton) convertView.findViewById(R.id.BTN___ROWWOLDEVICE___CONNECT);

        final WOLDeviceInfo deviceInfo = getItem(position);

        // imposta la visualizzazione degli elementi
        deviceName.setText(deviceInfo.getName());

        // assegna i listener ai pulsanti ImageButton
        wakeDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                parentDVA.sendCommandToDevice(new Message("__wakeonlan", deviceInfo.getId(), parentDVA.thisDevice));

            }

        });

        return convertView;

    }

}
