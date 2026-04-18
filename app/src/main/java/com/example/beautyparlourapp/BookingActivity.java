package com.example.beautyparlourapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.beautyparlourapp.adapter.DateAdapter;
import com.example.beautyparlourapp.adapter.BookingServiceAdapter;
import com.example.beautyparlourapp.adapter.TimeSlotAdapter;
import com.example.beautyparlourapp.models.DateItem;
import com.example.beautyparlourapp.network.AccessTokenHelper;
import com.example.beautyparlourapp.network.FcmRetrofitClient;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private final Calendar calendar = Calendar.getInstance();

    private RecyclerView rvDates;
    private RecyclerView rvServices;
    private RecyclerView rvTimeSlots;
    private TextView tvMonthYear;
    private Button btnBook;

    private DateAdapter dateAdapter;
    private BookingServiceAdapter serviceAdapter;
    private TimeSlotAdapter timeSlotAdapter;

    private DateItem selectedDate = null;
    private Map<String, Object> selectedService = null;
    private String selectedTime = null;

    private List<String> allTimeSlots;

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

        initViews();
        setupDates();
        setupServices();
        setupTimeSlots();
        setupBookingButton();
        
        attachFooter();

        findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());
    }

    private void initViews() {
        rvDates = findViewById(R.id.rv_dates);
        rvServices = findViewById(R.id.rv_services);
        rvTimeSlots = findViewById(R.id.rv_time_slots);
        tvMonthYear = findViewById(R.id.tv_month_year);
        btnBook = findViewById(R.id.btn_book);
    }

    private void setupDates() {
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<DateItem> dateList = new ArrayList<>();
        
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat fullFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

        tvMonthYear.setText(monthYearFormat.format(calendar.getTime()));

        Calendar tempCal = (Calendar) calendar.clone();
        for (int i = 0; i < 14; i++) {
            String dayName = dayFormat.format(tempCal.getTime()).substring(0, 2); // Mo, Tu, We
            String dayNumber = dateFormat.format(tempCal.getTime());
            String fullDate = fullFormat.format(tempCal.getTime());
            dateList.add(new DateItem(dayName, dayNumber, fullDate));
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        dateAdapter = new DateAdapter(dateList, dateItem -> {
            selectedDate = dateItem;
            if (timeSlotAdapter != null) {
                fetchBookedTimesForDate(dateItem.getFullDateString());
            }
        });
        rvDates.setAdapter(dateAdapter);
        // We will call selectInitial() AFTER timeSlots setup
    }

    private void setupServices() {
        rvServices.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        serviceAdapter = new BookingServiceAdapter(new ArrayList<>(), service -> {
            selectedService = service;
        });
        rvServices.setAdapter(serviceAdapter);

        FirebaseManager.getInstance().fetchServices(new FirebaseManager.ServicesCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> services) {
                if (!services.isEmpty()) {
                    serviceAdapter.updateData(services);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookingActivity.this, "Failed to load services", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTimeSlots() {
        allTimeSlots = new ArrayList<>();
        allTimeSlots.add("10:00 AM");
        allTimeSlots.add("11:00 AM");
        allTimeSlots.add("12:00 PM");
        allTimeSlots.add("01:00 PM");
        allTimeSlots.add("02:00 PM");
        allTimeSlots.add("03:00 PM");
        allTimeSlots.add("04:00 PM");
        allTimeSlots.add("05:00 PM");
        allTimeSlots.add("06:00 PM");
        allTimeSlots.add("07:00 PM");
        allTimeSlots.add("08:00 PM");

        timeSlotAdapter = new TimeSlotAdapter(allTimeSlots, new ArrayList<>(), time -> {
            selectedTime = time;
        });
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    private void fetchBookedTimesForDate(String date) {
        // Reset selected time
        selectedTime = null;
        timeSlotAdapter.updateBookedTimes(new ArrayList<>());

        FirebaseManager.getInstance().fetchBookedTimeSlots(date, new FirebaseManager.BookedTimeSlotsCallback() {
            @Override
            public void onSuccess(List<String> bookedTimes) {
                timeSlotAdapter.updateBookedTimes(bookedTimes);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(BookingActivity.this, "Failed to load available times for date", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBookingButton() {
        btnBook.setOnClickListener(v -> {
            selectedTime = timeSlotAdapter.getSelectedTime();

            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedService == null) {
                Toast.makeText(this, "Please select a service", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedTime == null) {
                Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
                return;
            }

            btnBook.setEnabled(false);

            String rawServiceName = (String) selectedService.get("name");
            final String serviceName = rawServiceName == null ? "Unknown Service" : rawServiceName;

            double estimatedPrice = 0.0;
            Object priceObj = selectedService.get("price");
            if (priceObj != null) {
                try {
                    estimatedPrice = Double.parseDouble(priceObj.toString());
                } catch (NumberFormatException ignored) {}
            }

            String date = selectedDate.getFullDateString();

            FirebaseManager.getInstance().createBooking(serviceName, date, selectedTime, estimatedPrice,
                    new FirebaseManager.BookingCallback() {
                        @Override
                        public void onSuccess() {
                            btnBook.setEnabled(true);
                            Toast.makeText(BookingActivity.this,
                                    "✓ Booking submitted for " + serviceName + "!",
                                    Toast.LENGTH_LONG).show();
                            
                            // Send Push Notification to all Admins
                            sendPushNotificationToAdmins(
                                    "New Booking Request",
                                    "A user has requested a " + serviceName + " appointment for " + date + " at " + selectedTime + ".");

                            // Schedule Reminder Alarm
                            try {
                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                Calendar alarmCal = (Calendar) calendar.clone();
                                alarmCal.setTime(timeFormat.parse(selectedTime));
                                
                                Calendar bookingCal = Calendar.getInstance();
                                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                bookingCal.setTime(df.parse(date));
                                bookingCal.set(Calendar.HOUR_OF_DAY, alarmCal.get(Calendar.HOUR_OF_DAY));
                                bookingCal.set(Calendar.MINUTE, alarmCal.get(Calendar.MINUTE));
                                
                                scheduleReminder(serviceName, selectedTime, bookingCal);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            finish(); // Go back to Home or previous screen
                        }

                        @Override
                        public void onFailure(String error) {
                            btnBook.setEnabled(true);
                            Toast.makeText(BookingActivity.this,
                                    "Booking failed: " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void scheduleReminder(String service, String timeStr, Calendar bookingCal) {
        Calendar reminderTime = (Calendar) bookingCal.clone();
        reminderTime.add(Calendar.HOUR_OF_DAY, -2);
        
        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(this, AppointmentReminderReceiver.class);
        intent.putExtra("service_name", service);
        intent.putExtra("appointment_time", timeStr);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void sendPushNotificationToAdmins(String title, String body) {
        new Thread(() -> {
            String accessToken = AccessTokenHelper.getAccessToken(BookingActivity.this);
            if (accessToken == null) return;

            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("topic", "admin_notifications");
            
            JsonObject notificationObj = new JsonObject();
            notificationObj.addProperty("title", title);
            notificationObj.addProperty("body", body);
            
            messageObj.add("notification", notificationObj);

            JsonObject rootPayload = new JsonObject();
            rootPayload.add("message", messageObj);

            String bearerValue = "Bearer " + accessToken;
            FcmRetrofitClient.getInstance().getApi().sendNotification(bearerValue, rootPayload)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                        }
                    });
        }).start();
    }

    private void attachFooter() {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.footer_container, new FooterFragment())
                .commit();
    }
}
