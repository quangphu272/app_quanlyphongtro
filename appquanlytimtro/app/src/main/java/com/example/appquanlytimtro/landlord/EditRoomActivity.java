package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.SelectedImageAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRoomActivity extends AppCompatActivity implements SelectedImageAdapter.OnImageRemoveListener {
    
    private String roomId;
    private Room room;
    private RetrofitClient retrofitClient;
    private User currentUser;
    
    // Views
    private EditText etTitle, etDescription, etArea, etMonthlyPrice, etDeposit;
    private EditText etElectricity, etWater, etInternet, etOther;
    private EditText etStreet, etWard, etDistrict, etCity;
    private TextInputEditText actvRoomType;
    private LinearLayout layoutAmenities, layoutRules;
    private RecyclerView recyclerViewImages;
    private ProgressBar progressBar;
    
    // Data
    private List<Uri> imageUris = new ArrayList<>();
    private List<String> selectedAmenities = new ArrayList<>();
    private List<String> selectedRules = new ArrayList<>();
    private SelectedImageAdapter imageAdapter;
    
    // Activity result launcher for image selection
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room);
        
        initViews();
        setupToolbar();
        setupImagePicker();
        
        retrofitClient = RetrofitClient.getInstance(this);
        loadUserData();
        
        // Get room ID from intent
        Intent intent = getIntent();
        if (intent != null) {
            roomId = intent.getStringExtra("room_id");
            if (roomId != null) {
                loadRoomDetails();
            } else {
                showError("Không tìm thấy thông tin phòng trọ");
                finish();
            }
        } else {
            showError("Không tìm thấy thông tin phòng trọ");
            finish();
        }
    }
    
    private void initViews() {
        // Basic info
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etArea = findViewById(R.id.etArea);
        etMonthlyPrice = findViewById(R.id.etMonthlyPrice);
        etDeposit = findViewById(R.id.etDeposit);
        
        // Utilities
        etElectricity = findViewById(R.id.etElectricity);
        etWater = findViewById(R.id.etWater);
        etInternet = findViewById(R.id.etInternet);
        etOther = findViewById(R.id.etOther);
        
        // Address
        etStreet = findViewById(R.id.etStreet);
        etWard = findViewById(R.id.etWard);
        etDistrict = findViewById(R.id.etDistrict);
        etCity = findViewById(R.id.etCity);
        
        // Room type
        actvRoomType = findViewById(R.id.actvRoomType);
        setupRoomTypeDropdown();
        
        // Amenities and rules
        layoutAmenities = findViewById(R.id.layoutAmenities);
        layoutRules = findViewById(R.id.layoutRules);
        setupAmenitiesAndRules();
        
        // Images
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        setupImageRecyclerView();
        
        // Progress bar
        progressBar = findViewById(R.id.progressBar);
        
        // Buttons
        Button btnSelectImages = findViewById(R.id.btnSelectImages);
        btnSelectImages.setOnClickListener(v -> selectImages());
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chỉnh sửa phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        // Multiple images selected
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                    } else if (result.getData().getData() != null) {
                        // Single image selected
                        Uri imageUri = result.getData().getData();
                        imageUris.add(imageUri);
                    }
                    imageAdapter.notifyDataSetChanged();
                }
            }
        );
    }
    
    private void setupRoomTypeDropdown() {
        actvRoomType.setOnClickListener(v -> showRoomTypeDialog());
    }
    
    private void showRoomTypeDialog() {
        String[] roomTypes = {"studio", "1bedroom", "2bedroom", "3bedroom", "shared"};
        String[] roomTypeLabels = {"Studio", "1 phòng ngủ", "2 phòng ngủ", "3 phòng ngủ", "Phòng chung"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn loại phòng");
        builder.setItems(roomTypeLabels, (dialog, which) -> {
            actvRoomType.setText(roomTypes[which]);
        });
        builder.show();
    }
    
    private void setupAmenitiesAndRules() {
        // Amenities
        String[] amenities = {"wifi", "air_conditioner", "refrigerator", "washing_machine", 
                             "hot_water", "desk", "wardrobe", "balcony", "parking", 
                             "kitchen", "bed", "security"};
        
        for (String amenity : amenities) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(getAmenityText(amenity));
            checkBox.setTag(amenity);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedAmenities.add(amenity);
                } else {
                    selectedAmenities.remove(amenity);
                }
            });
            layoutAmenities.addView(checkBox);
        }
        
        // Rules
        String[] rules = {"Không hút thuốc", "Không ồn ào sau 22h", "Không tổ chức tiệc tùng", 
                         "Giữ gìn vệ sinh chung", "Không nuôi thú cưng", 
                         "Không sử dụng thiết bị công suất lớn", "Trả phòng đúng hạn"};
        
        for (String rule : rules) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(rule);
            checkBox.setTag(rule);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedRules.add(rule);
                } else {
                    selectedRules.remove(rule);
                }
            });
            layoutRules.addView(checkBox);
        }
    }
    
    private void setupImageRecyclerView() {
        imageAdapter = new SelectedImageAdapter(imageUris, this);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imageAdapter);
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            Gson gson = new Gson();
            currentUser = gson.fromJson(userJson, User.class);
        }
    }
    
    private void loadRoomDetails() {
        showLoading(true);
        android.util.Log.d("EditRoomActivity", "Loading room details for ID: " + roomId);
        
        retrofitClient.getApiService().getRoom(roomId).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                
                android.util.Log.d("EditRoomActivity", "Response code: " + response.code());
                android.util.Log.d("EditRoomActivity", "Response body: " + response.body());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Map<String, Object>> apiResponse = response.body();
                    android.util.Log.d("EditRoomActivity", "API Response success: " + apiResponse.isSuccess());
                    android.util.Log.d("EditRoomActivity", "API Response data: " + apiResponse.getData());
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Map<String, Object> data = apiResponse.getData();
                        if (data.containsKey("room")) {
                            // Parse room from data.room
                            Gson gson = new Gson();
                            String roomJson = gson.toJson(data.get("room"));
                            android.util.Log.d("EditRoomActivity", "Room JSON: " + roomJson);
                            
                            room = gson.fromJson(roomJson, Room.class);
                            android.util.Log.d("EditRoomActivity", "Room loaded successfully: " + (room != null ? room.getTitle() : "null"));
                            populateForm();
                        } else {
                            android.util.Log.e("EditRoomActivity", "No room data in response");
                            showError("Không tìm thấy thông tin phòng");
                        }
                    } else {
                        android.util.Log.e("EditRoomActivity", "API Error: " + apiResponse.getMessage());
                        showError(apiResponse.getMessage());
                    }
                } else {
                    android.util.Log.e("EditRoomActivity", "HTTP Error: " + response.code());
                    showError("Không thể tải thông tin phòng trọ");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối. Vui lòng thử lại.");
            }
        });
    }
    
    private void populateForm() {
        if (room == null) {
            android.util.Log.e("EditRoomActivity", "Room is null, cannot populate form");
            return;
        }
        
        android.util.Log.d("EditRoomActivity", "Populating form with room: " + room.getTitle());
        
        // Basic info
        etTitle.setText(room.getTitle());
        etDescription.setText(room.getDescription());
        etArea.setText(String.valueOf(room.getArea()));
        
        if (room.getPrice() != null) {
            etMonthlyPrice.setText(String.valueOf(room.getPrice().getMonthly()));
            etDeposit.setText(String.valueOf(room.getPrice().getDeposit()));
            
            if (room.getPrice().getUtilities() != null) {
                etElectricity.setText(String.valueOf(room.getPrice().getUtilities().getElectricity()));
                etWater.setText(String.valueOf(room.getPrice().getUtilities().getWater()));
                etInternet.setText(String.valueOf(room.getPrice().getUtilities().getInternet()));
                etOther.setText(String.valueOf(room.getPrice().getUtilities().getOther()));
            }
        }
        
        // Address
        if (room.getAddress() != null) {
            etStreet.setText(room.getAddress().getStreet());
            etWard.setText(room.getAddress().getWard());
            etDistrict.setText(room.getAddress().getDistrict());
            etCity.setText(room.getAddress().getCity());
        }
        
        // Room type
        actvRoomType.setText(room.getRoomType());
        
        // Amenities
        if (room.getAmenities() != null) {
            selectedAmenities.clear();
            selectedAmenities.addAll(room.getAmenities());
            
            for (int i = 0; i < layoutAmenities.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) layoutAmenities.getChildAt(i);
                String amenity = (String) checkBox.getTag();
                checkBox.setChecked(selectedAmenities.contains(amenity));
            }
        }
        
        // Rules
        if (room.getRules() != null) {
            selectedRules.clear();
            selectedRules.addAll(room.getRules());
            
            for (int i = 0; i < layoutRules.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) layoutRules.getChildAt(i);
                String rule = (String) checkBox.getTag();
                checkBox.setChecked(selectedRules.contains(rule));
            }
        }
    }
    
    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh"));
    }
    
    @Override
    public void onImageRemove(int position) {
        imageUris.remove(position);
        imageAdapter.notifyDataSetChanged();
    }
    
    private void updateRoom() {
        if (!validateForm()) return;
        
        showLoading(true);
        
        // Create updated room object
        Room updatedRoom = createRoomFromForm();
        
        String token = "Bearer " + retrofitClient.getToken();
        
        retrofitClient.getApiService().updateRoom(token, roomId, updatedRoom).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Room> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(EditRoomActivity.this, "Cập nhật phòng trọ thành công", Toast.LENGTH_SHORT).show();
                        
                        // Upload new images if any
                        if (!imageUris.isEmpty()) {
                            uploadImages(roomId);
                        } else {
                            finish();
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật phòng trọ");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
    
    private Room createRoomFromForm() {
        Room room = new Room();
        
        // Basic info
        room.setTitle(etTitle.getText().toString().trim());
        room.setDescription(etDescription.getText().toString().trim());
        room.setArea(Double.parseDouble(etArea.getText().toString()));
        room.setRoomType(actvRoomType.getText().toString());
        
        // Price
        Room.Price price = new Room.Price();
        price.setMonthly(Double.parseDouble(etMonthlyPrice.getText().toString()));
        price.setDeposit(Double.parseDouble(etDeposit.getText().toString()));
        
        // Utilities
        Room.Utilities utilities = new Room.Utilities();
        utilities.setElectricity(Double.parseDouble(etElectricity.getText().toString()));
        utilities.setWater(Double.parseDouble(etWater.getText().toString()));
        utilities.setInternet(Double.parseDouble(etInternet.getText().toString()));
        utilities.setOther(Double.parseDouble(etOther.getText().toString()));
        price.setUtilities(utilities);
        
        room.setPrice(price);
        
        // Address
        User.Address address = new User.Address();
        address.setStreet(etStreet.getText().toString().trim());
        address.setWard(etWard.getText().toString().trim());
        address.setDistrict(etDistrict.getText().toString().trim());
        address.setCity(etCity.getText().toString().trim());
        room.setAddress(address);
        
        // Amenities and rules
        room.setAmenities(new ArrayList<>(selectedAmenities));
        room.setRules(new ArrayList<>(selectedRules));
        
        // Landlord
        room.setLandlord(currentUser);
        
        // Status
        room.setStatus("active");
        
        return room;
    }
    
    private boolean validateForm() {
        if (etTitle.getText().toString().trim().isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return false;
        }
        
        if (etDescription.getText().toString().trim().isEmpty()) {
            etDescription.setError("Vui lòng nhập mô tả");
            return false;
        }
        
        if (etArea.getText().toString().trim().isEmpty()) {
            etArea.setError("Vui lòng nhập diện tích");
            return false;
        }
        
        if (etMonthlyPrice.getText().toString().trim().isEmpty()) {
            etMonthlyPrice.setError("Vui lòng nhập giá thuê");
            return false;
        }
        
        if (etDeposit.getText().toString().trim().isEmpty()) {
            etDeposit.setError("Vui lòng nhập tiền cọc");
            return false;
        }
        
        if (actvRoomType.getText().toString().trim().isEmpty()) {
            actvRoomType.setError("Vui lòng chọn loại phòng");
            return false;
        }
        
        return true;
    }
    
    private void uploadImages(String roomId) {
        if (imageUris.isEmpty()) {
            finish();
            return;
        }
        
        // TODO: Implement image upload
        Toast.makeText(this, "Upload ảnh sẽ được thực hiện", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private String getAmenityText(String amenity) {
        switch (amenity) {
            case "wifi": return "WiFi";
            case "air_conditioner": return "Điều hòa";
            case "refrigerator": return "Tủ lạnh";
            case "washing_machine": return "Máy giặt";
            case "hot_water": return "Nước nóng";
            case "desk": return "Bàn làm việc";
            case "wardrobe": return "Tủ quần áo";
            case "balcony": return "Ban công";
            case "parking": return "Chỗ đỗ xe";
            case "kitchen": return "Bếp";
            case "bed": return "Giường";
            case "security": return "An ninh";
            default: return amenity;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_room_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            updateRoom();
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
