package com.example.storelocation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

public class MainActivity extends AppCompatActivity {
    Button loc;
    ImageView qr;
    TextView lonlat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc = findViewById(R.id.location);
        lonlat = findViewById(R.id.lon);
        qr = findViewById(R.id.qrcode);

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

        try {
            FileInputStream fis = openFileInput("lonlat");
            if (fis!=null){
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                lonlat.setText(new String(buffer));
                fis.close();
            }
            Bitmap bm = BitmapFactory.decodeFile("/data/data/com.example.storelocation/filesqrcode");
            if(bm!=null) qr.setImageBitmap(bm);
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

                        if (ContextCompat.checkSelfPermission( MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                            Toast.makeText(MainActivity.this, "error", Toast.LENGTH_LONG).show();
                        }
                        else{
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpsLocationListener);
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);

                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            float longitude = (float)location.getLongitude();
                            float latitude = (float)location.getLatitude();
                            lonlat.setText("위도 : "+String.format("%.6f",longitude)+"\n경도 : "+String.format("%.6f",latitude));

                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            try{
                                BitMatrix bitMatrix = multiFormatWriter.encode(lonlat.getText().toString(), BarcodeFormat.QR_CODE,200,200);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                qr.setImageBitmap(bitmap);

                                File tempFile = new File(getFilesDir()+"qrcode");

                                FileOutputStream ios = new FileOutputStream(tempFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ios);
                            }catch (Exception e){}

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
