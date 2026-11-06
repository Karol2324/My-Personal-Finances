package com.example.mypersonalfinances;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EvidenciaActivity extends AppCompatActivity {

    private ImageView ivPreview;
    private MaterialButton btnTomarFoto, btnSubirStorage;
    private ProgressBar progressBar;
    private Uri imagenUri;

    // ActivityResultLauncher para seleccionar la foto (Multimedia/Storage)
    private final ActivityResultLauncher<String> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    imagenUri = uri;
                    ivPreview.setImageURI(uri);
                    btnSubirStorage.setEnabled(true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidencia);

        ivPreview = findViewById(R.id.ivPreview);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnSubirStorage = findViewById(R.id.btnSubirStorage);
        progressBar = findViewById(R.id.progressBar);

        btnTomarFoto.setOnClickListener(v -> seleccionarImagenLauncher.launch("image/*"));
        btnSubirStorage.setOnClickListener(v -> subirFotoAStorage());
    }

    private void subirFotoAStorage() {
        if (imagenUri == null) return;

        progressBar.setVisibility(View.VISIBLE);
        btnSubirStorage.setEnabled(false);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "general";

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child("evidencias/" + uid + "/" + UUID.randomUUID().toString() + ".jpg");

        ref.putFile(imagenUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Foto subida con éxito a Storage", Toast.LENGTH_LONG).show();
                    finish();
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubirStorage.setEnabled(true);
                    Toast.makeText(this, "Error al subir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}