package com.apps.lore_f.domoticcontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DeviceSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

        Intent deviceViewIntent = new Intent(this, DeviceViewActivity.class);
        deviceViewIntent.putExtra("DEVICE_TO_CONNECT", "lorenzofailla-home");

        startActivity(deviceViewIntent);

    }

}
