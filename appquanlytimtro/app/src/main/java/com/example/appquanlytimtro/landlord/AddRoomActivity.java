package com.example.appquanlytimtro.landlord;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.ImagePreviewAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRoomActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etCity, etDistrict, etWard, etStreet;
    private TextInputEditText etArea, etPrice, etDeposit, etElectricPrice, etWaterPrice;
    private AutoCompleteTextView spinnerRoomType;
    private LinearLayout layoutAmenities, layoutRules;
    private MaterialButton btnSubmit, btnPickImages;
    private RecyclerView recyclerViewImages;
    private ProgressBar progressBar;
    
    private final List<Uri> imageUris = new ArrayList<>();
    private ImagePreviewAdapter imageAdapter;
    private ActivityResultLauncher<Intent> pickImagesLauncher;
    private RetrofitClient retrofitClient;

    // Amenities and rules
    private final String[] amenitiesList = {
        "WiFi", "Điều hòa", "Tủ lạnh", "Máy giặt", "Bàn làm việc", 
        "Tủ quần áo", "Giường", "Bếp", "Nóng lạnh", "Ban công",
        "Thang máy", "Bảo vệ 24/7", "Chỗ để xe", "Camera an ninh"
    };
    
    private final String[] rulesList = {
        "Không hút thuốc", "Không nuôi thú cưng", "Không ồn ào sau 22h",
        "Không tổ chức tiệc tùng", "Giữ gìn vệ sinh chung", "Trả phòng đúng hạn",
        "Không sử dụng thiết bị công suất lớn", "Khách không được ở qua đêm"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        initViews();
        setupToolbar();
        setupSpinners();
        setupAmenities();
        setupRules();
        setupImagePicker();
        setupSubmitButton();
    }
    
    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etCity = findViewById(R.id.etCity);
        etDistrict = findViewById(R.id.etDistrict);
        etWard = findViewById(R.id.etWard);
        etStreet = findViewById(R.id.etStreet);
        etArea = findViewById(R.id.etArea);
        etPrice = findViewById(R.id.etPrice);
        etDeposit = findViewById(R.id.etDeposit);
        etElectricPrice = findViewById(R.id.etElectricPrice);
        etWaterPrice = findViewById(R.id.etWaterPrice);
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        layoutAmenities = findViewById(R.id.layoutAmenities);
        layoutRules = findViewById(R.id.layoutRules);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnPickImages = findViewById(R.id.btnPickImages);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        progressBar = findViewById(R.id.progressBar);
        
        // Setup image recycler view
        imageAdapter = new ImagePreviewAdapter(imageUris, this::removeImage);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewImages.setAdapter(imageAdapter);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupSpinners() {
        String[] roomTypes = {"Phòng trọ", "Chung cư mini", "Nhà nguyên căn", "Homestay", "Ký túc xá"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roomTypes);
        spinnerRoomType.setAdapter(adapter);
    }
    
    private String mapRoomTypeToBackend(String displayType) {
        switch (displayType) {
            case "Phòng trọ":
                return "studio";
            case "Chung cư mini":
                return "1bedroom";
            case "Nhà nguyên căn":
                return "2bedroom";
            case "Homestay":
                return "3bedroom";
            case "Ký túc xá":
                return "shared";
            default:
                return "studio";
        }
    }
    
    private String mapAmenityToBackend(String displayAmenity) {
        switch (displayAmenity) {
            case "WiFi":
                return "wifi";
            case "Điều hòa":
                return "air_conditioner";
            case "Tủ lạnh":
                return "refrigerator";
            case "Máy giặt":
                return "washing_machine";
            case "Bàn làm việc":
                return "desk";
            case "Tủ quần áo":
                return "wardrobe";
            case "Giường":
                return "bed";
            case "Bếp":
                return "kitchen";
            case "Nóng lạnh":
                return "hot_water";
            case "Ban công":
                return "balcony";
            case "Thang máy":
                return "elevator";
            case "Bảo vệ 24/7":
                return "security";
            case "Chỗ để xe":
                return "parking";
            case "Camera an ninh":
                return "security";
            default:
                return displayAmenity.toLowerCase().replace(" ", "_");
        }
    }
    
    private void setupAmenities() {
        for (String amenity : amenitiesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(amenity);
            checkBox.setTag(amenity);
            layoutAmenities.addView(checkBox);
        }
    }
    
    private void setupRules() {
        for (String rule : rulesList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(rule);
            checkBox.setTag(rule);
            layoutRules.addView(checkBox);
        }
    }
    
    private void setupImagePicker() {
        pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.getClipData() != null) {
                        // Multiple images selected
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = data.getClipData().getItemAt(i).getUri();
                            imageUris.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        // Single image selected
                        imageUris.add(data.getData());
                    }
                    imageAdapter.notifyDataSetChanged();
                    android.util.Log.d("AddRoomActivity", "Images selected: " + imageUris.size());
                    android.util.Log.d("AddRoomActivity", "Selected image URIs: " + imageUris.toString());
                    Toast.makeText(this, "Đã chọn " + imageUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        btnPickImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            pickImagesLauncher.launch(Intent.createChooser(intent, "Chọn ảnh phòng"));
        });
    }
    
    private void removeImage(int position) {
        if (position >= 0 && position < imageUris.size()) {
            imageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
        }
    }
    
    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                createRoom();
            }
        });
    }
    
    private boolean validateInput() {
        String title = getText(etTitle);
        String city = getText(etCity);
        String priceStr = getText(etPrice);
        
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            etTitle.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(city)) {
            etCity.setError("Vui lòng nhập thành phố");
            etCity.requestFocus();
            return false;
        }
        
        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Vui lòng nhập giá thuê");
            etPrice.requestFocus();
            return false;
        }
        
        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Giá thuê không hợp lệ");
            etPrice.requestFocus();
            return false;
        }
        
        // Images are optional - no validation needed
        
        return true;
    }
    
    private void createRoom() {
        showLoading(true);
        
        // Debug: Log số lượng ảnh trước khi tạo phòng
        android.util.Log.d("AddRoomActivity", "=== CREATE ROOM DEBUG ===");
        android.util.Log.d("AddRoomActivity", "Number of images before creating room: " + imageUris.size());
        android.util.Log.d("AddRoomActivity", "Image URIs before creating room: " + imageUris.toString());
        
        Room room = new Room();
        room.setTitle(getText(etTitle));
        room.setDescription(getText(etDescription));
        room.setRoomType(mapRoomTypeToBackend(spinnerRoomType.getText().toString()));
        
        // Address
        User.Address address = new User.Address();
        address.setCity(getText(etCity));
        address.setDistrict(getText(etDistrict));
        address.setWard(getText(etWard));
        address.setStreet(getText(etStreet));
        room.setAddress(address);
        
        // Area
        String areaStr = getText(etArea);
        if (!TextUtils.isEmpty(areaStr)) {
            try {
                room.setArea(Double.parseDouble(areaStr));
            } catch (NumberFormatException e) {
                room.setArea(0);
            }
        }
        
        // Price
        Room.Price price = new Room.Price();
        try {
            price.setMonthly(Double.parseDouble(getText(etPrice)));
        } catch (NumberFormatException e) {
            price.setMonthly(0);
        }
        
        String depositStr = getText(etDeposit);
        if (!TextUtils.isEmpty(depositStr)) {
            try {
                price.setDeposit(Double.parseDouble(depositStr));
            } catch (NumberFormatException e) {
                price.setDeposit(0);
            }
        }
        
        // Set utilities - create utilities object
        Room.Utilities utilities = new Room.Utilities();
        String electricStr = getText(etElectricPrice);
        if (!TextUtils.isEmpty(electricStr)) {
            try {
                utilities.setElectricity(Double.parseDouble(electricStr));
            } catch (NumberFormatException e) {
                utilities.setElectricity(0);
            }
        } else {
            utilities.setElectricity(0);
        }
        
        String waterStr = getText(etWaterPrice);
        if (!TextUtils.isEmpty(waterStr)) {
            try {
                utilities.setWater(Double.parseDouble(waterStr));
            } catch (NumberFormatException e) {
                utilities.setWater(0);
            }
        } else {
            utilities.setWater(0);
        }
        
        price.setUtilities(utilities);
        
        room.setPrice(price);
        
        // Amenities
        List<String> selectedAmenities = new ArrayList<>();
        for (int i = 0; i < layoutAmenities.getChildCount(); i++) {
            View child = layoutAmenities.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    String displayAmenity = checkBox.getTag().toString();
                    String backendAmenity = mapAmenityToBackend(displayAmenity);
                    selectedAmenities.add(backendAmenity);
                }
            }
        }
        room.setAmenities(selectedAmenities);
        
        // Rules
        List<String> selectedRules = new ArrayList<>();
        for (int i = 0; i < layoutRules.getChildCount(); i++) {
            View child = layoutRules.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                if (checkBox.isChecked()) {
                    selectedRules.add(checkBox.getTag().toString());
                }
            }
        }
        room.setRules(selectedRules);
        
        // Set availability
        Room.Availability availability = new Room.Availability();
        availability.setAvailable(true);
        room.setAvailability(availability);
        
        // Set status
        room.setStatus("active");
        
        // Set landlord (current user)
        String userJson = retrofitClient.getUserData();
        if (userJson != null) {
            try {
                com.google.gson.Gson gson = new com.google.gson.Gson();
                User currentUser = gson.fromJson(userJson, User.class);
                room.setLandlord(currentUser);
                android.util.Log.d("AddRoomActivity", "Set landlord: " + (currentUser != null ? currentUser.getFullName() : "null"));
            } catch (Exception e) {
                android.util.Log.e("AddRoomActivity", "Error parsing user data: " + e.getMessage());
            }
        }
        
        // Set contact info
        Room.ContactInfo contactInfo = new Room.ContactInfo();
        if (room.getLandlord() != null) {
            contactInfo.setPhone(room.getLandlord().getPhone());
            contactInfo.setEmail(room.getLandlord().getEmail());
        }
        room.setContactInfo(contactInfo);
        
        // Log room data before sending
        android.util.Log.d("AddRoomActivity", "Creating room with data:");
        android.util.Log.d("AddRoomActivity", "Title: " + room.getTitle());
        android.util.Log.d("AddRoomActivity", "Description: " + room.getDescription());
        android.util.Log.d("AddRoomActivity", "Address: " + room.getAddress());
        android.util.Log.d("AddRoomActivity", "Area: " + room.getArea());
        android.util.Log.d("AddRoomActivity", "Monthly Price: " + (room.getPrice() != null ? room.getPrice().getMonthly() : "null"));
        android.util.Log.d("AddRoomActivity", "Deposit: " + (room.getPrice() != null ? room.getPrice().getDeposit() : "null"));
        android.util.Log.d("AddRoomActivity", "Utilities: " + (room.getPrice() != null && room.getPrice().getUtilities() != null ? 
            "Electricity: " + room.getPrice().getUtilities().getElectricity() + 
            ", Water: " + room.getPrice().getUtilities().getWater() + 
            ", Internet: " + room.getPrice().getUtilities().getInternet() + 
            ", Other: " + room.getPrice().getUtilities().getOther() : "null"));
        android.util.Log.d("AddRoomActivity", "Amenities: " + room.getAmenities());
        android.util.Log.d("AddRoomActivity", "Rules: " + room.getRules());
        
        String token = "Bearer " + retrofitClient.getToken();
        android.util.Log.d("AddRoomActivity", "Token: " + token);
        
        retrofitClient.getApiService().createRoom(token, room).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                android.util.Log.d("AddRoomActivity", "Response code: " + response.code());
                android.util.Log.d("AddRoomActivity", "Response body: " + response.body());
                
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        android.util.Log.d("AddRoomActivity", "Response success: " + response.body().isSuccess());
                        android.util.Log.d("AddRoomActivity", "Response message: " + response.body().getMessage());
                        
                        if (response.body().isSuccess()) {
                            // Backend returns data.room object
                            Map<String, Object> responseData = response.body().getData();
                            android.util.Log.d("AddRoomActivity", "Response data: " + responseData);
                            
                            Room createdRoom = null;
                            
                            if (responseData != null && responseData.containsKey("room")) {
                                android.util.Log.d("AddRoomActivity", "Found room in response data");
                                // Parse Room object from response data
                                com.google.gson.Gson gson = new com.google.gson.Gson();
                                Object roomData = responseData.get("room");
                                android.util.Log.d("AddRoomActivity", "Room data type: " + (roomData != null ? roomData.getClass().getSimpleName() : "null"));
                                android.util.Log.d("AddRoomActivity", "Room data: " + roomData);
                                
                                String roomJson = gson.toJson(roomData);
                                android.util.Log.d("AddRoomActivity", "Room JSON: " + roomJson);
                                
                                // Try parsing with Gson first
                                createdRoom = gson.fromJson(roomJson, Room.class);
                                android.util.Log.d("AddRoomActivity", "Parsed room ID: " + createdRoom.getId());
                                android.util.Log.d("AddRoomActivity", "Parsed room title: " + createdRoom.getTitle());
                                
                                // If ID is still null, try manual parsing
                                if (createdRoom.getId() == null && roomData instanceof Map) {
                                    android.util.Log.d("AddRoomActivity", "ID is null, trying manual parsing");
                                    Map<String, Object> roomMap = (Map<String, Object>) roomData;
                                    if (roomMap.containsKey("_id")) {
                                        String roomId = (String) roomMap.get("_id");
                                        android.util.Log.d("AddRoomActivity", "Manual parsed ID: " + roomId);
                                        createdRoom.setId(roomId);
                                    }
                                }
                                
                                // Debug: Check if _id field exists in JSON
                                if (roomJson.contains("\"_id\"")) {
                                    android.util.Log.d("AddRoomActivity", "_id field found in JSON");
                                } else {
                                    android.util.Log.e("AddRoomActivity", "_id field NOT found in JSON");
                                }
                            } else {
                                android.util.Log.e("AddRoomActivity", "No room found in response data or responseData is null");
                            }
                            
                            android.util.Log.d("AddRoomActivity", "Created room ID: " + (createdRoom != null ? createdRoom.getId() : "null"));
                            android.util.Log.d("AddRoomActivity", "Number of images selected: " + imageUris.size());
                            android.util.Log.d("AddRoomActivity", "Image URIs: " + imageUris.toString());
                            
                            if (createdRoom != null && createdRoom.getId() != null) {
                                if (!imageUris.isEmpty()) {
                                    android.util.Log.d("AddRoomActivity", "Room created successfully, uploading images...");
                                    android.util.Log.d("AddRoomActivity", "Starting image upload for room: " + createdRoom.getId());
                                    uploadImages(createdRoom.getId());
                                } else {
                                    android.util.Log.d("AddRoomActivity", "No images to upload, finishing...");
                                    showLoading(false);
                                    Toast.makeText(AddRoomActivity.this, "Tạo phòng thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } else {
                                showLoading(false);
                                if (createdRoom == null) {
                                    android.util.Log.e("AddRoomActivity", "Created room is null");
                                } else if (createdRoom.getId() == null) {
                                    android.util.Log.e("AddRoomActivity", "Created room ID is null");
                                }
                                Toast.makeText(AddRoomActivity.this, "Tạo phòng thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            showLoading(false);
                            String errorMsg = response.body().getMessage() != null ? response.body().getMessage() : "Tạo phòng thất bại";
                            android.util.Log.e("AddRoomActivity", "API Error: " + errorMsg);
                            Toast.makeText(AddRoomActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        showLoading(false);
                        android.util.Log.e("AddRoomActivity", "Response body is null");
                        Toast.makeText(AddRoomActivity.this, "Tạo phòng thất bại - Response body null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    android.util.Log.e("AddRoomActivity", "HTTP Error " + response.code() + ": " + errorBody);
                    Toast.makeText(AddRoomActivity.this, "Lỗi HTTP " + response.code() + ": " + errorBody, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                android.util.Log.e("AddRoomActivity", "Network error: " + t.getMessage(), t);
                Toast.makeText(AddRoomActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void uploadImages(String roomId) {
        android.util.Log.d("AddRoomActivity", "=== UPLOAD IMAGES START ===");
        android.util.Log.d("AddRoomActivity", "Room ID: " + roomId);
        android.util.Log.d("AddRoomActivity", "Number of images: " + imageUris.size());
        
        // Validate roomId
        if (roomId == null || roomId.trim().isEmpty()) {
            android.util.Log.e("AddRoomActivity", "Room ID is null or empty, cannot upload images");
            showLoading(false);
            Toast.makeText(this, "Lỗi: ID phòng không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate images
        if (imageUris.isEmpty()) {
            android.util.Log.d("AddRoomActivity", "No images to upload");
            showLoading(false);
            Toast.makeText(this, "Không có ảnh để upload", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<MultipartBody.Part> parts = new ArrayList<>();
        
        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            try {
                android.util.Log.d("AddRoomActivity", "Processing image " + (i + 1) + ": " + uri.toString());
                
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    android.util.Log.e("AddRoomActivity", "Cannot open input stream for image " + (i + 1));
                    continue;
                }
                
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                byte[] bytes = buffer.toByteArray();
                
                android.util.Log.d("AddRoomActivity", "Image " + (i + 1) + " size: " + bytes.length + " bytes");
                
                if (bytes.length == 0) {
                    android.util.Log.e("AddRoomActivity", "Image " + (i + 1) + " is empty");
                    continue;
                }
                
                RequestBody requestBody = RequestBody.create(bytes, MediaType.parse("image/*"));
                MultipartBody.Part part = MultipartBody.Part.createFormData("images", "image" + (i + 1) + ".jpg", requestBody);
                parts.add(part);
                
                inputStream.close();
                android.util.Log.d("AddRoomActivity", "Successfully processed image " + (i + 1));
            } catch (Exception e) {
                android.util.Log.e("AddRoomActivity", "Error processing image " + (i + 1), e);
                e.printStackTrace();
            }
        }
        
        android.util.Log.d("AddRoomActivity", "Total parts created: " + parts.size());
        
        String token = "Bearer " + retrofitClient.getToken();
        android.util.Log.d("AddRoomActivity", "Token: " + token);
        android.util.Log.d("AddRoomActivity", "Uploading images to API...");
        
        retrofitClient.getApiService().uploadRoomImages(token, roomId, parts).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                android.util.Log.d("AddRoomActivity", "=== UPLOAD IMAGES RESPONSE ===");
                android.util.Log.d("AddRoomActivity", "Response code: " + response.code());
                android.util.Log.d("AddRoomActivity", "Response body: " + response.body());
                
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    android.util.Log.d("AddRoomActivity", "Images uploaded successfully!");
                    Toast.makeText(AddRoomActivity.this, "Tạo phòng và upload ảnh thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Upload ảnh thất bại";
                    android.util.Log.e("AddRoomActivity", "Upload failed: " + errorMsg);
                    Toast.makeText(AddRoomActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                android.util.Log.e("AddRoomActivity", "Upload images network error: " + t.getMessage(), t);
                showLoading(false);
                Toast.makeText(AddRoomActivity.this, "Lỗi upload ảnh: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
        btnPickImages.setEnabled(!show);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
