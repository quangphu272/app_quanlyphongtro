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
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TenantHomeFragment extends Fragment {

    private TextView tvWelcome;
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
        
        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
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
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Call logout method from MainActivity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
    }
}
