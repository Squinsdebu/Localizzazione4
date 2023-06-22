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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private MediaRecorder mediaRecorder;
    private static final int REQUEST_PHONE_PERMISSION = 1;
    private Button signalButton;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        richiediAutorizzazioni();
        signalButton = findViewById(R.id.button);
        signalButton.setOnClickListener(v -> checkPhonePermission());
       /* SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

*/
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
            ottieniPosizioneAttuale();
           // inizializzaAudio();
        }
    }

    private void ottieniPosizioneAttuale() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng currentLocation = new LatLng(latitude, longitude);
                        myMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    } else {
                        Toast.makeText(this, "Impossibile ottenere la posizione attuale", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, "Errore nell'ottenimento della posizione", Toast.LENGTH_SHORT).show());
    }




    private void inizializzaAudio(){
        // Inizializza il MediaRecorder per acquisire l'audio
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile("/dev/null");

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Avvia un thread per monitorare continuamente il volume del microfono
        Thread volumeThread = new Thread(() -> {
            while (true) {
                double volume = getMicrophoneVolume();
                // Fai qualcosa con il valore del volume (in dB)
                // Ad esempio, puoi aggiornare un'interfaccia utente o effettuare calcoli basati su di esso

                try {
                    Thread.sleep(10000); // Attendi 1 secondo prima di rileggere il volume
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        volumeThread.start();
    }
    // Metodo per ottenere il volume attuale del microfono
    private double getMicrophoneVolume() {
        if (mediaRecorder != null) {
            int amplitude = mediaRecorder.getMaxAmplitude();
            if (amplitude > 0) {
                double volume = 20 * Math.log10(amplitude);
                return volume;
            }
        }
        return 0;
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
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
                // Gestisci il caso in cui l'utente non ha concesso le autorizzazioni
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
    private void getSignalStrength() {
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
        // Calcola la qualità del segnale in base ai valori forniti da SignalStrength
        // Implementa la logica di calcolo appropriata per le tue esigenze specifiche
        // Restituisce un valore intero che rappresenta la qualità del segnale

        // Esempio di implementazione: Restituisce il livello di segnale CDMA (0-4)
        if (signalStrength.isGsm()) {
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            int signalQuality = (gsmSignalStrength >= 0 && gsmSignalStrength <= 31) ? gsmSignalStrength / 8 : -1;
            return signalQuality;
        } else {
            return -1;
        }
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
            myMap = googleMap;
            ottieniPosizioneAttuale();

    }
}






