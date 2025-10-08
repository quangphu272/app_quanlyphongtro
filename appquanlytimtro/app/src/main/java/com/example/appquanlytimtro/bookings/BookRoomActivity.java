package com.example.appquanlytimtro.bookings;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.payments.PaymentActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookRoomActivity extends AppCompatActivity {

    private String roomId;
    private Room room;
    private User currentUser;
    private RetrofitClient retrofitClient;

    // Views
    private TextView tvRoomTitle, tvRoomPrice, tvRoomAddress;
    private TextInputEditText etCheckInDate, etCheckOutDate, etNotes;
    private AutoCompleteTextView spinnerDuration;
    private TextView tvTotalAmount, tvDeposit, tvMonthlyRent;
    private MaterialButton btnConfirmBooking;
    private ProgressBar progressBar;

    // Booking data
    private Calendar checkInCalendar = Calendar.getInstance();
    private Calendar checkOutCalendar = Calendar.getInstance();
    private int durationMonths = 6;
    private double totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_room);

        retrofitClient = RetrofitClient.getInstance(this);
        loadUserData();

        initViews();
        setupToolbar();
        setupSpinners();
        setupDatePickers();

        // Get room ID from intent
        Intent intent = getIntent();
        if (intent != null) {
            roomId = intent.getStringExtra("room_id");
            if (roomId != null) {
                loadRoomDetails();
            } else {
                showError("Không tìm thấy thông tin phòng trọ");
                finish();
            }
        }
    }

    private void initViews() {
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomPrice = findViewById(R.id.tvRoomPrice);
        tvRoomAddress = findViewById(R.id.tvRoomAddress);
        etCheckInDate = findViewById(R.id.etCheckInDate);
        etCheckOutDate = findViewById(R.id.etCheckOutDate);
        etNotes = findViewById(R.id.etNotes);
        spinnerDuration = findViewById(R.id.spinnerDuration);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDeposit = findViewById(R.id.tvDeposit);
        tvMonthlyRent = findViewById(R.id.tvMonthlyRent);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);
        progressBar = findViewById(R.id.progressBar);

        btnConfirmBooking.setOnClickListener(v -> createBooking());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đặt phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        String[] durations = {"1 tháng", "3 tháng", "6 tháng", "12 tháng", "24 tháng"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, durations);
        spinnerDuration.setAdapter(adapter);
        spinnerDuration.setText("6 tháng", false);

        spinnerDuration.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: durationMonths = 1; break;
                case 1: durationMonths = 3; break;
                case 2: durationMonths = 6; break;
                case 3: durationMonths = 12; break;
                case 4: durationMonths = 24; break;
            }
            updateCheckOutDate();
            calculateTotalAmount();
        });
    }

    private void setupDatePickers() {
        // Set default check-in date to tomorrow
        checkInCalendar.add(Calendar.DAY_OF_MONTH, 1);
        updateCheckInDate();
        updateCheckOutDate();

        etCheckInDate.setOnClickListener(v -> showCheckInDatePicker());
        etCheckOutDate.setOnClickListener(v -> showCheckOutDatePicker());
    }

    private void showCheckInDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkInCalendar.set(year, month, dayOfMonth);
                    updateCheckInDate();
                    updateCheckOutDate();
                    calculateTotalAmount();
                },
                checkInCalendar.get(Calendar.YEAR),
                checkInCalendar.get(Calendar.MONTH),
                checkInCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showCheckOutDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkOutCalendar.set(year, month, dayOfMonth);
                    updateCheckOutDate();
                    calculateDurationFromDates();
                    calculateTotalAmount();
                },
                checkOutCalendar.get(Calendar.YEAR),
                checkOutCalendar.get(Calendar.MONTH),
                checkOutCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to check-in date + 1 day
        Calendar minDate = (Calendar) checkInCalendar.clone();
        minDate.add(Calendar.DAY_OF_MONTH, 1);
        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.show();
    }

    private void updateCheckInDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etCheckInDate.setText(sdf.format(checkInCalendar.getTime()));
    }

    private void updateCheckOutDate() {
        checkOutCalendar.setTime(checkInCalendar.getTime());
        checkOutCalendar.add(Calendar.MONTH, durationMonths);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etCheckOutDate.setText(sdf.format(checkOutCalendar.getTime()));
    }

    private void calculateDurationFromDates() {
        long diffInMillis = checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis();
        long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
        durationMonths = (int) Math.ceil(diffInDays / 30.0);
        
        // Update spinner
        String durationText = durationMonths + " tháng";
        spinnerDuration.setText(durationText, false);
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    private void loadRoomDetails() {
        showLoading(true);

        retrofitClient.getApiService().getRoom(roomId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        if (data.containsKey("room")) {
                            // Parse room from data.room
                            Gson gson = new Gson();
                            String roomJson = gson.toJson(data.get("room"));
                            room = gson.fromJson(roomJson, Room.class);
                            displayRoomInfo();
                            calculateTotalAmount();
                        } else {
                            showError("Không tìm thấy thông tin phòng");
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải thông tin phòng trọ");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayRoomInfo() {
        if (room == null) return;

        tvRoomTitle.setText(room.getTitle());

        if (room.getAddress() != null) {
            String address = "";
            if (room.getAddress().getStreet() != null) address += room.getAddress().getStreet() + ", ";
            if (room.getAddress().getWard() != null) address += room.getAddress().getWard() + ", ";
            if (room.getAddress().getDistrict() != null) address += room.getAddress().getDistrict() + ", ";
            if (room.getAddress().getCity() != null) address += room.getAddress().getCity();
            
            if (address.endsWith(", ")) {
                address = address.substring(0, address.length() - 2);
            }
            tvRoomAddress.setText(address);
        }

        if (room.getPrice() != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String price = formatter.format(room.getPrice().getMonthly()) + " VNĐ/tháng";
            tvRoomPrice.setText(price);
        }
    }

    private void calculateTotalAmount() {
        if (room == null || room.getPrice() == null) return;

        double monthlyRent = room.getPrice().getMonthly();
        double deposit = room.getPrice().getDeposit();
        
        totalAmount = (monthlyRent * durationMonths) + deposit;

        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        
        tvMonthlyRent.setText("Tiền thuê: " + formatter.format(monthlyRent * durationMonths) + " VNĐ");
        tvDeposit.setText("Tiền cọc: " + formatter.format(deposit) + " VNĐ");
        tvTotalAmount.setText("Tổng cộng: " + formatter.format(totalAmount) + " VNĐ");
    }

    private void createBooking() {
        if (!validateInput()) return;

        showLoading(true);

        // Create booking object
        Booking booking = new Booking();
        
        // Set booking details
        Booking.BookingDetails bookingDetails = new Booking.BookingDetails();
        bookingDetails.setCheckInDate(checkInCalendar.getTime());
        bookingDetails.setCheckOutDate(checkOutCalendar.getTime());
        bookingDetails.setDuration(durationMonths);
        booking.setBookingDetails(bookingDetails);

        // Set pricing
        Booking.Pricing pricing = new Booking.Pricing();
        pricing.setTotalAmount(totalAmount);
        pricing.setMonthlyRent(room.getPrice().getMonthly());
        pricing.setDeposit(room.getPrice().getDeposit());
        booking.setPricing(pricing);

        // Set notes
        Booking.Notes notes = new Booking.Notes();
        String noteText = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        notes.setTenant(noteText);
        booking.setNotes(notes);

        String token = "Bearer " + retrofitClient.getToken();
        
        // Create a map for the request body with proper structure
        java.util.Map<String, Object> bookingRequest = new java.util.HashMap<>();
        bookingRequest.put("roomId", roomId);
        
        // Booking details
        java.util.Map<String, Object> bookingDetailsMap = new java.util.HashMap<>();
        bookingDetailsMap.put("checkInDate", bookingDetails.getCheckInDate());
        bookingDetailsMap.put("checkOutDate", bookingDetails.getCheckOutDate());
        bookingDetailsMap.put("duration", durationMonths);
        bookingDetailsMap.put("numberOfOccupants", 1); // Set default value
        bookingRequest.put("bookingDetails", bookingDetailsMap);
        
        // Pricing
        java.util.Map<String, Object> pricingMap = new java.util.HashMap<>();
        pricingMap.put("deposit", room.getPrice().getDeposit());
        pricingMap.put("monthlyRent", room.getPrice().getMonthly());
        pricingMap.put("totalAmount", totalAmount);
        // Convert utilities to number
        double utilitiesAmount = 0;
        Room.Utilities utilities = room.getPrice().getUtilities();
        if (utilities != null) {
            utilitiesAmount = utilities.getElectricity() + utilities.getWater() + 
                             utilities.getInternet() + utilities.getOther();
        }
        pricingMap.put("utilities", utilitiesAmount);
        bookingRequest.put("pricing", pricingMap);
        
        // Notes
        java.util.Map<String, Object> notesMap = new java.util.HashMap<>();
        notesMap.put("tenant", noteText);
        bookingRequest.put("notes", notesMap);

        retrofitClient.getApiService().createBooking(token, bookingRequest).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Booking createdBooking = apiResponse.getData();
                        Toast.makeText(BookRoomActivity.this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
                        
                        // Navigate to payment
                        Intent intent = new Intent(BookRoomActivity.this, PaymentActivity.class);
                        intent.putExtra("booking_id", createdBooking.getId());
                        intent.putExtra("amount", totalAmount);
                        startActivity(intent);
                        finish();
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tạo đặt phòng");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private boolean validateInput() {
        if (currentUser == null) {
            showError("Vui lòng đăng nhập để đặt phòng");
            return false;
        }

        if (room == null) {
            showError("Không tìm thấy thông tin phòng trọ");
            return false;
        }

        if (checkInCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
            showError("Ngày nhận phòng phải sau ngày hôm nay");
            return false;
        }

        if (checkOutCalendar.getTimeInMillis() <= checkInCalendar.getTimeInMillis()) {
            showError("Ngày trả phòng phải sau ngày nhận phòng");
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnConfirmBooking.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

