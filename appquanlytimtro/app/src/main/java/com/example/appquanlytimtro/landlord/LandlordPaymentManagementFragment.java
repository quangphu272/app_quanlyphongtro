package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.payments.PaymentListActivity;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordPaymentManagementFragment extends Fragment {

    private RecyclerView recyclerViewPayments;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnViewAllPayments;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_payment_management, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
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
        recyclerViewPayments = view.findViewById(R.id.recyclerViewPayments);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnViewAllPayments = view.findViewById(R.id.btnViewAllPayments);
        
        if (recyclerViewPayments != null) {
            recyclerViewPayments.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadPayments);
        }
    }
    
    private void setupClickListeners() {
        if (btnViewAllPayments != null) {
            btnViewAllPayments.setOnClickListener(v -> openAllPayments());
        }
    }
    
    private void openAllPayments() {
        Intent intent = new Intent(getActivity(), PaymentListActivity.class);
        intent.putExtra("role", "landlord");
        startActivity(intent);
    }
    
    private void loadPayments() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getPayments(token, null).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // TODO: Update RecyclerView with payment data
                    Toast.makeText(getContext(), "Đã tải danh sách thanh toán", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách thanh toán", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
