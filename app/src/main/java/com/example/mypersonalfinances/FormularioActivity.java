package com.example.mypersonalfinances;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mypersonalfinances.database.ConexionSQLiteHelper;
import com.example.mypersonalfinances.databinding.ActivityFormularioBinding;
import com.example.mypersonalfinances.model.Transaccion;

/**
 * Pantalla de formulario para registrar o editar una transacción.
 * Ejecuta INSERT (modo nuevo) o UPDATE (modo edición) en SQLite.
 */
public class FormularioActivity extends AppCompatActivity {

    /** Clave del Intent para recibir el id de la transacción a editar. */
    public static final String EXTRA_ID_TRANSACCION = "extra_id_transaccion";

    private ActivityFormularioBinding binding;
    private ConexionSQLiteHelper conexionHelper;

    /** true = UPDATE, false = INSERT. */
    private boolean modoEdicion;
    private int idTransaccion = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormularioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        conexionHelper = new ConexionSQLiteHelper(this);

        leerModoFormulario();
        configurarToolbar();
        configurarBotonGuardar();

        // Si llega un id válido, precarga los datos en los campos (UPDATE)
        if (modoEdicion) {
            cargarTransaccionExistente();
        }
    }

    /**
     * Determina si el formulario abre en modo creación o edición
     * según el extra enviado desde MainActivity.
     */
    private void leerModoFormulario() {
        idTransaccion = getIntent().getIntExtra(EXTRA_ID_TRANSACCION, -1);
        modoEdicion = idTransaccion != -1;
    }

    /** Configura el título y el botón atrás de la toolbar. */
    private void configurarToolbar() {
        int titulo = modoEdicion
                ? R.string.titulo_editar_transaccion
                : R.string.titulo_nueva_transaccion;
        binding.toolbarFormulario.setTitle(titulo);
        binding.toolbarFormulario.setNavigationOnClickListener(v -> finish());
    }

    /** READ: obtiene la transacción por id y rellena los EditTexts. */
    private void cargarTransaccionExistente() {
        Transaccion transaccion = conexionHelper.obtenerTransaccionPorId(idTransaccion);

        if (transaccion == null) {
            Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.etConcepto.setText(transaccion.getConcepto());
        binding.etMonto.setText(String.valueOf(transaccion.getMonto()));

        if (transaccion.esIngreso()) {
            binding.rbIngreso.setChecked(true);
        } else {
            binding.rbGasto.setChecked(true);
        }
    }

    /** Valida los campos y ejecuta INSERT o UPDATE según el modo. */
    private void configurarBotonGuardar() {
        binding.btnGuardar.setOnClickListener(v -> guardarTransaccion());
    }

    private void guardarTransaccion() {
        // Limpiar errores previos de validación
        binding.layoutConcepto.setError(null);
        binding.layoutMonto.setError(null);

        String concepto = binding.etConcepto.getText().toString().trim();
        String textoMonto = binding.etMonto.getText().toString().trim();

        // Validación: concepto obligatorio
        if (TextUtils.isEmpty(concepto)) {
            binding.layoutConcepto.setError(getString(R.string.error_concepto_vacio));
            binding.etConcepto.requestFocus();
            return;
        }

        // Validación: monto numérico positivo
        double monto;
        try {
            monto = Double.parseDouble(textoMonto.replace(",", "."));
            if (monto <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            binding.layoutMonto.setError(getString(R.string.error_monto_invalido));
            binding.etMonto.requestFocus();
            return;
        }

        // Tipo según el RadioButton seleccionado
        String tipo = binding.rbIngreso.isChecked()
                ? getString(R.string.tipo_ingreso)
                : getString(R.string.tipo_gasto);

        Transaccion transaccion = new Transaccion(concepto, monto, tipo);
        boolean operacionExitosa;

        if (modoEdicion) {
            // UPDATE: se requiere el id del registro existente
            transaccion.setId(idTransaccion);
            operacionExitosa = conexionHelper.actualizarTransaccion(transaccion) > 0;
        } else {
            // CREATE: insertar nuevo registro
            operacionExitosa = conexionHelper.insertarTransaccion(transaccion) != -1;
        }

        if (operacionExitosa) {
            int mensaje = modoEdicion
                    ? R.string.transaccion_actualizada
                    : R.string.transaccion_guardada;
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.error_guardar, Toast.LENGTH_SHORT).show();
        }
    }
}
