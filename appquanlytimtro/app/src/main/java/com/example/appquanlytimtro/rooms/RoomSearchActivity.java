package com.example.appquanlytimtro.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.RoomAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomSearchActivity extends AppCompatActivity implements RoomAdapter.OnRoomClickListener {
    
    private TextInputEditText etSearch;
    private AutoCompleteTextView actvCity, actvDistrict, actvRoomType;
    private TextInputEditText etMinPrice, etMaxPrice;
    private MaterialButton btnSearch, btnClear;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    
    private RoomAdapter roomAdapter;
    private List<Room> roomList;
    private RetrofitClient retrofitClient;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_search);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchFilters();
        loadRooms();
    }
    
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        actvCity = findViewById(R.id.actvCity);
        actvDistrict = findViewById(R.id.actvDistrict);
        actvRoomType = findViewById(R.id.actvRoomType);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        btnSearch = findViewById(R.id.btnSearch);
        btnClear = findViewById(R.id.btnClear);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        
        retrofitClient = RetrofitClient.getInstance(this);
        roomList = new ArrayList<>();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tìm kiếm phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(roomList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(roomAdapter);
    }
    
    private void setupSearchFilters() {
        // Setup city dropdown
        String[] cities = {"Tất cả", "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ"};
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cities);
        actvCity.setAdapter(cityAdapter);
        
        // Setup district dropdown
        String[] districts = {"Tất cả", "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5", "Hải Châu", "Thanh Khê", "Sơn Trà"};
        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districts);
        actvDistrict.setAdapter(districtAdapter);
        
        // Setup room type dropdown
        String[] roomTypes = {"Tất cả", "Studio", "1 phòng ngủ", "2 phòng ngủ", "3 phòng ngủ"};
        ArrayAdapter<String> roomTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomTypes);
        actvRoomType.setAdapter(roomTypeAdapter);
        
        // Setup search button
        btnSearch.setOnClickListener(v -> performSearch());
        
        // Setup clear button
        btnClear.setOnClickListener(v -> clearFilters());
        
        // Setup real-time search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Debounce search - search after user stops typing for 500ms
                recyclerView.removeCallbacks(searchRunnable);
                recyclerView.postDelayed(searchRunnable, 500);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private final Runnable searchRunnable = this::performSearch;
    
    private void performSearch() {
        showLoading(true);
        
        Map<String, String> queryParams = new HashMap<>();
        
        // Add search keyword
        String searchKeyword = etSearch.getText().toString().trim();
        if (!searchKeyword.isEmpty()) {
            queryParams.put("search", searchKeyword);
        }
        
        // Add city filter
        String city = actvCity.getText().toString().trim();
        if (!city.isEmpty() && !city.equals("Tất cả")) {
            queryParams.put("city", city);
        }
        
        // Add district filter
        String district = actvDistrict.getText().toString().trim();
        if (!district.isEmpty() && !district.equals("Tất cả")) {
            queryParams.put("district", district);
        }
        
        // Add room type filter
        String roomType = actvRoomType.getText().toString().trim();
        if (!roomType.isEmpty() && !roomType.equals("Tất cả")) {
            // Convert display text to API value
            switch (roomType) {
                case "Studio":
                    queryParams.put("roomType", "studio");
                    break;
                case "1 phòng ngủ":
                    queryParams.put("roomType", "1bedroom");
                    break;
                case "2 phòng ngủ":
                    queryParams.put("roomType", "2bedroom");
                    break;
                case "3 phòng ngủ":
                    queryParams.put("roomType", "3bedroom");
                    break;
            }
        }
        
        // Add price range filter
        String minPrice = etMinPrice.getText().toString().trim();
        if (!minPrice.isEmpty()) {
            queryParams.put("minPrice", minPrice);
        }
        
        String maxPrice = etMaxPrice.getText().toString().trim();
        if (!maxPrice.isEmpty()) {
            queryParams.put("maxPrice", maxPrice);
        }
        
        // Add default filters
        queryParams.put("status", "active");
        queryParams.put("availability", "true");
        queryParams.put("excludeBooked", "true"); // Exclude rooms with pending bookings
        
        
        retrofitClient.getApiService().getRooms(queryParams).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        if (data.containsKey("rooms")) {
                            try {
                                Gson gson = new Gson();
                                List<?> roomsData = (List<?>) data.get("rooms");
                                
                                roomList.clear();
                                
                                for (Object roomObj : roomsData) {
                                    String roomJson = gson.toJson(roomObj);
                                    Room room = gson.fromJson(roomJson, Room.class);
                                    if (room != null) {
                                        roomList.add(room);
                                    }
                                }
                                
                                roomAdapter.notifyDataSetChanged();
                                
                                if (roomList.isEmpty()) {
                                    Toast.makeText(RoomSearchActivity.this, "Không tìm thấy phòng phù hợp", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RoomSearchActivity.this, "Tìm thấy " + roomList.size() + " phòng", Toast.LENGTH_SHORT).show();
                                }
                                
                            } catch (Exception e) {
                                Toast.makeText(RoomSearchActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(RoomSearchActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RoomSearchActivity.this, "Không thể tải danh sách phòng", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(RoomSearchActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void clearFilters() {
        etSearch.setText("");
        actvCity.setText("");
        actvDistrict.setText("");
        actvRoomType.setText("");
        etMinPrice.setText("");
        etMaxPrice.setText("");
        performSearch();
    }
    
    private void loadRooms() {
        performSearch();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(this, RoomDetailActivity.class);
        intent.putExtra("room_id", room.getId());
        intent.putExtra("room_object", room); // Pass room object for booking details
        startActivity(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_search_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
