package com.example.mypersonalfinances;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificacionHelper {

    private static final String CANAL_ID = "canal_transacciones";
    private static final String CANAL_NOMBRE = "Notificaciones de Transacciones";

    public static void emitirNotificacion(Context context, String titulo, String mensaje) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    CANAL_NOMBRE,
                    NotificationManager.IMPORTANCE_HIGH
            );
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(android.R.drawable.ic_input_add)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}