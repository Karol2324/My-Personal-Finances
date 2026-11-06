package com.example.mypersonalfinances;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UbicacionActivity extends AppCompatActivity {

    private TextView tvLatitud, tvLongitud, tvDireccion;
    private Button btnObtenerUbicacion;
    private SwitchMaterial swRastreoTiempoReal;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> solicitarPermisosUbicacionLauncher;

    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean rastreoActivo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Georreferenciación GPS");
        }

        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvDireccion = findViewById(R.id.tvDireccion);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);
        swRastreoTiempoReal = findViewById(R.id.swRastreoTiempoReal);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        solicitarPermisosUbicacionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if ((fineLocationGranted != null && fineLocationGranted) ||
                            (coarseLocationGranted != null && coarseLocationGranted)) {
                        Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                        obtenerUltimaUbicacion();
                    } else {
                        Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_LONG).show();
                    }
                }
        );

        btnObtenerUbicacion.setOnClickListener(v -> verificarPermisosYObtenerUbicacion());

        configurarRastreoEnTiempoReal();

        swRastreoTiempoReal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                iniciarActualizacionesUbicacion();
            } else {
                detenerActualizacionesUbicacion();
            }
        });
    }

    private void verificarPermisosYObtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUltimaUbicacion();
        } else {
            solicitarPermisosUbicacionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtenerUltimaUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                actualizarUI(location);
            } else {
                Toast.makeText(this, "Sin ubicación previa en caché. Activa el GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void actualizarUI(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        tvLatitud.setText("Latitud: " + lat);
        tvLongitud.setText("Longitud: " + lon);

        convertirCoordenadasADireccion(lat, lon);
    }

    private void convertirCoordenadasADireccion(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address dir = direcciones.get(0);
                String direccionCompleta = dir.getAddressLine(0);
                tvDireccion.setText(direccionCompleta);
            } else {
                tvDireccion.setText("Dirección no disponible para estas coordenadas.");
            }
        } catch (IOException e) {
            tvDireccion.setText("Error al consultar Geocoder: " + e.getMessage());
        }
    }

    private void configurarRastreoEnTiempoReal() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    actualizarUI(location);
                }
            }
        };
    }

    private void iniciarActualizacionesUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            swRastreoTiempoReal.setChecked(false);
            Toast.makeText(this, "Se requiere permiso de GPS preciso para rastreo", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        rastreoActivo = true;
        Toast.makeText(this, "Rastreo GPS en tiempo real activado", Toast.LENGTH_SHORT).show();
    }

    private void detenerActualizacionesUbicacion() {
        if (rastreoActivo) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            rastreoActivo = false;
            Toast.makeText(this, "Rastreo detenido", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta Activity y regresa a MainActivity
        return true;
    }
}
