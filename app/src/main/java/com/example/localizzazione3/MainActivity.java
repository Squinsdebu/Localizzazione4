package com.example.localizzazione3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MediaRecorder mediaRecorder;
    private static final int REQUEST_PHONE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        richiediAutorizzazioni();
        Button signalButton = findViewById(R.id.button);
        signalButton.setOnClickListener(v -> checkPhonePermission());


    }
    private void checkPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE},
                    REQUEST_PHONE_PERMISSION);
        } else {
            getSignalStrength();
        }
    }


    private void richiediAutorizzazioni() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            } else {
               // ottieniPosizioneAttuale();
                //inizializzaAudio();
            }
        }


    private void ottieniPosizioneAttuale() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude(); // coordinate attuali
                        double longitude = location.getLongitude();
                        LatLng sydney = new LatLng(latitude,longitude);
                        myMap.addMarker(new MarkerOptions().position(sydney).title("Sydney"));
                        //myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        float zoomLevel = 15f; // livello di zoom
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));
                    } else {
                        // Se la posizione è nulla, rimando a sydney
                        LatLng sydney = new LatLng(-34,151);
                        myMap.addMarker(new MarkerOptions().position(sydney).title("Sydney"));
                        float zoomLevel = 15f;
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));
                    }
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, "Errore nell'ottenimento della posizione", Toast.LENGTH_SHORT).show());

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ottieniPosizioneAttuale();
            } else {
                Toast.makeText(this, "Le autorizzazioni sono necessarie per utilizzare questa funzionalità", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == REQUEST_PHONE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSignalStrength();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getSignalStrength() { // stampa del valore della calculateSignalQuality()
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                SignalStrength signalStrength = telephonyManager.getSignalStrength();
                int signalQuality = calculateSignalQuality(signalStrength);
                Toast.makeText(MainActivity.this, "Signal Quality: " + signalQuality, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int calculateSignalQuality(SignalStrength signalStrength) {
        // calcola la qualità del segnale e la resittuisce come numero intero
        if (signalStrength.isGsm()) {
            return signalStrength.getGsmSignalStrength();
        } else {
            return -1;
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        LatLng sydney = new LatLng(-34,151);
        myMap.addMarker(new MarkerOptions().position(sydney).title("Sydney"));
        myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        ottieniPosizioneAttuale();
    }
}

