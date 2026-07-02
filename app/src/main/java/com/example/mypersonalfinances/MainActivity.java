package com.example.mypersonalfinances;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mypersonalfinances.adapter.TransaccionAdapter;
import com.example.mypersonalfinances.database.ConexionSQLiteHelper;
import com.example.mypersonalfinances.databinding.ActivityMainBinding;
import com.example.mypersonalfinances.model.Transaccion;

import java.util.ArrayList;

/**
 * Pantalla principal de "Mis Finanzas".
 * Muestra el listado de transacciones y coordina las operaciones CRUD
 * delegando la persistencia a {@link ConexionSQLiteHelper}.
 */
public class MainActivity extends AppCompatActivity
        implements TransaccionAdapter.OnTransaccionClickListener {

    private ActivityMainBinding binding;
    private ConexionSQLiteHelper conexionHelper;
    private TransaccionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding: enlaza las vistas de activity_main.xml sin findViewById
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instancia única del helper SQLite para toda la Activity
        conexionHelper = new ConexionSQLiteHelper(this);

        configurarRecyclerView();
        configurarFab();
    }

    /**
     * onResume recarga la lista cada vez que volvemos desde FormularioActivity
     * o tras eliminar un registro, manteniendo la UI sincronizada con SQLite.
     */
    @Override
    protected void onResume() {
        super.onResume();
        cargarTransacciones();
    }

    /** Configura el RecyclerView con su adaptador personalizado. */
    private void configurarRecyclerView() {
        adapter = new TransaccionAdapter(new ArrayList<>(), this);
        binding.recyclerViewTransacciones.setAdapter(adapter);
    }

    /** El FAB abre el formulario en modo creación (sin id). */
    private void configurarFab() {
        binding.fabAgregar.setOnClickListener(v -> abrirFormularioNuevaTransaccion());
    }

    /** READ: consulta SQLite y actualiza el RecyclerView. */
    private void cargarTransacciones() {
        ArrayList<Transaccion> transacciones = conexionHelper.obtenerTransacciones();
        adapter.actualizarLista(transacciones);
        actualizarEstadoListaVacia(transacciones.isEmpty());
    }

    /** Muestra u oculta el mensaje cuando no hay registros. */
    private void actualizarEstadoListaVacia(boolean estaVacia) {
        binding.tvListaVacia.setVisibility(estaVacia ? View.VISIBLE : View.GONE);
        binding.recyclerViewTransacciones.setVisibility(estaVacia ? View.GONE : View.VISIBLE);
    }

    /** Abre FormularioActivity sin extras para insertar una transacción nueva. */
    private void abrirFormularioNuevaTransaccion() {
        Intent intent = new Intent(this, FormularioActivity.class);
        startActivity(intent);
    }

    /** Abre FormularioActivity en modo edición pasando el id por Intent. */
    private void abrirFormularioEdicion(Transaccion transaccion) {
        Intent intent = new Intent(this, FormularioActivity.class);
        intent.putExtra(FormularioActivity.EXTRA_ID_TRANSACCION, transaccion.getId());
        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Callbacks del TransaccionAdapter
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onEditarClick(Transaccion transaccion) {
        abrirFormularioEdicion(transaccion);
    }

    @Override
    public void onEliminarClick(Transaccion transaccion) {
        mostrarDialogoConfirmacionEliminar(transaccion);
    }

    /** DELETE: pide confirmación antes de borrar el registro en SQLite. */
    private void mostrarDialogoConfirmacionEliminar(Transaccion transaccion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar)
                .setMessage(getString(R.string.confirmar_eliminar_mensaje, transaccion.getConcepto()))
                .setPositiveButton(R.string.btn_confirmar, (dialog, which) -> {
                    int filasEliminadas = conexionHelper.eliminarTransaccion(transaccion.getId());
                    if (filasEliminadas > 0) {
                        Toast.makeText(this, R.string.transaccion_eliminada, Toast.LENGTH_SHORT).show();
                        cargarTransacciones();
                    }
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }
}
