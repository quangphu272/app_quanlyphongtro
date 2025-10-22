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
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.landlord.AddRoomActivity;
import com.example.appquanlytimtro.landlord.EditRoomActivity;
import com.example.appquanlytimtro.rooms.PostRoomFragment;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.adapters.LandlordRoomAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordRoomManagementFragment extends Fragment implements LandlordRoomAdapter.OnRoomActionListener {

    private RecyclerView recyclerViewRooms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnAddRoom;
    
    private RetrofitClient retrofitClient;
    private User currentUser;
    private LandlordRoomAdapter roomAdapter;
    private java.util.List<Room> roomList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landlord_room_management, container, false);
        
        retrofitClient = RetrofitClient.getInstance(getContext());
        loadUserData();
        
        initViews(view);
        setupClickListeners();
        loadRooms();
        
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
        recyclerViewRooms = view.findViewById(R.id.recyclerViewRooms);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnAddRoom = view.findViewById(R.id.btnAddRoom);
        
        // Initialize room list and adapter
        roomList = new java.util.ArrayList<>();
        roomAdapter = new LandlordRoomAdapter(roomList, this);
        
        if (recyclerViewRooms != null) {
            recyclerViewRooms.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewRooms.setAdapter(roomAdapter);
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRooms);
        }
    }
    
    private void setupClickListeners() {
        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> openAddRoom());
        }
    }
    
    private void openAddRoom() {
        Intent intent = new Intent(getActivity(), AddRoomActivity.class);
        startActivity(intent);
    }
    
    private void loadRooms() {
        if (currentUser == null) {
            return;
        }
        
        
        String token = "Bearer " + retrofitClient.getToken();
        
        // Create empty query map instead of null
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        
        retrofitClient.getApiService().getUserRooms(token, currentUser.getId(), queryParams).enqueue(new Callback<ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.Map<String, Object>>> call, Response<ApiResponse<java.util.Map<String, Object>>> response) {
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    java.util.Map<String, Object> data = response.body().getData();
                    
                    if (data != null && data.containsKey("rooms")) {
                        try {
                            // Parse rooms from response
                            Gson gson = new Gson();
                            java.util.List<?> roomsData = (java.util.List<?>) data.get("rooms");
                            
                            roomList.clear();
                            
                            int successCount = 0;
                            int errorCount = 0;
                            
                            for (int i = 0; i < roomsData.size(); i++) {
                                Object roomObj = roomsData.get(i);
                                try {
                                    String roomJson = gson.toJson(roomObj);
                                    
                                    Room room = gson.fromJson(roomJson, Room.class);
                                    if (room != null) {
                                        roomList.add(room);
                                        successCount++;
                                    } else {
                                        errorCount++;
                                    }
                                } catch (Exception e) {
                                    errorCount++;
                                }
                            }
                            
                            
                            roomAdapter.notifyDataSetChanged();
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Đã tải " + roomList.size() + " phòng (thành công: " + successCount + ", lỗi: " + errorCount + ")", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi xử lý dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không có phòng nào", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể tải danh sách phòng", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.Map<String, Object>>> call, Throwable t) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    // Implementation of LandlordRoomAdapter.OnRoomActionListener
    // Removed onRoomClick method - no more "view details" functionality
    
    @Override
    public void onEditRoom(Room room) {
        Intent intent = new Intent(getActivity(), EditRoomActivity.class);
        intent.putExtra("room_id", room.getId());
        startActivity(intent);
    }
    
    @Override
    public void onDeleteRoom(Room room) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Xóa phòng")
                .setMessage("Bạn có chắc chắn muốn xóa phòng \"" + room.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteRoom(room))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    @Override
    public void onToggleAvailability(Room room) {
        // Toggle room status
        String currentStatus = room.getStatus();
        String newStatus;
        
        switch (currentStatus != null ? currentStatus.toLowerCase() : "active") {
            case "active":
                newStatus = "inactive";
                break;
            case "inactive":
                newStatus = "active";
                break;
            default:
                newStatus = "active";
                break;
        }
        
        updateRoomStatus(room, newStatus);
    }
    
    private void deleteRoom(Room room) {
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().deleteRoom(token, room.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                    loadRooms(); // Reload the list
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể xóa phòng", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void updateRoomStatus(Room room, String newStatus) {
        String token = "Bearer " + retrofitClient.getToken();
        
        // Create updated room object
        Room updatedRoom = new Room();
        updatedRoom.setStatus(newStatus);
        
        retrofitClient.getApiService().updateRoom(token, room.getId(), updatedRoom).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Đã cập nhật trạng thái phòng", Toast.LENGTH_SHORT).show();
                    }
                    loadRooms(); // Reload the list
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Không thể cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
