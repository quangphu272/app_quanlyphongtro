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
import com.example.appquanlytimtro.rooms.PostRoomFragment;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LandlordRoomManagementFragment extends Fragment {

    private RecyclerView recyclerViewRooms;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnAddRoom;
    private FloatingActionButton fabAddRoom;
    
    private RetrofitClient retrofitClient;
    private User currentUser;

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
        fabAddRoom = view.findViewById(R.id.fabAddRoom);
        
        if (recyclerViewRooms != null) {
            recyclerViewRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::loadRooms);
        }
    }
    
    private void setupClickListeners() {
        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> openAddRoom());
        }
        
        if (fabAddRoom != null) {
            fabAddRoom.setOnClickListener(v -> openAddRoom());
        }
    }
    
    private void openAddRoom() {
        Intent intent = new Intent(getActivity(), RoomListActivity.class);
        intent.putExtra("action", "add");
        startActivity(intent);
    }
    
    private void loadRooms() {
        if (currentUser == null) return;
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().getUserRooms(token, currentUser.getId(), null).enqueue(new Callback<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>>() {
            @Override
            public void onResponse(Call<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> call, Response<com.example.appquanlytimtro.models.ApiResponse<java.util.Map<String, Object>>> response) {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // TODO: Update RecyclerView with room data
                    Toast.makeText(getContext(), "Đã tải danh sách phòng", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Không thể tải danh sách phòng", Toast.LENGTH_SHORT).show();
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
