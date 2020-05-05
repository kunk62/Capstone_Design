package com.example.storelocation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button loc;
    Button dir;
    TextView lonlat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc = findViewById(R.id.location);
        lonlat = findViewById(R.id.lon);
        dir = findViewById(R.id.direction);

        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    0 );
        }

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    open(view);
            }
        });
        dir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    String lat = lonlat.getText().subSequence(12, 21).toString();
                    String lon= lonlat.getText().subSequence(0, 10).toString();

                    String url = "nmap://route/public?dlat="+lat+"&dlng="+lon+"&dname=%EB%8F%84%EC%B0%A9%EC%9E%A5%EC%86%8C&appname=com.example.storelocation";

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);

                    List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    if (list == null || list.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
                    } else {
                        startActivity(intent);
                    }} catch (Exception e){Toast.makeText(MainActivity.this, "목적지를 입력하세요.", Toast.LENGTH_LONG).show();};
            }
        });
        try {
            FileInputStream fis = openFileInput("lonlat");
            if (fis!=null){
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                lonlat.setText(new String(buffer));
                fis.close();
            }
        } catch (IOException e) {}
    }
    public void open(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("현 위치를 집으로 설정하시겠습니까?");
        alertDialogBuilder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        LocationListener gpsLocationListener = new LocationListener() {
                            public void onLocationChanged(Location location) {
                                double longitude = location.getLongitude();
                                double latitude = location.getLatitude();
                            }
                            public void onStatusChanged(String provider, int status, Bundle extras) {}
                            public void onProviderEnabled(String provider) {}
                            public void onProviderDisabled(String provider) {}
                        };
                        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED&&
                        ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)&&
                                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                            }
                        }
                        else{
                            try {
                                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);

                                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                float longitude = (float)location.getLongitude();
                                float latitude = (float)location.getLatitude();
                                lonlat.setText(String.format("%.6f",longitude)+", "+String.format("%.6f",latitude));
                            } catch (Exception e){Toast.makeText(MainActivity.this, "위치 불러오기 실패", Toast.LENGTH_LONG).show();}

                            try {
                                FileOutputStream fos = openFileOutput("lonlat", Context.MODE_PRIVATE);
                                fos.write(lonlat.getText().toString().getBytes());
                                fos.close();
                            } catch (IOException e) {}
                        }
                    }
                });
        alertDialogBuilder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
