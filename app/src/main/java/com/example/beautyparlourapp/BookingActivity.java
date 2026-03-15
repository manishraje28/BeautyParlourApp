package com.example.beautyparlourapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    private final Calendar calendar = Calendar.getInstance();
    private TextView selectedDateText;
    private TextView selectedTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        setupServiceSpinner();
        setupDateAndTimePickers();
        setupBookingButton();
        attachFooter();
    }

    private void setupServiceSpinner() {
        Spinner serviceSpinner = findViewById(R.id.spinner_service);
        String[] services = {"Haircut", "Facial", "Bridal Makeup", "Hair Spa", "Waxing"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, services);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);

        // Pre-select service passed via gesture shortcut (Double Tap / Long Press)
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
        Spinner  serviceSpinner = findViewById(R.id.spinner_service);
        Button   bookButton     = findViewById(R.id.btn_book);

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

            // Save to Firestore if user is logged in
            if (FirebaseManager.getInstance().isLoggedIn()) {
                FirebaseManager.getInstance().saveBooking(service, date, time,
                        new FirebaseManager.BookingCallback() {
                            @Override
                            public void onSuccess() {
                                bookButton.setEnabled(true);
                                Toast.makeText(BookingActivity.this,
                                        "Booking confirmed for " + service
                                                + " on " + date + " at " + time,
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(String error) {
                                bookButton.setEnabled(true);
                                Toast.makeText(BookingActivity.this,
                                        "Booking saved locally. " + error,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // Not logged in — show local confirmation only
                bookButton.setEnabled(true);
                Toast.makeText(this,
                        "Appointment booked for " + service
                                + " on " + date + " at " + time,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attachFooter() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
