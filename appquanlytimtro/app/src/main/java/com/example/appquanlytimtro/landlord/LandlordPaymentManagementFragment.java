package com.example.appquanlytimtro.landlord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.PaymentAdapter;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordPaymentManagementFragment extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvTotalPaid, tvPendingAmount;
    private LinearLayout emptyState;
    
    private PaymentAdapter paymentAdapter;
    private List<Payment> payments;
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_payment_list, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadPayments();
        
        return view;
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalPaid = view.findViewById(R.id.tvTotalPaid);
        tvPendingAmount = view.findViewById(R.id.tvPendingAmount);
        emptyState = view.findViewById(R.id.emptyState);
        
        payments = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        // Get current user role
        String userRole = "landlord"; // default for this fragment
        if (currentUser != null && currentUser.getRole() != null) {
            userRole = currentUser.getRole();
        }
        
        paymentAdapter = new PaymentAdapter(payments, new PaymentAdapter.OnPaymentClickListener() {
            @Override
            public void onPaymentClick(Payment payment) {
                // Handle payment click if needed
                Toast.makeText(getContext(), "Payment: " + payment.getId(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onPaymentAction(Payment payment, String action) {
                // Handle payment actions if needed
                Toast.makeText(getContext(), "Action: " + action + " for payment: " + payment.getId(), Toast.LENGTH_SHORT).show();
            }
        }, userRole);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(paymentAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadPayments);
    }
    
    private void loadPayments() {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        
        // Use the general payments endpoint
        retrofitClient.getApiService().getPayments(token, new java.util.HashMap<>()).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("payments")) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String paymentsJson = gson.toJson(data.get("payments"));
                            List<Payment> paymentList = gson.fromJson(paymentsJson, new com.google.gson.reflect.TypeToken<List<Payment>>(){}.getType());
                            
                            
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
                                showEmptyState(true);
                            }
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Lỗi xử lý dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                            showEmptyState(true);
                        }
                    } else {
                        showEmptyState(true);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
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
        
        return validPayments;
    }
    
    private void calculateSummary(List<Payment> paymentList) {
        double paidAmount = 0;
        double pendingAmount = 0;
        
        for (Payment payment : paymentList) {
            if ("completed".equals(payment.getStatus())) {
                paidAmount += payment.getAmount();
            } else if ("pending".equals(payment.getStatus())) {
                pendingAmount += payment.getAmount();
            }
        }
        
        tvTotalPaid.setText(String.format("%.0f VNĐ", paidAmount));
        tvPendingAmount.setText(String.format("%.0f VNĐ", pendingAmount));
    }
    
    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }
    
    private void showEmptyState(boolean show) {
        if (show) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}