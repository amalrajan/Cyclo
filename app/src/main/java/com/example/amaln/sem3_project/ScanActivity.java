package com.example.amaln.sem3_project;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

public class ScanActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    private CodeScannerView mScannerView;
    Button mOkayButton;
    TextView mAcceptPermission;
    private static final int PERMISSIONS_REQUEST = 1;
    private static final int PERMSISSION_GRANTED = 1;
    private static final int PERMISSION_NOT_GRANTED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mOkayButton = findViewById(R.id.button_okay);
        mAcceptPermission = findViewById(R.id.text_view_accept_permission);
        mScannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, mScannerView);

        checkPermissionGrantedAndOpenScanner();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    private void openScanner() {

        final Activity activity = ScanActivity.this;

        updateUI(1);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                Intent ridingIntent = new Intent(ScanActivity.this, RidingActivity.class);
                ridingIntent.putExtra("bluetooth_address", result.getText());
                startActivity(ridingIntent);
            }
        });
        mScannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void checkPermissionGrantedAndOpenScanner() {
        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            updateUI(PERMSISSION_GRANTED);
            openScanner();
        } else {
            updateUI(PERMISSION_NOT_GRANTED);
            mOkayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateUI(PERMSISSION_GRANTED);
            openScanner();
        }
    }

    private void updateUI(int state) {

        if(state == PERMSISSION_GRANTED) {
            mAcceptPermission.setVisibility(View.GONE);
            mOkayButton.setVisibility(View.GONE);
            mScannerView.setVisibility(View.VISIBLE);
        } else if(state == PERMISSION_NOT_GRANTED) {
            mScannerView.setVisibility(View.GONE);
            mAcceptPermission.setVisibility(View.VISIBLE);
            mOkayButton.setVisibility(View.VISIBLE);
        }

    }
}
