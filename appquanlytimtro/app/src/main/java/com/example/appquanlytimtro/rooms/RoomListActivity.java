package com.example.appquanlytimtro.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.RoomAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.google.gson.Gson;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomListActivity extends AppCompatActivity implements RoomAdapter.OnRoomClickListener {
    
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private RoomAdapter roomAdapter;
    private List<Room> rooms;
    private RetrofitClient retrofitClient;
    private boolean showMyRooms = false;
    private boolean showAvailableOnly = false;
    
    // Search views
    private TextInputEditText etSearch;
    private MaterialButton btnSearch;
    private MaterialButton btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        
        initViews();
        setupToolbar();
        setupSearchListeners();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        // Check intent parameters
        Intent intent = getIntent();
        if (intent != null) {
            showMyRooms = intent.getBooleanExtra("show_my_rooms", false);
            showAvailableOnly = intent.getBooleanExtra("show_available_only", false);
            
            if (showMyRooms) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Phòng trọ của tôi");
                }
            } else if (showAvailableOnly) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Phòng trọ có sẵn");
                }
            }
        }
        
        setupRecyclerView();
        setupSwipeRefresh();
        loadRooms("");
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        
        // Search views
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Danh sách phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        rooms = new ArrayList<>();
        roomAdapter = new RoomAdapter(rooms, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);
    }
    
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
            loadRooms(searchQuery);
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
    }

    private void setupSearchListeners() {
        btnSearch.setOnClickListener(v -> performSearch());
        btnFilter.setOnClickListener(v -> showFilterDialog());
        
        // Add search on Enter key press
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private void performSearch() {
        String searchQuery = etSearch.getText() != null ? etSearch.getText().toString().trim() : "";
        loadRooms(searchQuery);
    }

    private void showFilterDialog() {
        // TODO: Implement filter dialog
        Toast.makeText(this, "Chức năng bộ lọc sẽ được phát triển", Toast.LENGTH_SHORT).show();
    }

    private void loadRooms(String searchQuery) {
        showLoading(true);
        
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "20");
        
        // Add search query if provided
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            params.put("search", searchQuery.trim());
        }
        
        // Add filter for available rooms only
        if (showAvailableOnly) {
            params.put("status", "active");
            params.put("available", "true");
            params.put("excludeBooked", "true"); // Exclude rooms with pending bookings
        }
        
        Call<ApiResponse<Map<String, Object>>> call = null;
        
        if (showMyRooms) {
            // Load user's own rooms
            String userId = getCurrentUserId();
            if (userId != null) {
                call = retrofitClient.getApiService().getUserRooms(
                        retrofitClient.getToken(), 
                        userId, 
                        params
                );
            } else {
                showError("Không thể lấy thông tin người dùng");
                showLoading(false);
                return;
            }
        } else {
            // Load all rooms
            call = retrofitClient.getApiService().getRooms(params);
        }
        
        if (call != null) {
            call.enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        List<?> roomsData = (List<?>) data.get("rooms");
                        
                        if (roomsData != null) {
                            rooms.clear();
                            Gson gson = new Gson();
                            for (Object roomObj : roomsData) {
                                if (roomObj instanceof Map) {
                                    Room room = gson.fromJson(gson.toJson(roomObj), Room.class);
                                    rooms.add(room);
                                }
                            }
                            roomAdapter.notifyDataSetChanged();
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải danh sách phòng trọ");
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
    }
    
    private String getCurrentUserId() {
        // Get current user ID from stored user data
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                com.example.appquanlytimtro.models.User user = gson.fromJson(userJson, com.example.appquanlytimtro.models.User.class);
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra("room_id", room.getId());
        intent.putExtra("room_object", room); // Pass room object for booking details
        startActivity(intent);
    }
    
    public void onRoomLike(Room room) {
        // Handle room like functionality
        retrofitClient.getApiService().toggleRoomLike(retrofitClient.getToken(), room.getId())
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                // Update the room in the list
                                int position = rooms.indexOf(room);
                                if (position != -1) {
                                    roomAdapter.notifyItemChanged(position);
                                }
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        // Handle error silently
                    }
                });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showMyRooms) {
            getMenuInflater().inflate(R.menu.room_list_menu, menu);
        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_add_room) {
            // Navigate to add room activity
            Toast.makeText(this, "Chức năng thêm phòng sẽ được phát triển", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
