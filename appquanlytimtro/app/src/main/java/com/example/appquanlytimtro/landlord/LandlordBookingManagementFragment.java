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
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.adapters.LandlordBookingAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordBookingManagementFragment extends Fragment implements LandlordBookingAdapter.OnBookingActionListener {

    private RecyclerView recyclerViewBookings;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnViewAllBookings;
    
    private RetrofitClient retrofitClient;
    private User currentUser;
    private LandlordBookingAdapter bookingAdapter;
    private java.util.List<Booking> bookingList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_booking_management, container, false);
        
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
        
        // Initialize booking list and adapter
        bookingList = new java.util.ArrayList<>();
        bookingAdapter = new LandlordBookingAdapter(bookingList, this);
        
        if (recyclerViewBookings != null) {
            recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewBookings.setAdapter(bookingAdapter);
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
        intent.putExtra("role", "landlord");
        startActivity(intent);
    }
    
    private void loadBookings() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        // Get bookings for landlord's rooms
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("landlord", currentUser.getId());
        
        retrofitClient.getApiService().getBookings(token, params).enqueue(new Callback<ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.Map<String, Object>>> call, Response<ApiResponse<java.util.Map<String, Object>>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    if (data != null && data.containsKey("bookings")) {
                        try {
                            // Parse bookings from response
                            Gson gson = new Gson();
                            java.util.List<?> bookingsData = (java.util.List<?>) data.get("bookings");
                            bookingList.clear();
                            
                            for (Object bookingObj : bookingsData) {
                                String bookingJson = gson.toJson(bookingObj);
                                Booking booking = gson.fromJson(bookingJson, Booking.class);
                                bookingList.add(booking);
                            }
                            
                            bookingAdapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Đã tải " + bookingList.size() + " đơn đặt phòng", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không có đơn đặt phòng nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách đặt phòng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Implementation of LandlordBookingAdapter.OnBookingActionListener
    @Override
    public void onBookingClick(Booking booking) {
        // Navigate to booking detail
        Toast.makeText(getContext(), "Chi tiết đặt phòng: " + booking.getId(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onConfirmBooking(Booking booking) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xác nhận đặt phòng")
                .setMessage("Bạn có chắc chắn muốn xác nhận đơn đặt phòng này?")
                .setPositiveButton("Xác nhận", (dialog, which) -> confirmBooking(booking))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    @Override
    public void onRejectBooking(Booking booking) {
        // Show rejection dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Từ chối đặt phòng")
                .setMessage("Bạn có chắc chắn muốn từ chối đơn đặt phòng này?")
                .setPositiveButton("Từ chối", (dialog, which) -> rejectBooking(booking))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    @Override
    public void onViewContract(Booking booking) {
        // Navigate to contract view
        Toast.makeText(getContext(), "Xem hợp đồng: " + booking.getId(), Toast.LENGTH_SHORT).show();
    }
    
    private void confirmBooking(Booking booking) {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().confirmBooking(token, booking.getId()).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Đã xác nhận đặt phòng", Toast.LENGTH_SHORT).show();
                    loadBookings(); // Reload the list
                } else {
                    Toast.makeText(getContext(), "Không thể xác nhận đặt phòng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void rejectBooking(Booking booking) {
        String token = "Bearer " + retrofitClient.getToken();
        
        java.util.Map<String, String> reason = new java.util.HashMap<>();
        reason.put("reason", "Từ chối bởi chủ trọ");
        
        retrofitClient.getApiService().cancelBooking(token, booking.getId(), reason).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Đã từ chối đặt phòng", Toast.LENGTH_SHORT).show();
                    loadBookings(); // Reload the list
                } else {
                    Toast.makeText(getContext(), "Không thể từ chối đặt phòng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
