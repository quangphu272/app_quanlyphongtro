package com.example.appquanlytimtro.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.AdminBookingAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminBookingManagementFragment extends Fragment implements AdminBookingAdapter.OnBookingActionListener {

    private RetrofitClient retrofitClient;
    private List<Booking> bookings;
    private AdminBookingAdapter bookingAdapter;
    
    // Views
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private View emptyView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrofitClient = RetrofitClient.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_booking_management, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        
        loadBookings();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBookings);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupRecyclerView() {
        bookings = new ArrayList<>();
        bookingAdapter = new AdminBookingAdapter(bookings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                        
                        if (bookingsData != null) {
                            bookings.clear();
                            Gson gson = new Gson();
                            for (Object bookingObj : bookingsData) {
                                if (bookingObj instanceof Map) {
                                    Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                                    bookings.add(booking);
                                }
                            }
                            bookingAdapter.notifyDataSetChanged();
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
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewBookingDetails(Booking booking) {
        // Navigate to booking detail
        // Intent intent = new Intent(getContext(), BookingDetailActivity.class);
        // intent.putExtra("booking_id", booking.getId());
        // startActivity(intent);
    }

    @Override
    public void onDeleteBooking(Booking booking) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đặt phòng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    deleteBooking(booking.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBooking(String bookingId) {
        showLoading(true);
        
        String token = "Bearer " + retrofitClient.getToken();
        retrofitClient.getApiService().deleteBooking(token, bookingId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        showError("Xóa đặt phòng thành công");
                        loadBookings(); // Reload list
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể xóa đặt phòng");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}