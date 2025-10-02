package com.example.appquanlytimtro.utils;

public class Constants {
    // API Constants
    public static final String BASE_URL = "http://10.0.2.2:5000/api/";
    
    // User Roles
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_LANDLORD = "landlord";
    public static final String ROLE_TENANT = "tenant";
    
    // Room Types
    public static final String ROOM_TYPE_STUDIO = "studio";
    public static final String ROOM_TYPE_1_BEDROOM = "1_bedroom";
    public static final String ROOM_TYPE_2_BEDROOM = "2_bedroom";
    public static final String ROOM_TYPE_3_BEDROOM = "3_bedroom";
    public static final String ROOM_TYPE_SHARED = "shared";
    
    // Booking Status
    public static final String BOOKING_STATUS_PENDING = "pending";
    public static final String BOOKING_STATUS_CONFIRMED = "confirmed";
    public static final String BOOKING_STATUS_DEPOSIT_PAID = "deposit_paid";
    public static final String BOOKING_STATUS_ACTIVE = "active";
    public static final String BOOKING_STATUS_COMPLETED = "completed";
    public static final String BOOKING_STATUS_CANCELLED = "cancelled";
    
    // SharedPreferences Keys
    public static final String PREFS_NAME = "app_prefs";
    public static final String TOKEN_KEY = "auth_token";
    public static final String USER_DATA_KEY = "user_data";
}