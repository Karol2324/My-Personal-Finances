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
        String concepto = binding.etConcepto.getText().toString().trim();
        double monto = Double.parseDouble(binding.etMonto.getText().toString().trim().replace(",", "."));
        String tipo = binding.rbIngreso.isChecked() ? "Ingreso" : "Gasto";


        binding.btnGuardar.setEnabled(false);
        binding.btnEliminar.setEnabled(false);
        binding.pbCargaFormulario.setVisibility(View.VISIBLE);

        Map<String, Object> transaccionMap = new HashMap<>();
        transaccionMap.put("concepto", concepto);
        transaccionMap.put("monto", monto);
        transaccionMap.put("tipo", tipo);

        if (transaccionId == null) {

            db.collection("transacciones").add(transaccionMap)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, R.string.transaccion_guardada, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> restaurarEstadoBotones("Error al guardar: " + e.getMessage()));
        } else {

            db.collection("transacciones").document(transaccionId).update(transaccionMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, R.string.transaccion_actualizada, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> restaurarEstadoBotones("Error al actualizar: " + e.getMessage()));
        }
    }

    private void eliminarDeFirestore() {
        if (transaccionId != null) {
            binding.btnGuardar.setEnabled(false);
            binding.btnEliminar.setEnabled(false);
            binding.pbCargaFormulario.setVisibility(View.VISIBLE);


            db.collection("transacciones").document(transaccionId).delete()
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
}