package com.example.appquanlytimtro.admin;

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
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingManagementFragment extends Fragment {

    private RecyclerView recyclerViewBookings;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnViewAllBookings;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_booking_management, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadBookings();
        
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
        recyclerViewBookings = view.findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnViewAllBookings = view.findViewById(R.id.btnViewAllBookings);
        
        if (recyclerViewBookings != null) {
            recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadBookings);
        }
    }
    
    private void setupClickListeners() {
        if (btnViewAllBookings != null) {
            btnViewAllBookings.setOnClickListener(v -> openAllBookings());
        }
    }
    
    private void openAllBookings() {
        Intent intent = new Intent(getActivity(), BookingListActivity.class);
        intent.putExtra("role", "admin");
        startActivity(intent);
    }
    
    private void loadBookings() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getBookings(token, null).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // TODO: Update RecyclerView with booking data
                    Toast.makeText(getContext(), "Đã tải danh sách đặt phòng toàn hệ thống", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách đặt phòng", Toast.LENGTH_SHORT).show();
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
