package com.example.appquanlytimtro.payments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.appquanlytimtro.config.VNPayConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class VNPayService {
    
    private static final String TAG = "VNPayService";
    
    // VNPay configuration - sử dụng VNPayConfig
    private static final String VNPAY_URL = VNPayConfig.VNPAY_URL;
    private static final String VNPAY_RETURN_URL = VNPayConfig.VNPAY_RETURN_URL;
    private static final String VNPAY_TMN_CODE = VNPayConfig.VNPAY_TMN_CODE;
    private static final String VNPAY_HASH_SECRET = VNPayConfig.VNPAY_HASH_SECRET;
    
    // VNPay parameters
    private static final String VERSION = VNPayConfig.VERSION;
    private static final String CURRENCY_CODE = VNPayConfig.CURRENCY_CODE;
    private static final String LOCALE = VNPayConfig.LOCALE;
    private static final String COMMAND = VNPayConfig.COMMAND;
    
    public static class PaymentRequest {
        private String orderId;
        private String orderInfo;
        private long amount;
        private String returnUrl;
        private String ipAddress;
        private Date createDate;
        
        public PaymentRequest(String orderId, String orderInfo, long amount, String ipAddress) {
            this.orderId = orderId;
            this.orderInfo = orderInfo;
            this.amount = amount;
            this.returnUrl = VNPAY_RETURN_URL;
            this.ipAddress = ipAddress;
            this.createDate = new Date();
        }
        
        // Getters
        public String getOrderId() { return orderId; }
        public String getOrderInfo() { return orderInfo; }
        public long getAmount() { return amount; }
        public String getReturnUrl() { return returnUrl; }
        public String getIpAddress() { return ipAddress; }
        public Date getCreateDate() { return createDate; }
    }
    
    public static class PaymentResponse {
        private boolean success;
        private String message;
        private String transactionId;
        private String orderId;
        private long amount;
        private String responseCode;
        
        public PaymentResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public long getAmount() { return amount; }
        public void setAmount(long amount) { this.amount = amount; }
        public String getResponseCode() { return responseCode; }
        public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    }
    
    /**
     * Tạo URL thanh toán VNPay
     */
    public static String createPaymentUrl(PaymentRequest request) {
        try {
            Map<String, String> vnpParams = new HashMap<>();
            
            // Required parameters
            vnpParams.put("vnp_Version", VERSION);
            vnpParams.put("vnp_Command", COMMAND);
            vnpParams.put("vnp_TmnCode", VNPAY_TMN_CODE);
            vnpParams.put("vnp_Amount", String.valueOf(request.getAmount() * 100)); // VNPay requires amount in cents
            vnpParams.put("vnp_CurrCode", CURRENCY_CODE);
            vnpParams.put("vnp_TxnRef", request.getOrderId());
            vnpParams.put("vnp_OrderInfo", request.getOrderInfo());
            vnpParams.put("vnp_OrderType", "other");
            vnpParams.put("vnp_Locale", LOCALE);
            vnpParams.put("vnp_ReturnUrl", request.getReturnUrl());
            vnpParams.put("vnp_IpAddr", request.getIpAddress());
            
            // Date format: yyyyMMddHHmmss
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            vnpParams.put("vnp_CreateDate", dateFormat.format(request.getCreateDate()));
            
            // Expire date (15 minutes from now)
            Calendar expireDate = Calendar.getInstance();
            expireDate.add(Calendar.MINUTE, 15);
            vnpParams.put("vnp_ExpireDate", dateFormat.format(expireDate.getTime()));
            
            // Sort parameters and create query string
            String queryString = createQueryString(vnpParams);
            
            // Create secure hash
            String secureHash = hmacSHA512(VNPAY_HASH_SECRET, queryString);
            Log.d(TAG, "Query string for hash: " + queryString);
            Log.d(TAG, "Generated secure hash: " + secureHash);
            vnpParams.put("vnp_SecureHash", secureHash);
            
            // Create final query string
            String finalQueryString = createQueryString(vnpParams);
            Log.d(TAG, "Final payment URL: " + VNPAY_URL + "?" + finalQueryString);
            
            return VNPAY_URL + "?" + finalQueryString;
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating payment URL: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Xử lý kết quả thanh toán từ VNPay
     */
    public static PaymentResponse handlePaymentResult(Uri uri) {
        try {
            Map<String, String> params = new HashMap<>();
            
            // Parse query parameters
            for (String param : uri.getQuery().split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
            
            String responseCode = params.get("vnp_ResponseCode");
            String transactionId = params.get("vnp_TransactionNo");
            String orderId = params.get("vnp_TxnRef");
            String amount = params.get("vnp_Amount");
            String secureHash = params.get("vnp_SecureHash");
            
            // Verify secure hash
            if (!verifySecureHash(params, secureHash)) {
                return new PaymentResponse(false, "Mã bảo mật không hợp lệ");
            }
            
            PaymentResponse response = new PaymentResponse(false, "Thanh toán thất bại");
            response.setTransactionId(transactionId);
            response.setOrderId(orderId);
            response.setResponseCode(responseCode);
            
            if (amount != null) {
                response.setAmount(Long.parseLong(amount) / 100); // Convert from cents
            }
            
            // Check response code
            if ("00".equals(responseCode)) {
                response = new PaymentResponse(true, "Thanh toán thành công");
                response.setTransactionId(transactionId);
                response.setOrderId(orderId);
                response.setResponseCode(responseCode);
                if (amount != null) {
                    response.setAmount(Long.parseLong(amount) / 100);
                }
            } else {
                String errorMessage = getErrorMessage(responseCode);
                response = new PaymentResponse(false, errorMessage);
                response.setTransactionId(transactionId);
                response.setOrderId(orderId);
                response.setResponseCode(responseCode);
                if (amount != null) {
                    response.setAmount(Long.parseLong(amount) / 100);
                }
            }
            
            return response;
            
        } catch (Exception e) {
            Log.e(TAG, "Error handling payment result: " + e.getMessage(), e);
            return new PaymentResponse(false, "Lỗi xử lý kết quả thanh toán");
        }
    }
    
    /**
     * Mở trình duyệt để thanh toán
     */
    public static void openPayment(Context context, PaymentRequest request) {
        String paymentUrl = createPaymentUrl(request);
        if (paymentUrl != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
            context.startActivity(intent);
        } else {
            Log.e(TAG, "Failed to create payment URL");
        }
    }
    
    /**
     * Tạo query string từ parameters
     */
    private static String createQueryString(Map<String, String> params) {
        List<String> paramNames = new ArrayList<>(params.keySet());
        Collections.sort(paramNames);
        
        StringBuilder queryString = new StringBuilder();
        Iterator<String> iterator = paramNames.iterator();
        
        while (iterator.hasNext()) {
            String paramName = iterator.next();
            String paramValue = params.get(paramName);
            
            if (paramValue != null && !paramValue.isEmpty()) {
                queryString.append(paramName).append("=").append(paramValue);
                if (iterator.hasNext()) {
                    queryString.append("&");
                }
            }
        }
        
        return queryString.toString();
    }
    
    /**
     * Tạo HMAC SHA512 hash - sử dụng thuật toán chuẩn
     */
    private static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating HMAC SHA512: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Verify secure hash
     */
    private static boolean verifySecureHash(Map<String, String> params, String receivedHash) {
        if (receivedHash == null || receivedHash.isEmpty()) {
            return false;
        }
        
        // Remove vnp_SecureHash from params
        Map<String, String> paramsWithoutHash = new HashMap<>(params);
        paramsWithoutHash.remove("vnp_SecureHash");
        
        // Create query string and hash
        String queryString = createQueryString(paramsWithoutHash);
        String calculatedHash = hmacSHA512(VNPAY_HASH_SECRET, queryString);
        
        return receivedHash.equals(calculatedHash);
    }
    
    /**
     * Get error message from response code
     */
    private static String getErrorMessage(String responseCode) {
        switch (responseCode) {
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking của ngân hàng.";
            case "10":
                return "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Đã hết hạn chờ thanh toán. Xin vui lòng thực hiện lại giao dịch.";
            case "12":
                return "Giao dịch bị hủy.";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì.";
            case "79":
                return "Nhập sai mật khẩu thanh toán quá số lần quy định. Xin vui lòng thực hiện lại giao dịch.";
            default:
                return "Giao dịch không thành công. Mã lỗi: " + responseCode;
        }
    }
}
