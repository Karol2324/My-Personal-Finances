package com.example.mypersonalfinances;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    // Vistas
    private TextView tvIrRegistro;
    private Button btnLogin, btnGoogleLogin, btnMicrosoftLogin, btnFacebookLogin;
    private TextInputEditText etLoginCorreo, etLoginPassword;

    // Firebase y Autenticación
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicializar vistas
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnMicrosoftLogin = findViewById(R.id.btnMicrosoftLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        etLoginCorreo = findViewById(R.id.etLoginCorreo);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        // 2. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Si ya hay usuario autenticado, podemos redirigir directo
        /*
        if (mAuth.getCurrentUser() != null) {
            cargarDatosUsuarioNavegar(mAuth.getCurrentUser().getUid());
            return;
        }
        */

        // 3. Evento para ir al registro
        tvIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // 4. Login con correo y contraseña
        btnLogin.setOnClickListener(v -> iniciarSesionCorreo());

        // 5. Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                (ActivityResult result) -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            if (account != null) {
                                autenticarConFirebaseGoogle(account.getIdToken());
                            }
                        } catch (ApiException e) {
                            Toast.makeText(LoginActivity.this, "Error de Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // 6. Configurar Microsoft Login
        btnMicrosoftLogin.setOnClickListener(v -> iniciarSesionMicrosoft());

        // 7. Configurar Facebook Login
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        btnFacebookLogin.setOnClickListener(v -> LoginManager.getInstance().logInWithReadPermissions(
                LoginActivity.this,
                Arrays.asList("email", "public_profile")
        ));

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                autenticarConFirebaseFacebook(loginResult.getAccessToken());
                            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Inicio de sesión cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // --- MÉTODOS DE AUTENTICACIÓN ---

    private void iniciarSesionCorreo() {
        String correo = etLoginCorreo.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Ingrese correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("VERIFICANDO...");

        mAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        registrarUsuarioEnFirestore(mAuth.getCurrentUser());
                    } else {
                        // Reactivar botón
                        btnLogin.setEnabled(true);
                        btnLogin.setText("INICIAR SESION");

                        // Capturar el mensaje exacto de la excepción
                        String mensajeError = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            mensajeError = task.getException().getLocalizedMessage();
                        }

                        Toast.makeText(LoginActivity.this, mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void autenticarConFirebaseGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        registrarUsuarioEnFirestore(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(this, "Fallo al autenticar con Firebase (Google)", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void iniciarSesionMicrosoft() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("microsoft.com");
        provider.addCustomParameter("prompt", "login");

        mAuth.startActivityForSignInWithProvider(this, provider.build())
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() != null) {
                        registrarUsuarioEnFirestore(mAuth.getCurrentUser());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error con Microsoft: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void autenticarConFirebaseFacebook(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        registrarUsuarioEnFirestore(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LoginActivity.this, "Fallo al conectar Facebook con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- MÉTODOS DE BASE DE DATOS Y NAVEGACIÓN ---

    private void registrarUsuarioEnFirestore(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String nombre = firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Usuario";
        String correo = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String rolPredeterminado = "Cliente";

        // Usar la clase Usuario estructurada en el proyecto
        Usuario usuario = new Usuario();
        usuario.setIdDocumento(uid);
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setRol(rolPredeterminado);

        // Guardar o fusionar en la colección "usuarios"
        db.collection("usuarios")
                .document(uid)
                .set(usuario, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoginActivity.this, "¡Bienvenido " + nombre + "!", Toast.LENGTH_SHORT).show();
                    cargarDatosUsuarioNavegar(uid);
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("INICIAR SESION");
                    Toast.makeText(LoginActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void cargarDatosUsuarioNavegar(String uid) {
        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("INICIAR SESION");

                    if (documentSnapshot.exists()) {
                        String nombreReal = documentSnapshot.getString("nombre");
                        String rol = documentSnapshot.getString("rol");

                        SharedPreferences prefs = getSharedPreferences("SesionUsuario", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombre", nombreReal);
                        editor.putString("rol", rol);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "No se encontraron datos de usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("INICIAR SESION");
                    Toast.makeText(LoginActivity.this, "Error de conexión con la base de datos", Toast.LENGTH_SHORT).show();
                });
    }
}