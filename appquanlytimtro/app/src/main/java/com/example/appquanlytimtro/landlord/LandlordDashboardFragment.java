package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;

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
    private MaterialCardView cardLogout;
    private MaterialButton btnAddRoom, btnManageRooms, btnViewBookings;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_dashboard, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadDashboardData();
        
        return view;
    }

    private void initViews(View view) {
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        tvPendingBookings = view.findViewById(R.id.tvPendingBookings);
        cardLogout = view.findViewById(R.id.cardLogout);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);
        btnManageRooms = view.findViewById(R.id.btnManageRooms);
        btnViewBookings = view.findViewById(R.id.btnViewBookings);
    }

    private void setupClickListeners() {
        cardLogout.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Call logout method from MainActivity
                    if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                        ((com.example.appquanlytimtro.MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddRoomActivity.class);
                startActivity(intent);
            });
        }
        
        if (btnManageRooms != null) {
            btnManageRooms.setOnClickListener(v -> {
                // Navigate to room management fragment
                if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                    ((com.example.appquanlytimtro.MainActivity) getActivity()).navigateToFragment(1); // Room management tab
                }
            });
        }
        
        if (btnViewBookings != null) {
            btnViewBookings.setOnClickListener(v -> {
                // Navigate to bookings fragment
                if (getActivity() instanceof com.example.appquanlytimtro.MainActivity) {
                    ((com.example.appquanlytimtro.MainActivity) getActivity()).navigateToFragment(2); // Bookings tab
                }
            });
        }
    }

    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }

    private void loadDashboardData() {
        if (currentUser == null) {
            loadDefaultData();
            return;
        }
        
        
        // Skip the dashboard API and go directly to load real data
        // This ensures we always get real data
        loadRealDataFromAPIs();
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
    
    private void loadRealDataFromAPIs() {
        String token = "Bearer " + retrofitClient.getToken();
        
        
        // Load rooms data
        java.util.Map<String, String> roomParams = new java.util.HashMap<>();
        roomParams.put("landlordId", currentUser.getId());
        
        retrofitClient.getApiService().getRooms(roomParams).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("rooms")) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String roomsJson = gson.toJson(data.get("rooms"));
                            java.util.List<com.example.appquanlytimtro.models.Room> rooms = gson.fromJson(roomsJson, new com.google.gson.reflect.TypeToken<java.util.List<com.example.appquanlytimtro.models.Room>>(){}.getType());
                            
                            
                            if (rooms != null) {
                                final int totalRooms = rooms.size();
                                final int[] occupiedRooms = {0};
                                final double[] totalRevenue = {0};
                                
                                for (com.example.appquanlytimtro.models.Room room : rooms) {
                                    if ("active".equals(room.getStatus())) {
                                        occupiedRooms[0]++;
                                    }
                                    if (room.getPrice() != null) {
                                        totalRevenue[0] += room.getPrice().getMonthly();
                                    }
                                }
                                
                                
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        tvTotalRooms.setText(String.valueOf(totalRooms));
                                        tvOccupiedRooms.setText(String.valueOf(occupiedRooms[0]));
                                        tvTotalRevenue.setText(String.format("%.0f VNĐ", totalRevenue[0]));
                                    });
                                }
                            } else {
                                loadDefaultData();
                            }
                        } catch (Exception e) {
                            loadDefaultData();
                        }
                    } else {
                        loadDefaultData();
                    }
                } else {
                    loadDefaultData();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                loadDefaultData();
            }
        });
        
        // Load bookings data
        java.util.Map<String, String> bookingParams = new java.util.HashMap<>();
        bookingParams.put("landlordId", currentUser.getId());
        
        
        retrofitClient.getApiService().getBookings(token, bookingParams).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("bookings")) {
                        try {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String bookingsJson = gson.toJson(data.get("bookings"));
                            java.util.List<com.example.appquanlytimtro.models.Booking> bookings = gson.fromJson(bookingsJson, new com.google.gson.reflect.TypeToken<java.util.List<com.example.appquanlytimtro.models.Booking>>(){}.getType());
                            
                            
                            if (bookings != null) {
                                final int[] pendingBookings = {0};
                                for (com.example.appquanlytimtro.models.Booking booking : bookings) {
                                    if ("pending".equals(booking.getStatus())) {
                                        pendingBookings[0]++;
                                    }
                                }
                                
                                
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        tvPendingBookings.setText(String.valueOf(pendingBookings[0]));
                                    });
                                }
                            } else {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        tvPendingBookings.setText("0");
                                    });
                                }
                            }
                        } catch (Exception e) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    tvPendingBookings.setText("0");
                                });
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                tvPendingBookings.setText("0");
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            tvPendingBookings.setText("0");
                        });
                    }
                }
            }
            
            @Override
            public void onFailure(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvPendingBookings.setText("0");
                    });
                }
            }
        });
    }
    
    private void loadDefaultData() {
        tvTotalRooms.setText("0");
        tvOccupiedRooms.setText("0");
        tvTotalRevenue.setText("0 VNĐ");
        tvPendingBookings.setText("0");
    }
}
