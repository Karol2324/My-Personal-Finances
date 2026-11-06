package com.example.mypersonalfinances;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mypersonalfinances.databinding.ActivityFormularioBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class FormularioActivity extends AppCompatActivity {

    private ActivityFormularioBinding binding;
    private FirebaseFirestore db;

    private String transaccionId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFormularioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        leerDatosIntent();
        configurarToolbar();
        configurarValidacionesTiempoReal();
        configurarBotones();
    }

    private void leerDatosIntent() {
        if (getIntent().hasExtra("id")) {
            transaccionId = getIntent().getStringExtra("id");
            binding.etConcepto.setText(getIntent().getStringExtra("concepto"));
            binding.etMonto.setText(String.valueOf(getIntent().getDoubleExtra("monto", 0.0)));

            String tipo = getIntent().getStringExtra("tipo");
            if ("GASTO".equalsIgnoreCase(tipo) || "Gasto".equalsIgnoreCase(tipo)) {
                binding.rbGasto.setChecked(true);
            } else {
                binding.rbIngreso.setChecked(true);
            }

            binding.btnGuardar.setText("ACTUALIZAR TRANSACCIÓN");
            binding.btnEliminar.setVisibility(View.VISIBLE);
        }
    }

    private void configurarToolbar() {
        int titulo = (transaccionId != null)
                ? R.string.titulo_editar_transaccion
                : R.string.titulo_nueva_transaccion;
        binding.toolbarFormulario.setTitle(titulo);
        binding.toolbarFormulario.setNavigationOnClickListener(v -> finish());
    }

    private void configurarValidacionesTiempoReal() {

        binding.etConcepto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() < 3) {
                    binding.layoutConcepto.setError("Ingrese al menos 3 caracteres");
                } else {
                    binding.layoutConcepto.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etMonto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    binding.layoutMonto.setError("El monto es requerido");
                } else try {
                    double valor = Double.parseDouble(s.toString().trim().replace(",", "."));
                    if (valor <= 0) {
                        binding.layoutMonto.setError("El monto debe ser mayor a 0");
                    } else {
                        binding.layoutMonto.setErrorEnabled(false);
                    }
                } catch (NumberFormatException e) {
                    binding.layoutMonto.setError("Monto inválido");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void configurarBotones() {
        binding.btnGuardar.setOnClickListener(v -> {
            if (esFormularioValido()) {
                guardarEnFirestore();
            }
        });

        binding.btnEliminar.setOnClickListener(v -> eliminarDeFirestore());
    }

    private boolean esFormularioValido() {
        boolean valido = true;
        String concepto = binding.etConcepto.getText().toString().trim();
        String textoMonto = binding.etMonto.getText().toString().trim();

        if (TextUtils.isEmpty(concepto) || concepto.length() < 3) {
            binding.layoutConcepto.setError("Ingrese un concepto válido");
            valido = false;
        }

        if (TextUtils.isEmpty(textoMonto)) {
            binding.layoutMonto.setError("Ingrese un monto");
            valido = false;
        } else {
            try {
                double monto = Double.parseDouble(textoMonto.replace(",", "."));
                if (monto <= 0) {
                    binding.layoutMonto.setError("El monto debe ser mayor a 0");
                    valido = false;
                }
            } catch (Exception e) {
                binding.layoutMonto.setError("Monto no válido");
                valido = false;
            }
        }
        return valido;
    }

    private void guardarEnFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        String concepto = binding.etConcepto.getText().toString().trim();
        double monto = Double.parseDouble(binding.etMonto.getText().toString().trim().replace(",", "."));
        String tipo = binding.rbIngreso.isChecked() ? "Ingreso" : "Gasto";

        binding.btnGuardar.setEnabled(false);
        binding.pbCargaFormulario.setVisibility(View.VISIBLE);

        Map<String, Object> transaccionMap = new HashMap<>();
        transaccionMap.put("concepto", concepto);
        transaccionMap.put("monto", monto);
        transaccionMap.put("tipo", tipo);

        if (transaccionId == null) {
            // Crear nueva transacción en Firestore
            db.collection("usuarios").document(uid)
                    .collection("transacciones").add(transaccionMap)
                    .addOnSuccessListener(documentReference -> {
                        // Guardar el ID auto-generado dentro del mismo documento
                        documentReference.update("id", documentReference.getId());
                        Toast.makeText(this, R.string.transaccion_guardada, Toast.LENGTH_SHORT).show();
                        NotificacionHelper.emitirNotificacion(
                                this,
                                "¡Transacción Registrada!",
                                "El movimiento financiero se guardó correctamente en la nube."
                        );
                        finish();
                    })
                    .addOnFailureListener(e -> restaurarEstadoBotones("Error al guardar: " + e.getMessage()));
        } else {
            // Actualizar transacción existente
            transaccionMap.put("id", transaccionId);
            db.collection("usuarios").document(uid)
                    .collection("transacciones").document(transaccionId).update(transaccionMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.transaccion_actualizada, Toast.LENGTH_SHORT).show();
                        NotificacionHelper.emitirNotificacion(
                                this,
                                "¡Transacción Actualizada!",
                                "La actualización se guardó correctamente en la nube."
                        );
                        finish();
                    })
                    .addOnFailureListener(e -> restaurarEstadoBotones("Error al actualizar: " + e.getMessage()));
        }
    }

    private void eliminarDeFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (transaccionId != null && currentUser != null) {
            String uid = currentUser.getUid();

            binding.btnGuardar.setEnabled(false);
            binding.btnEliminar.setEnabled(false);
            binding.pbCargaFormulario.setVisibility(View.VISIBLE);

            db.collection("usuarios").document(uid)
                    .collection("transacciones").document(transaccionId).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Transacción eliminada", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> restaurarEstadoBotones("Error al eliminar: " + e.getMessage()));
        }
    }

    private void restaurarEstadoBotones(String mensaje) {
        binding.pbCargaFormulario.setVisibility(View.GONE);
        binding.btnGuardar.setEnabled(true);
        binding.btnEliminar.setEnabled(true);
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void cargarProductoSugeridoDesdeApi(int productoId) {
        ApiService apiService = RetrofitClient.getApiService();

        // Llamada asíncrona con Retrofit
        apiService.obtenerProducto(productoId).enqueue(new retrofit2.Callback<ProductoAPI>() {
            @Override
            public void onResponse(retrofit2.Call<ProductoAPI> call, retrofit2.Response<ProductoAPI> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductoAPI producto = response.body();

                    // Ejemplo: Llenar automáticamente los campos del formulario con los datos de la API
                    String conceptoSugerido = producto.getTitle();
                    double precioSugerido = producto.getPrice();

                    Toast.makeText(FormularioActivity.this,
                            "API Exito: " + conceptoSugerido + " ($" + precioSugerido + ")",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FormularioActivity.this, "Error en la respuesta de la API", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ProductoAPI> call, Throwable t) {
                Toast.makeText(FormularioActivity.this, "Fallo al conectar con API: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}