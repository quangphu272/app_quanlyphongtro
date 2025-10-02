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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingListActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookings;
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        setupRecyclerView();
        setupSwipeRefresh();
        loadBookings();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đặt phòng của tôi");
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
        
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "20");
        
        String userId = getCurrentUserId();
        if (userId != null) {
            retrofitClient.getApiService().getUserBookings(
                    retrofitClient.getToken(), 
                    userId, 
                    params
            ).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        List<Booking> bookingList = (List<Booking>) data.get("bookings");
                        
                        if (bookingList != null) {
                            bookings.clear();
                            bookings.addAll(bookingList);
                            bookingAdapter.notifyDataSetChanged();
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
        } else {
            showError("Không thể lấy thông tin người dùng");
            showLoading(false);
        }
    }
    
    private String getCurrentUserId() {
        // Get current user ID from stored user data
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.example.appquanlytimtro.models.User user = gson.fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public void onBookingClick(Booking booking) {
        // Navigate to booking detail
        Toast.makeText(this, "Chi tiết đặt phòng: " + booking.getId(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onBookingAction(Booking booking, String action) {
        // Handle booking actions (cancel, confirm, etc.)
        Toast.makeText(this, "Thực hiện: " + action + " cho booking " + booking.getId(), Toast.LENGTH_SHORT).show();
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
            onBackPressed();
            return true;
        } else if (id == R.id.action_filter) {
            // Show filter options
            Toast.makeText(this, "Chức năng lọc sẽ được phát triển", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
