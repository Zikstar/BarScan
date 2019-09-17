package com.example.barscan;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission_group.CAMERA;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA_CODE = 1;
    private ZXingScannerView scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //crete a new scanner view
        scanner = new ZXingScannerView(this);
        //set it as what is seen when the activity is launched
        setContentView(scanner);

        //check if the api level of the device is greater than 23
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(checkPermission()){

                Toast.makeText(MainActivity.this, "Permission is granted!", Toast.LENGTH_LONG).show();
            }

            else{

                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_CODE);
        Toast.makeText(this,"I have requested permission", Toast.LENGTH_LONG).show();
    }

    private boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode,String[] permission, int[] grantResults ){
        switch (requestCode){

            case REQUEST_CAMERA_CODE:
                if(grantResults.length >0){
                    boolean cameraAccepted = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_LONG).show();
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){

                                displayAlertMessage("You need to give both permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA_CODE);
                                            }
                                        });

                                return;

                            }

                        }
                    }
                }

                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(checkPermission()){

                if(scanner == null){

                    scanner = new ZXingScannerView(this);
                    setContentView(scanner);
                }
                scanner.setResultHandler(this);
                scanner.startCamera();
            }

            else{

                requestPermission();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanner.stopCamera();
    }


    private void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(final Result result) {
        final String myScanResult = result.getText();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scanner.resumeCameraPreview(MainActivity.this);
            }
        });

        builder.setNeutralButton("Visit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myScanResult));
                startActivity(intent);
            }
        });

        builder.setMessage(myScanResult);
        AlertDialog alert =  builder.create();
        alert.show();

    }
}
