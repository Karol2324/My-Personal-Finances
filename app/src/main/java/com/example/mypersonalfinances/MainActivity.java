package com.example.mypersonalfinances;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mypersonalfinances.adapter.TransaccionAdapter;
import com.example.mypersonalfinances.databinding.ActivityMainBinding;
import com.example.mypersonalfinances.model.Transaccion;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private ListenerRegistration listenerFirestore;
    private TransaccionAdapter adapter;
    private final List<Transaccion> listaTransacciones = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        configurarRecyclerView();
        configurarFab();
        escucharTransaccionesEnTiempoReal();
    }

    private void configurarRecyclerView() {
        adapter = new TransaccionAdapter(listaTransacciones);
        binding.recyclerViewTransacciones.setAdapter(adapter);
    }

    private void configurarFab() {
        binding.fabAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FormularioActivity.class);
            startActivity(intent);
        });
    }

    private void escucharTransaccionesEnTiempoReal() {
        listenerFirestore = db.collection("transacciones")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error al escuchar cambios", error);
                        Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        listaTransacciones.clear();
                        double totalIngresos = 0.0;
                        double totalGastos = 0.0;

                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            // Extraer valores garantizando compatibilidad
                            String concepto = doc.getString("concepto");
                            if (concepto == null) concepto = doc.getString("Concepto");

                            Double montoObj = doc.getDouble("monto");
                            if (montoObj == null) montoObj = doc.getDouble("Monto");
                            double monto = (montoObj != null) ? montoObj : 0.0;

                            String tipo = doc.getString("tipo");
                            if (tipo == null) tipo = doc.getString("Tipo");

                            if (concepto != null && tipo != null) {
                                Transaccion t = new Transaccion(doc.getId(), concepto, monto, tipo);
                                listaTransacciones.add(t);

                                // Suma de totales en tiempo real
                                if ("INGRESO".equalsIgnoreCase(tipo)) {
                                    totalIngresos += monto;
                                } else if ("GASTO".equalsIgnoreCase(tipo)) {
                                    totalGastos += monto;
                                }
                            }
                        }


                        adapter.notifyDataSetChanged();
                        actualizarEstadoListaVacia(listaTransacciones.isEmpty());
                        binding.tvTotalIngresos.setText(String.format("$ %.2f", totalIngresos));
                        binding.tvTotalGastos.setText(String.format("$ %.2f", totalGastos));
                    }
                });
    }

    private void actualizarEstadoListaVacia(boolean estaVacia) {
        binding.tvListaVacia.setVisibility(estaVacia ? View.VISIBLE : View.GONE);
        binding.recyclerViewTransacciones.setVisibility(estaVacia ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerFirestore != null) {
            listenerFirestore.remove();
        }
    }
}