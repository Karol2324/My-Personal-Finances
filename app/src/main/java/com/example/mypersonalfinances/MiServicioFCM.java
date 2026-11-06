package com.example.mypersonalfinances;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiServicioFCM extends FirebaseMessagingService {

    private static final String CANAL_ID = "canal_finanzas";
    private static final String CANAL_NOMBRE = "Notificaciones Finanzas";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "El token de este dispositivo es: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCM_MESSAGE", "Mensaje recibido desde: " + remoteMessage.getFrom());

        String titulo = "MyPersonalFinances";
        String cuerpo = "Nueva actualización disponible";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                titulo = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                cuerpo = remoteMessage.getNotification().getBody();
            }
            Log.d("FCM_MESSAGE", "Titulo: " + titulo + " | Contenido: " + cuerpo);
        }

        mostrarNotificacion(titulo, cuerpo);
    }

    private void mostrarNotificacion(String titulo, String cuerpo) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear el canal de notificación para Android 8.0+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    CANAL_NOMBRE,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Canal de alertas de transacciones y novedades");
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }

        // PendingIntent para reabrir MainActivity al tocar la notificación
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
