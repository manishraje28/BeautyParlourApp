package com.example.beautyparlourapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AppointmentReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "appointment_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String serviceName = intent.getStringExtra("service_name");
        String appointmentTime = intent.getStringExtra("appointment_time");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminders for upcoming beauty parlour appointments.");
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Tapping the notification takes them to the Home Activity
        Intent homeIntent = new Intent(context, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                homeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar_decor)
                .setContentTitle("Your Appointment at Bharati's Beauty Parlour 💅")
                .setContentText("Hey! Don't forget your " + serviceName + " appointment today at " + appointmentTime + "!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
