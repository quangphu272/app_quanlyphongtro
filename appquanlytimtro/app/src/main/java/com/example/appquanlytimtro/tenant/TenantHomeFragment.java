//fragment: màn hình chính cho người thuê trọ
// Mục đích file: File này dùng để hiển thị màn hình chính cho người thuê trọ
// function: 
// - onCreateView(): Khởi tạo view và setup các component
// - initViews(): Khởi tạo các view components
// - setupRecyclerView(): Thiết lập RecyclerView và adapter
// - setupClickListeners(): Thiết lập các sự kiện click
// - loadUserData(): Tải thông tin user hiện tại
// - loadRecentRooms(): Tải danh sách phòng gần đây
// - loadUserBookings(): Tải danh sách đặt phòng của user
// - onSearchRoomsClick(): Xử lý click tìm kiếm phòng
// - onViewAllRoomsClick(): Xử lý click xem tất cả phòng
// - onViewBookingsClick(): Xử lý click xem đặt phòng
// - onRoomClick(): Xử lý click vào phòng
// - showLoading(): Hiển thị/ẩn loading indicator
// - showError(): Hiển thị thông báo lỗi
package com.example.appquanlytimtro.tenant;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.payments.PaymentListActivity;
import com.example.appquanlytimtro.profile.ProfileActivity;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.models.ApiResponse;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantHomeFragment extends Fragment {

    private TextView tvWelcome;
    private TextView tvTotalBookings;
    private TextView tvPendingPayments;
    private TextView tvTotalDeposit;
    private TextView tvTotalPaid;
    private MaterialCardView cardSearchRooms;
    private MaterialCardView cardMyBookings;
    private MaterialCardView cardMyPayments;
    private MaterialCardView cardMyProfile;
    private MaterialCardView cardLogout;
    private RecyclerView recyclerViewRecentRooms;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tenant_home, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadStatistics();
        
        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        tvPendingPayments = view.findViewById(R.id.tvPendingPayments);
        tvTotalDeposit = view.findViewById(R.id.tvTotalDeposit);
        tvTotalPaid = view.findViewById(R.id.tvTotalPaid);
        cardSearchRooms = view.findViewById(R.id.cardSearchRooms);
        cardMyBookings = view.findViewById(R.id.cardMyBookings);
        cardMyPayments = view.findViewById(R.id.cardMyPayments);
        cardMyProfile = view.findViewById(R.id.cardMyProfile);
        cardLogout = view.findViewById(R.id.cardLogout);
        recyclerViewRecentRooms = view.findViewById(R.id.recyclerViewRecentRooms);
        
        recyclerViewRecentRooms.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
            if (currentUser != null) {
                updateWelcomeMessage();
            }
        }
    }
    
    private void updateWelcomeMessage() {
        if (tvWelcome != null && currentUser != null) {
            String welcomeText = "Chào mừng " + currentUser.getFullName() + " đến với Quản Lý Tìm Trọ!";
            tvWelcome.setText(welcomeText);
        }
    }

    private void setupClickListeners() {
        cardSearchRooms.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoomListActivity.class);
            intent.putExtra("show_available_only", true); // Chỉ hiển thị phòng có sẵn
            startActivity(intent);
        });

        cardMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BookingListActivity.class);
            startActivity(intent);
        });

        cardMyPayments.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PaymentListActivity.class);
            startActivity(intent);
        });

        cardMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }

    private void loadStatistics() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getBookings(token, new java.util.HashMap<>()).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null && data.containsKey("bookings")) {
                        List<?> bookingsData = (List<?>) data.get("bookings");
                        calculateBookingStats(bookingsData);
                    }
                }
                loadPaymentStats();
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                loadPaymentStats();
            }
        });
    }
    
    private void calculateBookingStats(List<?> bookingsData) {
        int totalBookings = 0;
        int pendingPayments = 0;
        double totalDeposit = 0.0;
        
        Gson gson = new Gson();
        for (Object bookingObj : bookingsData) {
            try {
                Booking booking = gson.fromJson(gson.toJson(bookingObj), Booking.class);
                if (booking != null) {
                    totalBookings++;
                    
                    if ("pending".equals(booking.getStatus())) {
                        pendingPayments++;
                    }
                    
                    if (booking.getPricing() != null) {
                        totalDeposit += booking.getPricing().getDeposit();
                    }
                }
            } catch (Exception e) {
            }
        }
        
        tvTotalBookings.setText(String.valueOf(totalBookings));
        tvPendingPayments.setText(String.valueOf(pendingPayments));
        
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        tvTotalDeposit.setText(formatter.format(totalDeposit) + " VNĐ");
    }
    
    private void loadPaymentStats() {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getPayments(token, new java.util.HashMap<>()).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Map<String, Object> data = response.body().getData();
                    if (data != null && data.containsKey("payments")) {
                        List<?> paymentsData = (List<?>) data.get("payments");
                        calculatePaymentStats(paymentsData);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
            }
        });
    }
    
    private void calculatePaymentStats(List<?> paymentsData) {
        double totalPaid = 0.0;
        
        Gson gson = new Gson();
        for (Object paymentObj : paymentsData) {
            try {
                Payment payment = gson.fromJson(gson.toJson(paymentObj), Payment.class);
                if (payment != null && "completed".equals(payment.getStatus())) {
                    totalPaid += payment.getAmount();
                }
            } catch (Exception e) {
            }
        }
        
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        tvTotalPaid.setText(formatter.format(totalPaid) + " VNĐ");
    }
}
