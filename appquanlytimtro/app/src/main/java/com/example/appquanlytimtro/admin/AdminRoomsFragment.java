package com.example.appquanlytimtro.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.appquanlytimtro.adapters.RoomAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRoomsFragment extends Fragment implements RoomAdapter.OnRoomClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvTotalRooms, tvActiveRooms, tvOccupiedRooms;
    private RoomAdapter adapter;
    private final List<Room> rooms = new ArrayList<>();
    private RetrofitClient retrofitClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_rooms, container, false);
        
        retrofitClient = RetrofitClient.getInstance(requireContext());
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvActiveRooms = view.findViewById(R.id.tvActiveRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        
        setupRecyclerView();
        setupSwipeRefresh();
        loadRooms();
        
        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomAdapter(rooms, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadRooms);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void loadRooms() {
        showLoading(true);
        
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "100"); // Load more rooms for admin
        
        retrofitClient.getApiService().getRooms(params)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            Object listObj = response.body().getData().get("rooms");
                            
                            if (listObj instanceof List<?>) {
                                rooms.clear();
                                int totalRooms = 0;
                                int activeRooms = 0;
                                int occupiedRooms = 0;
                                
                                for (Object o : (List<?>) listObj) {
                                    String json = gson.toJson(o);
                                    Room room = gson.fromJson(json, Room.class);
                                    rooms.add(room);
                                    
                                    // Count statistics
                                    totalRooms++;
                                    if ("active".equals(room.getStatus())) {
                                        activeRooms++;
                                        // Assume room is occupied if status is active
                                        // In a real app, you might check bookings or other indicators
                                        occupiedRooms++;
                                    }
                                }
                                
                                // Update statistics
                                tvTotalRooms.setText(String.valueOf(totalRooms));
                                tvActiveRooms.setText(String.valueOf(activeRooms));
                                tvOccupiedRooms.setText(String.valueOf(occupiedRooms));
                                
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải danh sách phòng", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRoomClick(Room room) {
        // Handle room click - could show room details
        Toast.makeText(getContext(), "Xem chi tiết phòng: " + room.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
