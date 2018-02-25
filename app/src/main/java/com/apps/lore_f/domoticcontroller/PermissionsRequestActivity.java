package com.apps.lore_f.domoticcontroller;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class PermissionsRequestActivity extends AppCompatActivity {

    private final static int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private final static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1010;

    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_request);

    }

    @Override
    protected void onResume() {

        super.onResume();
        managePermissions();

    }

    private void managePermissions() {

        if (checkPermissions()) {

            startActivity(new Intent(this, MainActivity.class));
            finish();

        }

    }

    private boolean checkPermissions() {

        return
                askPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.PERMISSION_REQUEST_RATIONALE_1),
                        PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                ) && askPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.PERMISSION_REQUEST_RATIONALE_2),
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                );


    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        managePermissions();

    }


    private void showRationale(String message, final String permission, final int permissionID) {

        new AlertDialog.Builder(this)
                .setTitle(R.string.ALERTDIALOG_TITLE_PERMISSION_NOT_GRANTED)
                .setMessage(message)
                .setPositiveButton(R.string.ALERTDIALOG_GOT_IT, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                PermissionsRequestActivity.this,
                                new String[]{permission},
                                permissionID);
                    }
                })
                .create()
                .show();

    }

    private boolean askPermission(String permission, String permissionRationale, int permissionID) {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {

            /*
            Permesso non garantito
            Decide se mostrare una spiegazione oppure chiedere direttamente il permesso
             */

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                /*
                Mostra una spiegazione
                 */
                Log.d(TAG,String.format("Rationale needed for %s",permission));
                showRationale(
                        permissionRationale,
                        permission,
                        permissionID
                );

            } else {

                /*
                Chide direttamente il permesso
                 */
                Log.d(TAG,String.format("Rationale not needed for %s; asking for ",permission));
                ActivityCompat.requestPermissions(this, new String[]{permission}, permissionID);

            }

            return false;

        } else {

            /*
            Permesso garantito
             */
            Log.d(TAG,String.format("Permission %s already granted",permission));
            return true;

        }

    }

}




