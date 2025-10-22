package com.example.appquanlytimtro.payments;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.PaymentAdapter;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvTotalPaid, tvPendingAmount;
    private LinearLayout emptyState;
    
    private PaymentAdapter paymentAdapter;
    private List<Payment> payments = new ArrayList<>();
    private RetrofitClient retrofitClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        loadPayments();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);
        emptyState = findViewById(R.id.emptyState);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        // Get current user role
        String userJson = retrofitClient.getUserData();
        String userRole = "tenant"; // default
        if (userJson != null) {
            try {
                com.example.appquanlytimtro.models.User currentUser = new com.google.gson.Gson().fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                if (currentUser != null && currentUser.getRole() != null) {
                    userRole = currentUser.getRole();
                }
            } catch (Exception e) {
                android.util.Log.e("PaymentListActivity", "Error parsing user role: " + e.getMessage());
            }
        }
        
        paymentAdapter = new PaymentAdapter(payments, new PaymentAdapter.OnPaymentClickListener() {
            @Override
            public void onPaymentClick(Payment payment) {
                PaymentListActivity.this.onPaymentClick(payment);
            }
            
            @Override
            public void onPaymentAction(Payment payment, String action) {
                // Handle payment actions if needed
                Toast.makeText(PaymentListActivity.this, "Action: " + action + " for payment: " + payment.getId(), Toast.LENGTH_SHORT).show();
            }
        }, userRole);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(paymentAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPayments);
    }
    
    private void loadPayments() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        
        // Get current user ID
        String userJson = retrofitClient.getUserData();
        if (userJson == null) {
            showLoading(false);
            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.example.appquanlytimtro.models.User currentUser = new com.google.gson.Gson().fromJson(userJson, com.example.appquanlytimtro.models.User.class);
        if (currentUser == null || currentUser.getId() == null) {
            showLoading(false);
            Toast.makeText(this, "Thông tin người dùng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use the general payments endpoint instead of user-specific endpoint
        retrofitClient.getApiService().getPayments(token, new java.util.HashMap<>()).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                android.util.Log.d("PaymentListActivity", "Payments API Response Code: " + response.code());
                android.util.Log.d("PaymentListActivity", "Payments API Response Body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    android.util.Log.d("PaymentListActivity", "Payments Data: " + data);
                    
                    if (data != null && data.containsKey("payments")) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String paymentsJson = gson.toJson(data.get("payments"));
                            List<Payment> paymentList = gson.fromJson(paymentsJson, new com.google.gson.reflect.TypeToken<List<Payment>>(){}.getType());
                            
                            android.util.Log.d("PaymentListActivity", "Parsed payments count: " + (paymentList != null ? paymentList.size() : 0));
                            
                            if (paymentList != null) {
                                // Filter payments - chỉ hiển thị payments từ booking đã confirmed
                                List<Payment> filteredPayments = filterValidPayments(paymentList);
                                
                                payments.clear();
                                payments.addAll(filteredPayments);
                                paymentAdapter.notifyDataSetChanged();
                                
                                // Calculate summary
                                calculateSummary(filteredPayments);
                                
                                // Show/hide empty state
                                if (payments.isEmpty()) {
                                    showEmptyState(true);
                                } else {
                                    showEmptyState(false);
                                }
                            } else {
                                android.util.Log.w("PaymentListActivity", "Payment list is null");
                                showEmptyState(true);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("PaymentListActivity", "Error parsing payments: " + e.getMessage(), e);
                            Toast.makeText(PaymentListActivity.this, "Lỗi xử lý dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        android.util.Log.w("PaymentListActivity", "No payments key in response data");
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(PaymentListActivity.this, "Lỗi tải dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(PaymentListActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState(true);
            }
        });
    }
    
    private List<Payment> filterValidPayments(List<Payment> paymentList) {
        List<Payment> validPayments = new ArrayList<>();
        
        for (Payment payment : paymentList) {
            // Chỉ hiển thị payments có booking và booking đã được confirmed
            if (payment.getBooking() != null) {
                String bookingStatus = payment.getBooking().getStatus();
                // Chỉ hiển thị payments từ booking đã confirmed hoặc active
                if ("confirmed".equals(bookingStatus) || "active".equals(bookingStatus)) {
                    validPayments.add(payment);
                }
            }
        }
        
        android.util.Log.d("PaymentListActivity", "Filtered payments: " + validPayments.size() + " from " + paymentList.size() + " total");
        return validPayments;
    }
    
    private void calculateSummary(List<Payment> paymentList) {
        double totalPaid = 0;
        double pendingAmount = 0;
        
        for (Payment payment : paymentList) {
            if ("completed".equals(payment.getStatus())) {
                totalPaid += payment.getAmount();
            } else if ("pending".equals(payment.getStatus())) {
                pendingAmount += payment.getAmount();
            }
        }
        
        tvTotalPaid.setText(String.format("%.0f VNĐ", totalPaid));
        tvPendingAmount.setText(String.format("%.0f VNĐ", pendingAmount));
    }
    
    private void onPaymentClick(Payment payment) {
        // Navigate to payment detail
        Toast.makeText(this, "Chi tiết thanh toán: " + payment.getId(), Toast.LENGTH_SHORT).show();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showEmptyState(boolean show) {
        emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
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