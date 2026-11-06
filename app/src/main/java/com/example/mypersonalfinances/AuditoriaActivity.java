package com.example.mypersonalfinances;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypersonalfinances.adapter.TransaccionAdapter;
import com.example.mypersonalfinances.model.Transaccion;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuditoriaActivity extends AppCompatActivity {

    private TextView tvTotalTransacciones, tvAuditoriaIngresos, tvAuditoriaGastos;
    private RecyclerView rvAuditoria;
    private ProgressBar pbCargandoAuditoria;
    private TransaccionAdapter adapter;
    private List<Transaccion> listaTransacciones;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditoria);

        // Inicializar vistas
        MaterialToolbar toolbar = findViewById(R.id.toolbarAuditoria);
        tvTotalTransacciones = findViewById(R.id.tvTotalTransacciones);
        tvAuditoriaIngresos = findViewById(R.id.tvAuditoriaIngresos);
        tvAuditoriaGastos = findViewById(R.id.tvAuditoriaGastos);
        rvAuditoria = findViewById(R.id.rvAuditoria);
        pbCargandoAuditoria = findViewById(R.id.pbCargandoAuditoria);

        // Configuración de navegación
        toolbar.setNavigationOnClickListener(v -> finish());

        // Inicializar RecyclerView y Firebase
        rvAuditoria.setLayoutManager(new LinearLayoutManager(this));
        listaTransacciones = new ArrayList<>();

        // Reutilizamos el TransaccionAdapter existente
        adapter = new TransaccionAdapter(listaTransacciones);
        rvAuditoria.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        cargarTodasLasTransacciones();
    }

    private void cargarTodasLasTransacciones() {
        pbCargandoAuditoria.setVisibility(View.VISIBLE);

        // Consulta global sin filtrar por usuarioId (vista administrativa)
        db.collection("transacciones")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaTransacciones.clear();
                    double totalIngresos = 0;
                    double totalGastos = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaccion t = doc.toObject(Transaccion.class);
                        if (t != null) {
                            listaTransacciones.add(t);

                            // Acumuladores para el panel superior
                            if ("Ingreso".equalsIgnoreCase(t.getTipo())) {
                                totalIngresos += t.getMonto();
                            } else {
                                totalGastos += t.getMonto();
                            }
                        }
                    }

                    // Actualizar métricas calculadas
                    tvTotalTransacciones.setText(String.valueOf(listaTransacciones.size()));

                    NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
                    tvAuditoriaIngresos.setText(formato.format(totalIngresos));
                    tvAuditoriaGastos.setText(formato.format(totalGastos));

                    adapter.notifyDataSetChanged();
                    pbCargandoAuditoria.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    pbCargandoAuditoria.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar auditoría: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}