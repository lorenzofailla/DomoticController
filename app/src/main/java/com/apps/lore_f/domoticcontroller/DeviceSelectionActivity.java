package com.apps.lore_f.domoticcontroller;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DeviceSelectionActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()){

                case R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE:

                    // avvia l'activity per la gestione del cloud storage

            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_selection);

    }

    @Override
    protected void onResume(){

        super.onResume();

        // assegno OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(onClickListener);

    }

    @Override
    protected void onPause(){

        super.onPause();

        // rimuovo OnClickListener
        findViewById(R.id.BTN___DEVICE_SELECTION___CLOUDSTORAGE).setOnClickListener(null);

    }

}
