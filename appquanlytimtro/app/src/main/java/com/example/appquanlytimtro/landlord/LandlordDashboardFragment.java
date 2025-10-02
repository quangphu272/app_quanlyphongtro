package com.example.appquanlytimtro.landlord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordDashboardFragment extends Fragment {

    private TextView tvTotalRooms;
    private TextView tvOccupiedRooms;
    private TextView tvTotalRevenue;
    private TextView tvPendingBookings;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        loadDashboardData();
        
        return view;
    }

    private void initViews(View view) {
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvPendingBookings = view.findViewById(R.id.tvPendingBookings);
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    private void loadDashboardData() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getDashboardStats(token).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    updateDashboardData(data);
                } else {
                    loadDefaultData();
                }
            }

            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                loadDefaultData();
            }
        });
    }
    
    private void updateDashboardData(java.util.Map<String, Object> data) {
        if (tvTotalRooms != null) {
            Object totalRooms = data.get("totalRooms");
            tvTotalRooms.setText(totalRooms != null ? totalRooms.toString() : "0");
        }
        
        if (tvOccupiedRooms != null) {
            Object occupiedRooms = data.get("occupiedRooms");
            tvOccupiedRooms.setText(occupiedRooms != null ? occupiedRooms.toString() : "0");
        }
        
        if (tvTotalRevenue != null) {
            Object revenue = data.get("totalRevenue");
            if (revenue != null) {
                tvTotalRevenue.setText(revenue.toString() + " VNĐ");
            } else {
                tvTotalRevenue.setText("0 VNĐ");
            }
        }
        
        if (tvPendingBookings != null) {
            Object pendingBookings = data.get("pendingBookings");
            tvPendingBookings.setText(pendingBookings != null ? pendingBookings.toString() : "0");
        }
    }
    
    private void loadDefaultData() {
        tvTotalRooms.setText("0");
        tvOccupiedRooms.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
        tvPendingBookings.setText("0");
    }
}
