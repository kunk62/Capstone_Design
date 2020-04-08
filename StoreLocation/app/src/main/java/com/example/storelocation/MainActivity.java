package com.example.storelocation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button loc;
    TextView lonlat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loc = findViewById(R.id.location);
        lonlat = findViewById(R.id.lon);

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open(view);
            }
        });
    }
    public void open(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("현 위치를 집으로 설정하시겠습니까?");
        alertDialogBuilder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                            ActivityCompat.requestPermissions( MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                    0 );
                        }
                        else{
                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            double longitude = location.getLongitude();
                            double latitude = location.getLatitude();
                            lonlat.setText("위도 : "+longitude+"\n경도 : "+latitude);
                        }
                        Toast.makeText(MainActivity.this, "설정 완료", Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MainActivity.this, "설정 취소", Toast.LENGTH_LONG).show();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
