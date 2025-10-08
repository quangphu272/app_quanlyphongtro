package com.example.appquanlytimtro.payments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.config.VNPayConfig;

public class VNPayWebViewActivity extends AppCompatActivity {

    private static final String TAG = "VNPayWebViewActivity";
    
    private WebView webView;
    private ProgressBar progressBar;
    private String paymentUrl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_webview);
        
        initViews();
        setupToolbar();
        setupWebView();
        loadPaymentUrl();
    }
    
    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thanh toán VNPay");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        
        // Set WebViewClient to handle URL navigation
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, "Loading URL: " + url);
                
                // Check if this is a return URL from VNPay
                if (url.startsWith(VNPayConfig.VNPAY_RETURN_URL)) {
                    Log.d(TAG, "VNPay return URL detected: " + url);
                    handleVNPayReturn(url);
                    return true;
                }
                
                // Allow normal navigation
                return false;
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "Page finished loading: " + url);
            }
        });
        
        // Set WebChromeClient for progress updates
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    
    private void loadPaymentUrl() {
        // Get payment URL from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("payment_url")) {
            paymentUrl = intent.getStringExtra("payment_url");
            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                Log.d(TAG, "Loading payment URL: " + paymentUrl);
                webView.loadUrl(paymentUrl);
            } else {
                showError("URL thanh toán không hợp lệ");
            }
        } else {
            showError("Không tìm thấy URL thanh toán");
        }
    }
    
    private void handleVNPayReturn(String returnUrl) {
        Log.d(TAG, "Handling VNPay return: " + returnUrl);
        
        try {
            Uri uri = Uri.parse(returnUrl);
            VNPayService.PaymentResponse response = VNPayService.handlePaymentResult(uri);
            
            // Create result intent
            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_success", response.isSuccess());
            resultIntent.putExtra("payment_message", response.getMessage());
            resultIntent.putExtra("transaction_id", response.getTransactionId());
            resultIntent.putExtra("order_id", response.getOrderId());
            resultIntent.putExtra("amount", response.getAmount());
            
            setResult(RESULT_OK, resultIntent);
            finish();
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling VNPay return: " + e.getMessage(), e);
            showError("Lỗi xử lý kết quả thanh toán");
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
