package com.example.appquanlytimtro.rooms;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.adapters.RoomImageAdapter;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailActivity extends AppCompatActivity {
    
    private String roomId;
    private Room room;
    private RetrofitClient retrofitClient;
    
    private ViewPager2 viewPagerImages;
    private TextView tvTitle, tvAddress, tvPrice, tvArea, tvRoomType, tvDescription;
    private TextView tvContactInfo, tvRating, tvViews;
    private LinearLayout layoutAmenities, layoutRules;
    private Button btnBook, btnLike, btnContact;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
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
        }
    }
    
    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvTitle = findViewById(R.id.tvTitle);
        tvAddress = findViewById(R.id.tvAddress);
        tvPrice = findViewById(R.id.tvPrice);
        tvArea = findViewById(R.id.chipArea);
        tvRoomType = findViewById(R.id.chipRoomType);
        tvDescription = findViewById(R.id.tvDescription);
        tvContactInfo = findViewById(R.id.tvContactInfo);
        tvRating = findViewById(R.id.tvRating);
        tvViews = findViewById(R.id.tvViews);
        layoutAmenities = findViewById(R.id.layoutAmenities);
        layoutRules = findViewById(R.id.layoutRules);
        btnBook = findViewById(R.id.btnBook);
        btnLike = findViewById(R.id.btnLike);
        btnContact = findViewById(R.id.btnContact);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết phòng trọ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void loadRoomDetails() {
        showLoading(true);
        
        retrofitClient.getApiService().getRoom(roomId).enqueue(new Callback<ApiResponse<Room>>() {
            @Override
            public void onResponse(Call<ApiResponse<Room>> call, Response<ApiResponse<Room>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Room> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        room = apiResponse.getData();
                        displayRoomDetails();
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải thông tin phòng trọ");
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Room>> call, Throwable t) {
                showLoading(false);
                showError("Lỗi kết nối. Vui lòng thử lại.");
            }
        });
    }
    
    private void displayRoomDetails() {
        if (room == null) return;
        
        // Set basic info
        tvTitle.setText(room.getTitle());
        
        if (room.getAddress() != null) {
            String address = room.getAddress().getStreet() + ", " + 
                           room.getAddress().getWard() + ", " + 
                           room.getAddress().getDistrict() + ", " + 
                           room.getAddress().getCity();
            tvAddress.setText(address);
        }
        
        if (room.getPrice() != null) {
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            String price = formatter.format(room.getPrice().getMonthly()) + " VNĐ/tháng";
            tvPrice.setText(price);
        }
        
        tvArea.setText(room.getArea() + " m²");
        tvRoomType.setText(getRoomTypeText(room.getRoomType()));
        tvDescription.setText(room.getDescription());
        
        if (room.getRating() != null) {
            tvRating.setText(String.format("Đánh giá: %.1f/5.0 (%d đánh giá)", 
                    room.getRating().getAverage(), room.getRating().getCount()));
        }
        
        tvViews.setText("Lượt xem: " + room.getViews());
        
        // Set images
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (Room.RoomImage image : room.getImages()) {
                String imageUrl = image.getUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "http://10.0.2.2:5000" + imageUrl;
                    }
                    imageUrls.add(imageUrl);
                }
            }
            
            if (!imageUrls.isEmpty()) {
                RoomImageAdapter adapter = new RoomImageAdapter(imageUrls);
                viewPagerImages.setAdapter(adapter);
            }
        }
        
        // Set amenities
        if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
            layoutAmenities.removeAllViews();
            for (String amenity : room.getAmenities()) {
                TextView textView = new TextView(this);
                textView.setText("• " + amenity);
                textView.setTextSize(14);
                textView.setPadding(0, 4, 0, 4);
                layoutAmenities.addView(textView);
            }
        }
        
        // Set rules
        if (room.getRules() != null && !room.getRules().isEmpty()) {
            layoutRules.removeAllViews();
            for (String rule : room.getRules()) {
                TextView textView = new TextView(this);
                textView.setText("• " + rule);
                textView.setTextSize(14);
                textView.setPadding(0, 4, 0, 4);
                layoutRules.addView(textView);
            }
        }
        
        // Set contact info
        if (room.getContactInfo() != null) {
            String contact = "Liên hệ: " + room.getContactInfo().getPhone();
            if (room.getContactInfo().getEmail() != null) {
                contact += " | " + room.getContactInfo().getEmail();
            }
            tvContactInfo.setText(contact);
        }
        
        // Setup click listeners
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        btnBook.setOnClickListener(v -> {
            // Navigate to booking activity
            Toast.makeText(this, "Chức năng đặt phòng sẽ được phát triển", Toast.LENGTH_SHORT).show();
        });
        
        btnLike.setOnClickListener(v -> {
            if (room != null) {
                toggleLike();
            }
        });
        
        btnContact.setOnClickListener(v -> {
            if (room != null && room.getContactInfo() != null) {
                // Open contact options
                Toast.makeText(this, "Chức năng liên hệ sẽ được phát triển", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void toggleLike() {
        retrofitClient.getApiService().toggleRoomLike(retrofitClient.getToken(), room.getId())
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Map<String, Object>> apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Map<String, Object> data = apiResponse.getData();
                                boolean isLiked = (Boolean) data.get("isLiked");
                                int likesCount = ((Double) data.get("likesCount")).intValue();
                                
                                btnLike.setText(isLiked ? "Đã thích" : "Thích");
                                Toast.makeText(RoomDetailActivity.this, 
                                        isLiked ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích", 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        // Handle error silently
                    }
                });
    }
    
    private String getRoomTypeText(String roomType) {
        switch (roomType) {
            case "studio":
                return "Studio";
            case "1_bedroom":
                return "1 phòng ngủ";
            case "2_bedroom":
                return "2 phòng ngủ";
            case "3_bedroom":
                return "3 phòng ngủ";
            case "shared":
                return "Phòng chung";
            default:
                return roomType;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_detail_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_share) {
            // Share room details
            Toast.makeText(this, "Chức năng chia sẻ sẽ được phát triển", Toast.LENGTH_SHORT).show();
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
