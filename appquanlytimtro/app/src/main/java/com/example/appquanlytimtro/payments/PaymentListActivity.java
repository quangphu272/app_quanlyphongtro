package com.example.appquanlytimtro.payments;

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
import com.example.appquanlytimtro.adapters.PaymentAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentListActivity extends AppCompatActivity implements PaymentAdapter.OnPaymentClickListener {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private PaymentAdapter paymentAdapter;
    private List<Payment> payments;
    private RetrofitClient retrofitClient;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_list);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        // Get user role from intent or current user
        userRole = getIntent().getStringExtra("user_role");
        if (userRole == null) {
            userRole = getCurrentUserRole();
        }
        
        payments = new ArrayList<>();
        paymentAdapter = new PaymentAdapter(payments, this, userRole);
        recyclerView.setAdapter(paymentAdapter);
        
        loadPayments();
        setupSwipeRefresh();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPayments();
        });
    }
    
    private String getCurrentUserRole() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.example.appquanlytimtro.models.User user = gson.fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                return user != null ? user.getRole() : Constants.ROLE_TENANT;
            } catch (Exception e) {
                return Constants.ROLE_TENANT;
            }
        }
        return Constants.ROLE_TENANT;
    }
    
    private void loadPayments() {
        showLoading(true);
        
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "20");
        
        Call<ApiResponse<Map<String, Object>>> call;
        
        if (Constants.ROLE_TENANT.equals(userRole)) {
            // Load user's payments
            String userId = getCurrentUserId();
            if (userId != null) {
                call = retrofitClient.getApiService().getUserPayments(
                        retrofitClient.getToken(), 
                        userId, 
                        params
                );
            } else {
                showError("Không thể lấy thông tin người dùng");
                return;
            }
        } else if (Constants.ROLE_LANDLORD.equals(userRole)) {
            // Load payments for landlord's rooms
            call = retrofitClient.getApiService().getPayments(
                    retrofitClient.getToken(), 
                    params
            );
        } else {
            // Admin - load all payments
            call = retrofitClient.getApiService().getPayments(
                    retrofitClient.getToken(), 
                    params
            );
        }
        
        if (call != null) {
            call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Map<String, Object>> apiResponse = response.body();
                        
                        if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                            Map<String, Object> data = apiResponse.getData();
                            List<Payment> paymentList = (List<Payment>) data.get("payments");
                            
                            if (paymentList != null) {
                                payments.clear();
                                payments.addAll(paymentList);
                                paymentAdapter.notifyDataSetChanged();
                            }
                        } else {
                            showError(apiResponse.getMessage());
                        }
                    } else {
                        showError("Không thể tải danh sách thanh toán");
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
    }
    
    private String getCurrentUserId() {
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
    public void onPaymentClick(Payment payment) {
        // Show payment details in a dialog or toast
        Toast.makeText(this, "Chi tiết thanh toán: " + payment.getId(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPaymentAction(Payment payment, String action) {
        if ("pay".equals(action)) {
            // Process payment via API
            processPayment(payment);
        } else if ("view".equals(action)) {
            // View payment details
            onPaymentClick(payment);
        }
    }
    
    private void processPayment(Payment payment) {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().processPayment(token, payment.getId()).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PaymentListActivity.this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                    loadPayments(); // Refresh list
                } else {
                    Toast.makeText(PaymentListActivity.this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(PaymentListActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.payment_list_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_filter) {
            // Open filter dialog
            Toast.makeText(this, "Tính năng lọc đang được phát triển", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
