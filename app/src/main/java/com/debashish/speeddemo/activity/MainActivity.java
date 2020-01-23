package com.debashish.speeddemo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.debashish.speeddemo.R;
import com.debashish.speeddemo.service.LocationService;

public class MainActivity extends AppCompatActivity {

    boolean status;
    LocationManager locationManager;
    public static long startTime, endTime;
    public static int p = 0;
    LocationService myService;
    private Button btnPause, btnStart, btnStop;

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1000);
            }
        }

        init();
    }

    /**
     * Initialize components
     */
    private void init() {
        //init variables
        btnPause = findViewById(R.id.button_pause);
        btnStart = findViewById(R.id.button_start);
        btnStop = findViewById(R.id.button_stop);

        setListeners();
    }

    /**
     * Setup click listeners
     */
    private void setListeners() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Starting service", Toast.LENGTH_SHORT).show();
                checkGPS();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                assert locationManager != null;
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    return;

                if (!status) {
                    bindService();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status) {
                    unbindService();
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnPause.getText().toString().equalsIgnoreCase("Pause")) {
                    btnPause.setText("Resume");
                } else {
                    btnPause.setText("Pause");
                }
            }
        });
    }

    /**
     * Setup GPS methods
     */
    private void checkGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            showGPSDisabledAlert();
    }

    private void showGPSDisabledAlert() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Enable GPS to use application");
        alertDialogBuilder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    private void bindService() {
        if (status)
            return;

        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
    }

    private void unbindService() {
        if (!status) {
            return;
        }
        startService(new Intent(getApplicationContext(), LocationService.class));
        unbindService(sc);
        status = false;
    }

    /**
     * handler methods
     */
    @Override
    protected void onDestroy() {
        if (status) {
            unbindService();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!status) {
            super.onBackPressed();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}