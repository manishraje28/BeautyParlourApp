package com.example.beautyparlourapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private final Calendar calendar = Calendar.getInstance();
    private TextView selectedDateText;
    private TextView selectedTimeText;

    // Launcher for POST_NOTIFICATIONS
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        setupServiceSpinner();
        setupDateAndTimePickers();
        setupBookingButton();
        
        attachFooter();
        
        // Make the back button in the header functional
        android.view.View backButton = findViewById(R.id.ll_top_bar);
        if (backButton != null) {
            backButton.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupServiceSpinner() {
        Spinner serviceSpinner = findViewById(R.id.spinner_service);
        String[] services = {"Haircut", "Facial", "Bridal Makeup", "Hair Spa", "Waxing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, services);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);

        String preSelected = getIntent().getStringExtra("selected_service");
        if (preSelected != null) {
            for (int i = 0; i < services.length; i++) {
                if (services[i].equals(preSelected)) {
                    serviceSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private void setupDateAndTimePickers() {
        selectedDateText = findViewById(R.id.tv_selected_date);
        selectedTimeText = findViewById(R.id.tv_selected_time);

        Button pickDate = findViewById(R.id.btn_pick_date);
        Button pickTime = findViewById(R.id.btn_pick_time);

        pickDate.setOnClickListener(v -> {
            int year  = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day   = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(this, (view, y, m, d) -> {
                calendar.set(y, m, d);
                selectedDateText.setText(
                        String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y));
            }, year, month, day).show();
        });

        pickTime.setOnClickListener(v -> {
            int hour   = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(this, (view, h, min) -> {
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, min);
                selectedTimeText.setText(
                        String.format(Locale.getDefault(), "%02d:%02d", h, min));
            }, hour, minute, false).show();
        });
    }

    private void setupBookingButton() {
        Spinner serviceSpinner = findViewById(R.id.spinner_service);
        Button bookButton = findViewById(R.id.btn_book);

        bookButton.setOnClickListener(v -> {
            String service = String.valueOf(serviceSpinner.getSelectedItem());
            String date    = selectedDateText.getText().toString();
            String time    = selectedTimeText.getText().toString();

            if (date.equals(getString(R.string.not_selected))
                    || time.equals(getString(R.string.not_selected))) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            bookButton.setEnabled(false);

            // TODO: Retrieve specific price per service, providing fallback price for now
            double estimatedPrice = 65.0; 

            // Create booking directly in Firestore
            FirebaseManager.getInstance().createBooking(service, date, time, estimatedPrice,
                    new FirebaseManager.BookingCallback() {
                        @Override
                        public void onSuccess() {
                            bookButton.setEnabled(true);
                            Toast.makeText(BookingActivity.this,
                                    "✓ Booking submitted for " + service + " (Pending Admin Approval)",
                                    Toast.LENGTH_LONG).show();
                            
                            // Note: Local AlarmManager reminder has been replaced by Real FCM Push Notifications 
                            // sent remotely from the Node.js backend whenever the Admin explicitly approves it.
                        }

                        @Override
                        public void onFailure(String error) {
                            bookButton.setEnabled(true);
                            Toast.makeText(BookingActivity.this,
                                    "Booking failed: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void scheduleReminder(String service, String timeStr) {
        // Clone calendar which holds the user-picked Date & Time exactly!
        Calendar reminderTime = (Calendar) calendar.clone();

        // Let's subtract 2 hours from the appointment time
        reminderTime.add(Calendar.HOUR_OF_DAY, -2);

        // Make sure the reminder isn't in the past
        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(this, AppointmentReminderReceiver.class);
        intent.putExtra("service_name", service);
        intent.putExtra("appointment_time", timeStr);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(), // unique ID for every booking 
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Depending on OEM, USE_EXACT_ALARM takes care of API 33+ exact alarm constraints
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void attachFooter() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
