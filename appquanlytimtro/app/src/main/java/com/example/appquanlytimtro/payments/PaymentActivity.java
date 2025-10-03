package com.example.appquanlytimtro.payments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.MainActivity;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private String bookingId;
    private double amount;
    private RetrofitClient retrofitClient;

    // Views
    private TextView tvBookingId, tvAmount, tvDescription;
    private MaterialCardView cardVNPay, cardBankTransfer, cardCash;
    private MaterialButton btnVNPay, btnBankTransfer, btnCash;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        retrofitClient = RetrofitClient.getInstance(this);

        initViews();
        setupToolbar();
        setupClickListeners();

        // Get data from intent
        Intent intent = getIntent();
        if (intent != null) {
            bookingId = intent.getStringExtra("booking_id");
            amount = intent.getDoubleExtra("amount", 0);

            if (bookingId != null && amount > 0) {
                displayPaymentInfo();
            } else {
                showError("Thông tin thanh toán không hợp lệ");
                finish();
            }
        }
    }

    private void initViews() {
        tvBookingId = findViewById(R.id.tvBookingId);
        tvAmount = findViewById(R.id.tvAmount);
        tvDescription = findViewById(R.id.tvDescription);
        cardVNPay = findViewById(R.id.cardVNPay);
        cardBankTransfer = findViewById(R.id.cardBankTransfer);
        cardCash = findViewById(R.id.cardCash);
        btnVNPay = findViewById(R.id.btnVNPay);
        btnBankTransfer = findViewById(R.id.btnBankTransfer);
        btnCash = findViewById(R.id.btnCash);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        btnVNPay.setOnClickListener(v -> processVNPayPayment());
        btnBankTransfer.setOnClickListener(v -> processBankTransferPayment());
        btnCash.setOnClickListener(v -> processCashPayment());
    }

    private void displayPaymentInfo() {
        tvBookingId.setText("Mã đặt phòng: " + bookingId);
        
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        tvAmount.setText(formatter.format(amount) + " VNĐ");
        
        tvDescription.setText("Thanh toán cho đặt phòng trọ");
    }

    private void processVNPayPayment() {
        showLoading(true);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("bookingId", bookingId);
        paymentData.put("amount", amount);
        paymentData.put("orderInfo", "Thanh toan dat phong " + bookingId);
        paymentData.put("returnUrl", "vnpay://payment-result");

        String token = "Bearer " + retrofitClient.getToken();

        retrofitClient.getApiService().createVNPayPayment(token, paymentData)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                         Response<ApiResponse<Map<String, Object>>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                Map<String, Object> data = apiResponse.getData();
                                String paymentUrl = (String) data.get("paymentUrl");

                                if (paymentUrl != null) {
                                    // Open VNPay payment URL
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                    startActivity(browserIntent);
                                } else {
                                    showError("Không thể tạo liên kết thanh toán VNPay");
                                }
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Không thể tạo thanh toán VNPay");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void processBankTransferPayment() {
        showLoading(true);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("bookingId", bookingId);
        paymentData.put("amount", amount);
        paymentData.put("paymentMethod", "bank_transfer");

        String token = "Bearer " + retrofitClient.getToken();

        retrofitClient.getApiService().createBankTransferPayment(token, paymentData)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Map<String, Object>>> call, 
                                         Response<ApiResponse<Map<String, Object>>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Map<String, Object>> apiResponse = response.body();

                            if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                                Map<String, Object> data = apiResponse.getData();
                                
                                // Show bank transfer information
                                showBankTransferInfo(data);
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Không thể tạo thanh toán chuyển khoản");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                        showLoading(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void processCashPayment() {
        // Show cash payment instructions
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thanh toán tiền mặt")
                .setMessage("Vui lòng liên hệ trực tiếp với chủ trọ để thanh toán bằng tiền mặt. " +
                           "Sau khi thanh toán, chủ trọ sẽ xác nhận đặt phòng của bạn.")
                .setPositiveButton("Đã hiểu", (dialog, which) -> {
                    // Navigate back to main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBankTransferInfo(Map<String, Object> data) {
        String bankName = (String) data.get("bankName");
        String accountNumber = (String) data.get("accountNumber");
        String accountName = (String) data.get("accountName");
        String transferContent = (String) data.get("transferContent");

        String message = "Thông tin chuyển khoản:\n\n" +
                        "Ngân hàng: " + (bankName != null ? bankName : "Vietcombank") + "\n" +
                        "Số tài khoản: " + (accountNumber != null ? accountNumber : "1234567890") + "\n" +
                        "Tên tài khoản: " + (accountName != null ? accountName : "CONG TY TNHH QUAN LY TIM TRO") + "\n" +
                        "Nội dung: " + (transferContent != null ? transferContent : bookingId) + "\n" +
                        "Số tiền: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(amount) + " VNĐ\n\n" +
                        "Vui lòng chuyển khoản đúng nội dung để hệ thống tự động xác nhận thanh toán.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Thông tin chuyển khoản")
                .setMessage(message)
                .setPositiveButton("Đã chuyển khoản", (dialog, which) -> {
                    // Navigate back to main activity
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Sao chép thông tin", (dialog, which) -> {
                    // Copy to clipboard
                    android.content.ClipboardManager clipboard = 
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Bank Transfer Info", message);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Đã sao chép thông tin chuyển khoản", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVNPay.setEnabled(!show);
        btnBankTransfer.setEnabled(!show);
        btnCash.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        // Handle VNPay return result
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            if ("vnpay".equals(data.getScheme())) {
                handleVNPayResult(data);
            }
        }
    }

    private void handleVNPayResult(Uri data) {
        String responseCode = data.getQueryParameter("vnp_ResponseCode");
        
        if ("00".equals(responseCode)) {
            // Payment successful
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            
            // Navigate back to main activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // Payment failed
            Toast.makeText(this, "Thanh toán thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        }
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

