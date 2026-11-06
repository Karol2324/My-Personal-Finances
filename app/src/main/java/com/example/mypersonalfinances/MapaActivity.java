package com.example.mypersonalfinances;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miMapa;
    private Button btnVolverMapa, btnTipoMapa;
    private boolean esSatelital = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        btnTipoMapa = findViewById(R.id.btnTipoMapa);
        btnVolverMapa = findViewById(R.id.btnVolverMapa);

        btnVolverMapa.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnTipoMapa.setOnClickListener(v -> {
            if (miMapa != null) {
                if (!esSatelital) {
                    miMapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    btnTipoMapa.setText("🗺️ NORMAL");
                    esSatelital = true;
                } else {
                    miMapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    btnTipoMapa.setText("🛰️ SATÉLITE");
                    esSatelital = false;
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        miMapa = googleMap;

        miMapa.getUiSettings().setZoomControlsEnabled(true);
        miMapa.getUiSettings().setCompassEnabled(true);

        // Coordenadas temáticas para MyPersonalFinances (Puntos de gasto / Sedes financieras)
        LatLng bogotaCentro = new LatLng(4.5981, -74.0760);
        miMapa.animateCamera(CameraUpdateFactory.newLatLngZoom(bogotaCentro, 14f));

        MarkerOptions pinPrincipal = new MarkerOptions()
                .position(bogotaCentro)
                .title("Sede Financiera Central")
                .snippet("Oficina principal de gestión y pagos");
        miMapa.addMarker(pinPrincipal);

        LatLng sucursalNorte = new LatLng(4.6580, -74.0550);
        MarkerOptions pinNorte = new MarkerOptions()
                .position(sucursalNorte)
                .title("Punto de Pago Chapinero")
                .snippet("Transacciones registradas: 18")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        miMapa.addMarker(pinNorte);

        LatLng sucursalOccidente = new LatLng(4.6300, -74.1200);
        MarkerOptions pinOccidente = new MarkerOptions()
                .position(sucursalOccidente)
                .title("Cajero / Sucursal Kennedy")
                .snippet("Punto de retiro frecuente")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        miMapa.addMarker(pinOccidente);

        miMapa.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false;
        });

        miMapa.setOnInfoWindowClickListener(marker -> {
            Toast.makeText(MapaActivity.this, "Seleccionaste: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }
}