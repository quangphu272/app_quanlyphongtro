package com.example.appquanlytimtro.payments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.config.VNPayConfig;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvRoomTitle, tvRoomAddress, tvCheckIn, tvCheckOut, tvTotalAmount, tvDepositAmount;
    private MaterialButton btnPayNow, btnPayLater;
    private ProgressBar progressBar;
    private MaterialCardView cardPaymentInfo;
    
    private Room room;
    private Booking booking;
    private User currentUser;
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();
        setupToolbar();
        loadData();
        setupPaymentButtons();
    }

    private void initViews() {
        tvRoomTitle = findViewById(R.id.tvRoomTitle);
        tvRoomAddress = findViewById(R.id.tvRoomAddress);
        tvCheckIn = findViewById(R.id.tvCheckIn);
        tvCheckOut = findViewById(R.id.tvCheckOut);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvDepositAmount = findViewById(R.id.tvDepositAmount);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnPayLater = findViewById(R.id.btnPayLater);
        progressBar = findViewById(R.id.progressBar);
        cardPaymentInfo = findViewById(R.id.cardPaymentInfo);
        
        retrofitClient = RetrofitClient.getInstance(this);
        // currentUser will be loaded from API
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadData() {
        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            String roomId = intent.getStringExtra("room_id");
            String bookingId = intent.getStringExtra("booking_id");
            
            if (roomId != null) {
                loadRoomDetails(roomId);
            }
            
            if (bookingId != null) {
                loadBookingDetails(bookingId);
            }
        }
    }
    
    private void loadRoomDetails(String roomId) {
        showLoading(true);

        retrofitClient.getApiService().getRoom(roomId).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
                    @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        java.util.Map<String, Object> data = apiResponse.getData();
                        if (data.containsKey("room")) {
                            try {
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                String roomJson = gson.toJson(data.get("room"));
                                room = gson.fromJson(roomJson, Room.class);
                                
                                displayRoomInfo();
                                
                            } catch (Exception e) {
                                android.util.Log.e("PaymentActivity", "Error parsing room: " + e.getMessage(), e);
                                Toast.makeText(PaymentActivity.this, "Lỗi xử lý dữ liệu phòng", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                showLoading(false);
                    }

                    @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi tải thông tin phòng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadBookingDetails(String bookingId) {
        // Load booking details if needed
        // This would be implemented based on your booking API
    }
    
    private void displayRoomInfo() {
        if (room != null) {
            tvRoomTitle.setText(room.getTitle());
            
            // Set address
            if (room.getAddress() != null) {
                String address = "";
                if (room.getAddress().getStreet() != null && !room.getAddress().getStreet().isEmpty()) {
                    address += room.getAddress().getStreet() + ", ";
                }
                if (room.getAddress().getWard() != null && !room.getAddress().getWard().isEmpty()) {
                    address += room.getAddress().getWard() + ", ";
                }
                if (room.getAddress().getDistrict() != null && !room.getAddress().getDistrict().isEmpty()) {
                    address += room.getAddress().getDistrict() + ", ";
                }
                if (room.getAddress().getCity() != null && !room.getAddress().getCity().isEmpty()) {
                    address += room.getAddress().getCity();
                }
                // Remove trailing comma and space
                if (address.endsWith(", ")) {
                    address = address.substring(0, address.length() - 2);
                }
                tvRoomAddress.setText(address);
            }
            
            // Set amounts
            if (room.getPrice() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                
                // Total amount (monthly rent)
                double totalAmount = room.getPrice().getMonthly();
                tvTotalAmount.setText(formatter.format((long)totalAmount) + " VNĐ");
                
                // Deposit amount
                double depositAmount = room.getPrice().getDeposit();
                tvDepositAmount.setText(formatter.format((long)depositAmount) + " VNĐ");
            }
            
            // Set check-in/check-out dates (from booking or default)
            tvCheckIn.setText("Ngay nhận phòng");
            tvCheckOut.setText("Ngay trả phòng");
        }
    }
    
    private void setupPaymentButtons() {
        btnPayNow.setOnClickListener(v -> processPayment());
        btnPayLater.setOnClickListener(v -> {
            // Save booking without payment
            Toast.makeText(this, "Đã lưu thông tin đặt phòng. Vui lòng thanh toán sau.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    private void loadCurrentUser() {
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().getCurrentUser(token).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<User>>() {
                    @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<User>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<User>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<User> apiResponse = response.body();
                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        currentUser = apiResponse.getData();
                        // Now proceed with payment
                        proceedWithPayment();
                            } else {
                        Toast.makeText(PaymentActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                    Toast.makeText(PaymentActivity.this, "Lỗi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<User>> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithPayment() {
        // Create payment request
        String orderId = "ORDER_" + System.currentTimeMillis();
        String orderInfo = "Thanh toan phong tro: " + room.getTitle();
        long amount = (long)room.getPrice().getDeposit(); // Pay deposit first
        
        VNPayService.PaymentRequest paymentRequest = new VNPayService.PaymentRequest(
            orderId,
            orderInfo,
            amount,
            "127.0.0.1" // In real app, get actual IP address
        );
        
        // Open VNPay payment
        VNPayService.openPayment(this, paymentRequest);
    }
    
    private void processPayment() {
        if (room == null) {
            Toast.makeText(this, "Không có thông tin phòng để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Kiểm tra cấu hình VNPay
        if (!VNPayConfig.isConfigured()) {
            Toast.makeText(this, "VNPay chưa được cấu hình. Vui lòng liên hệ admin.", Toast.LENGTH_LONG).show();
            android.util.Log.w("PaymentActivity", VNPayConfig.getConfigurationMessage());
            return;
        }
        
        // Load current user first
        loadCurrentUser();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        // Handle VNPay return URL
        Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals("com.example.appquanlytimtro") && uri.getHost().equals("vnpay")) {
            handlePaymentResult(uri);
        }
    }
    
    private void handlePaymentResult(Uri uri) {
        VNPayService.PaymentResponse response = VNPayService.handlePaymentResult(uri);
        
        if (response.isSuccess()) {
            // Payment successful
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            
            // Create booking record
            createBookingRecord(response);
            
        } else {
            // Payment failed
            Toast.makeText(this, "Thanh toán thất bại: " + response.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void createBookingRecord(VNPayService.PaymentResponse paymentResponse) {
        showLoading(true);
        
        // Create booking object
        Booking booking = new Booking();
        
        // Set room
        booking.setRoom(room);
        
        // Set tenant
        booking.setTenant(currentUser);
        
        // Set landlord
        booking.setLandlord(room.getLandlord());
        
        // Set booking details
        Booking.BookingDetails bookingDetails = new Booking.BookingDetails();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            bookingDetails.setCheckInDate(sdf.parse("2025-01-01"));
            bookingDetails.setCheckOutDate(sdf.parse("2025-02-01"));
        } catch (java.text.ParseException e) {
            // If parsing fails, set to current date
            bookingDetails.setCheckInDate(new java.util.Date());
            bookingDetails.setCheckOutDate(new java.util.Date());
        }
        booking.setBookingDetails(bookingDetails);
        
        // Set pricing
        Booking.Pricing pricing = new Booking.Pricing();
        pricing.setTotalAmount(room.getPrice().getMonthly());
        pricing.setDeposit(room.getPrice().getDeposit());
        booking.setPricing(pricing);
        
        // Set status
        booking.setStatus("confirmed");
        // PaymentStatus is a class, not enum, so we'll set it later if needed
        
        // Send to backend
        String token = "Bearer " + retrofitClient.getToken();
        
        // Create a map for the request body with proper structure
        java.util.Map<String, Object> bookingRequest = new java.util.HashMap<>();
        bookingRequest.put("roomId", room.getId());
        
        // Booking details
        java.util.Map<String, Object> bookingDetailsMap = new java.util.HashMap<>();
        bookingDetailsMap.put("checkInDate", "2025-01-01");
        bookingDetailsMap.put("checkOutDate", "2025-02-01");
        bookingDetailsMap.put("duration", 1);
        bookingDetailsMap.put("numberOfOccupants", 1);
        bookingRequest.put("bookingDetails", bookingDetailsMap);
        
        // Pricing
        java.util.Map<String, Object> pricingMap = new java.util.HashMap<>();
        pricingMap.put("deposit", room.getPrice().getDeposit());
        pricingMap.put("monthlyRent", room.getPrice().getMonthly());
        pricingMap.put("totalAmount", room.getPrice().getMonthly());
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
        notesMap.put("tenant", "Thanh toán qua VNPay");
        bookingRequest.put("notes", notesMap);
        
        retrofitClient.getApiService().createBooking(token, bookingRequest).enqueue(new retrofit2.Callback<com.example.appquanlytimtro.models.ApiResponse<Booking>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<Booking>> call, retrofit2.Response<com.example.appquanlytimtro.models.ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    com.example.appquanlytimtro.models.ApiResponse<Booking> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(PaymentActivity.this, "Đặt phòng thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Lỗi tạo đặt phòng: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Lỗi tạo đặt phòng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.example.appquanlytimtro.models.ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(PaymentActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cardPaymentInfo.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}