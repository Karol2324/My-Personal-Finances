package com.example.mypersonalfinances;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mypersonalfinances.adapter.TransaccionAdapter;
import com.example.mypersonalfinances.model.Transaccion;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvBienvenida, tvRolUsuario, tvTotalIngresos, tvTotalGastos, tvListaVacia;
    private Button btnCerrarSesion, btnModuloAuditoria;
    private FloatingActionButton fabAgregar;

    private RecyclerView recyclerViewTransacciones;
    private TransaccionAdapter adapter;
    private List<Transaccion> listaTransacciones = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Inicializar vistas
        tvBienvenida = findViewById(R.id.tvBienvenida);
        tvRolUsuario = findViewById(R.id.tvRolUsuario);
        tvTotalIngresos = findViewById(R.id.tvTotalIngresos);
        tvTotalGastos = findViewById(R.id.tvTotalGastos);
        tvListaVacia = findViewById(R.id.tvListaVacia);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        fabAgregar = findViewById(R.id.fabAgregar);

        Button btnAbrirGPS = findViewById(R.id.btnAbrirGPS);
        Button btnAbrirMapa = findViewById(R.id.btnAbrirMapa);
        Button btnAbrirEvidencias = findViewById(R.id.btnAbrirEvidencias);
        btnModuloAuditoria = findViewById(R.id.btnModuloAuditoria);

        // 2. Configurar RecyclerView
        recyclerViewTransacciones = findViewById(R.id.recyclerViewTransacciones);
        recyclerViewTransacciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransaccionAdapter(listaTransacciones);
        recyclerViewTransacciones.setAdapter(adapter);

        // 3. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 4. Configurar eventos de navegación
        if (btnCerrarSesion != null) {
            btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
        }

        if (fabAgregar != null) {
            fabAgregar.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FormularioActivity.class)));
        }

        if (btnAbrirGPS != null) {
            btnAbrirGPS.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, UbicacionActivity.class)));
        }

        if (btnAbrirMapa != null) {
            btnAbrirMapa.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MapaActivity.class)));
        }

        if (btnAbrirEvidencias != null) {
            btnAbrirEvidencias.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EvidenciaActivity.class)));
        }

        // Asignación directa del botón de Auditoría
        if (btnModuloAuditoria != null) {
            btnModuloAuditoria.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AuditoriaActivity.class);
                startActivity(intent);
            });
        }

        // 5. Verificar Rol (primero validación local/rápida, luego sincronización con Firestore)
        verificarRolUsuario();

        // 6. Cargar transacciones en tiempo real
        cargarTransaccionesDesdeFirestore();
    }

    private void verificarRolUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";

        // Asignación de nombre por defecto o personalizado según el correo
        String nombre = "Diana Guerrero";
        if (!email.equals("karolisam_27@hotmail.com")) {
            if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                nombre = user.getDisplayName();
            } else if (!email.isEmpty()) {
                nombre = email.split("@")[0];
            } else {
                nombre = "Usuario";
            }
        }

        // 1. Asignación inmediata por correo clave
        if (email.contains("admin") || email.equals("karolisam_27@hotmail.com")) {
            actualizarInterfazSegunRol(nombre, "Administrador");
        } else {
            actualizarInterfazSegunRol(nombre, "Cliente");
        }

        // 2. Sincronización con Firestore
        final String nombreBase = nombre;
        db.collection("usuarios").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String nombreDb = doc.getString("nombre");
                        String rolDb = doc.getString("rol");

                        String nombreFinal = (nombreDb != null && !nombreDb.trim().isEmpty()) ? nombreDb : nombreBase;
                        String rolFinal = ("Administrador".equalsIgnoreCase(rolDb) || email.equals("karolisam_27@hotmail.com"))
                                ? "Administrador" : "Cliente";

                        actualizarInterfazSegunRol(nombreFinal, rolFinal);
                    }
                });
    }

    private void actualizarInterfazSegunRol(String nombre, String rol) {
        if (tvBienvenida != null) {
            tvBienvenida.setText("¡Bienvenido/a, " + nombre + "!");
        }

        boolean esAdmin = "Administrador".equalsIgnoreCase(rol);

        if (tvRolUsuario != null) {
            tvRolUsuario.setText("Rol: " + rol);
            tvRolUsuario.setTextColor(esAdmin ?
                    android.graphics.Color.parseColor("#D32F2F") :
                    android.graphics.Color.parseColor("#388E3C"));
        }

        if (btnModuloAuditoria != null) {
            btnModuloAuditoria.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
        }
    }

    private void cargarTransaccionesDesdeFirestore() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).collection("transacciones")
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        return;
                    }

                    listaTransacciones.clear();

                    if (querySnapshots != null && !querySnapshots.isEmpty()) {
                        double ingresosTotal = 0.0;
                        double gastosTotal = 0.0;

                        for (QueryDocumentSnapshot doc : querySnapshots) {
                            Transaccion t = doc.toObject(Transaccion.class);

                            if (t.getId() == null || t.getId().isEmpty()) {
                                t.setId(doc.getId());
                            }

                            listaTransacciones.add(t);

                            if ("Ingreso".equalsIgnoreCase(t.getTipo())) {
                                ingresosTotal += t.getMonto();
                            } else if ("Gasto".equalsIgnoreCase(t.getTipo())) {
                                gastosTotal += t.getMonto();
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvTotalIngresos.setText(String.format("$ %.2f", ingresosTotal));
                        tvTotalGastos.setText(String.format("$ %.2f", gastosTotal));
                        tvListaVacia.setVisibility(View.GONE);
                    } else {
                        adapter.notifyDataSetChanged();
                        tvTotalIngresos.setText("$ 0.00");
                        tvTotalGastos.setText("$ 0.00");
                        tvListaVacia.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void cerrarSesion() {
        mAuth.signOut();
        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}