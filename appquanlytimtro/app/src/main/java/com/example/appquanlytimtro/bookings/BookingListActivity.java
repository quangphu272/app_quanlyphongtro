package com.example.appquanlytimtro.bookings;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.BookingAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.payments.PaymentActivity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingListActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private RetrofitClient retrofitClient;
    private List<Booking> bookings;
    private BookingAdapter bookingAdapter;
    
    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);

        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        
        loadBookings();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách đặt phòng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        bookings = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bookingAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void loadBookings() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        java.util.Map<String, String> params = new java.util.HashMap<>();
        retrofitClient.getApiService().getBookings(token, params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        List<?> bookingsData = (List<?>) data.get("bookings");
                        
                        android.util.Log.d("BookingListActivity", "API Response - bookings count: " + (bookingsData != null ? bookingsData.size() : 0));
                        
                        if (bookingsData != null) {
                            bookings.clear();
                            Gson gson = new Gson();
                            for (Object bookingObj : bookingsData) {
                                if (bookingObj instanceof Map) {
                                    try {
                                        Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                                        bookings.add(booking);
                                        android.util.Log.d("BookingListActivity", "Parsed booking: " + booking.getId() + " - Status: " + booking.getStatus());
                                    } catch (Exception e) {
                                        android.util.Log.e("BookingListActivity", "Error parsing booking: " + e.getMessage());
                                        android.util.Log.e("BookingListActivity", "Booking data: " + gson.toJson(bookingObj));
                                    }
                                }
                            }
                            android.util.Log.d("BookingListActivity", "Total bookings after parsing: " + bookings.size());
                            bookingAdapter.updateBookings(bookings);
                            updateEmptyView();
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải danh sách đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Lỗi kết nối. Vui lòng thử lại.");
            }
        });
    }

    private void updateEmptyView() {
        if (bookings.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Navigate to booking detail
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onBookingStatusChange(Booking booking, String newStatus) {
        // Handle status change
        updateBookingStatus(booking.getId(), newStatus);
    }

    @Override
    public void onPaymentClick(Booking booking) {
        // Navigate to payment
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.putExtra("room_id", booking.getRoom().getId());
        startActivity(intent);
    }

    private void updateBookingStatus(String bookingId, String newStatus) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().updateBooking(token, bookingId, new Booking()).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(BookingListActivity.this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                        loadBookings(); // Reload list
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật trạng thái");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.booking_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.action_refresh) {
            loadBookings();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        String[] statusOptions = {"Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đã thanh toán", "Đang hoạt động", "Đã hoàn thành", "Đã hủy"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Lọc theo trạng thái")
                .setItems(statusOptions, (dialog, which) -> {
                    String selectedStatus = statusOptions[which];
                    filterBookingsByStatus(selectedStatus);
                })
                .show();
    }

    private void filterBookingsByStatus(String status) {
        if ("Tất cả".equals(status)) {
            bookingAdapter.filterByStatus(null);
        } else {
            String statusValue = convertStatusToValue(status);
            bookingAdapter.filterByStatus(statusValue);
        }
    }

    private String convertStatusToValue(String displayStatus) {
        switch (displayStatus) {
            case "Chờ xác nhận": return "pending";
            case "Đã xác nhận": return "confirmed";
            case "Đã thanh toán": return "deposit_paid";
            case "Đang hoạt động": return "active";
            case "Đã hoàn thành": return "completed";
            case "Đã hủy": return "cancelled";
            default: return null;
        }
    }
}