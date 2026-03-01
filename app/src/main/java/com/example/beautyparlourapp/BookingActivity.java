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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceSpinner.setAdapter(adapter);
    }

    private void setupDateAndTimePickers() {
        selectedDateText = findViewById(R.id.tv_selected_date);
        selectedTimeText = findViewById(R.id.tv_selected_time);

        Button pickDate = findViewById(R.id.btn_pick_date);
        Button pickTime = findViewById(R.id.btn_pick_time);

        pickDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    BookingActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        selectedDateText.setText(formattedDate);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });

        pickTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    BookingActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        selectedTimeText.setText(formattedTime);
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });
    }

    private void setupBookingButton() {
        Spinner serviceSpinner = findViewById(R.id.spinner_service);
        Button bookButton = findViewById(R.id.btn_book);

        bookButton.setOnClickListener(v -> {
            String selectedService = String.valueOf(serviceSpinner.getSelectedItem());
            String selectedDate = selectedDateText.getText().toString();
            String selectedTime = selectedTimeText.getText().toString();

            if (selectedDate.equals(getString(R.string.not_selected)) || selectedTime.equals(getString(R.string.not_selected))) {
                Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            String confirmation = "Appointment booked for " + selectedService + " on " + selectedDate + " at " + selectedTime;
            Toast.makeText(this, confirmation, Toast.LENGTH_LONG).show();
        });
    }

    private void attachFooter() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
