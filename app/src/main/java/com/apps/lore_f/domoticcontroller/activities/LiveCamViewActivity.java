package com.apps.lore_f.domoticcontroller.activities;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.apps.lore_f.domoticcontroller.R;

public class LiveCamViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_cam_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // recupera l'handler alla WebView
        WebView webView = (WebView) findViewById(R.id.WVW___LIVECAMVIEW___WEBVIEW);

        // imposta alcuni parametri della WebView
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        Intent intent = getIntent();
        if(intent.hasExtra("__URL_TO_VIEW")){

            webView.loadUrl(intent.getStringExtra("__URL_TO_VIEW"));

        } else {

            finish();

        }

    }

}
