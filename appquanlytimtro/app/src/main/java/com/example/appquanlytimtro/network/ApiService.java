package com.example.appquanlytimtro.network;

import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.models.LoginRequest;
import com.example.appquanlytimtro.models.LoginResponse;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.models.RegisterRequest;
import com.example.appquanlytimtro.models.RegisterResponse;
import com.example.appquanlytimtro.models.Room;
import com.example.appquanlytimtro.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import okhttp3.MultipartBody;

public interface ApiService {
    
    // Authentication endpoints
    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);
    
    @POST("auth/register")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest registerRequest);
    
    @GET("auth/me")
    Call<ApiResponse<User>> getCurrentUser(@Header("Authorization") String token);
    
    @POST("auth/logout")
    Call<ApiResponse<Void>> logout(@Header("Authorization") String token);
    
    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body Map<String, String> email);
    
    @POST("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Header("Authorization") String token, 
                                          @Body Map<String, String> passwords);
    
    // User endpoints
    @GET("users")
    Call<ApiResponse<Map<String, Object>>> getUsers(@Header("Authorization") String token,
                                                   @QueryMap Map<String, String> params);
    
    @GET("users/{id}")
    Call<ApiResponse<User>> getUser(@Header("Authorization") String token, @Path("id") String userId);
    
    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(@Header("Authorization") String token, 
                                      @Path("id") String userId, @Body User user);
    
    @DELETE("users/{id}")
    Call<ApiResponse<Void>> deleteUser(@Header("Authorization") String token, @Path("id") String userId);
    
    @GET("users/{id}/bookings")
    Call<ApiResponse<Map<String, Object>>> getUserBookings(@Header("Authorization") String token,
                                                          @Path("id") String userId,
                                                          @QueryMap Map<String, String> params);
    
    @GET("users/{id}/rooms")
    Call<ApiResponse<Map<String, Object>>> getUserRooms(@Header("Authorization") String token,
                                                       @Path("id") String userId,
                                                       @QueryMap Map<String, String> params);
    
    @GET("users/{id}/payments")
    Call<ApiResponse<Map<String, Object>>> getUserPayments(@Header("Authorization") String token,
                                                          @Path("id") String userId,
                                                          @QueryMap Map<String, String> params);
    
    // Room endpoints
    @GET("rooms")
    Call<ApiResponse<Map<String, Object>>> getRooms(@QueryMap Map<String, String> params);
    
    @GET("rooms/featured")
    Call<ApiResponse<List<Room>>> getFeaturedRooms(@Query("limit") int limit);
    
    @GET("rooms/{id}")
    Call<ApiResponse<Room>> getRoom(@Path("id") String roomId);
    
    @POST("rooms")
    Call<ApiResponse<Room>> createRoom(@Header("Authorization") String token, @Body Room room);
    
    @PUT("rooms/{id}")
    Call<ApiResponse<Room>> updateRoom(@Header("Authorization") String token, 
                                      @Path("id") String roomId, @Body Room room);
    
    @DELETE("rooms/{id}")
    Call<ApiResponse<Void>> deleteRoom(@Header("Authorization") String token, @Path("id") String roomId);
    
    @POST("rooms/{id}/like")
    Call<ApiResponse<Map<String, Object>>> toggleRoomLike(@Header("Authorization") String token, 
                                                         @Path("id") String roomId);
    
    @GET("rooms/{id}/similar")
    Call<ApiResponse<List<Room>>> getSimilarRooms(@Path("id") String roomId, @Query("limit") int limit);
    
    @GET("rooms/stats/overview")
    Call<ApiResponse<Map<String, Object>>> getRoomStats();

    // Room images upload
    @Multipart
    @POST("rooms/{id}/images")
    Call<ApiResponse<Map<String, Object>>> uploadRoomImages(
            @Header("Authorization") String token,
            @Path("id") String roomId,
            @Part List<MultipartBody.Part> images
    );
    
    // Booking endpoints
    @GET("bookings")
    Call<ApiResponse<Map<String, Object>>> getBookings(@Header("Authorization") String token,
                                                      @QueryMap Map<String, String> params);
    
    @GET("bookings/{id}")
    Call<ApiResponse<Booking>> getBooking(@Header("Authorization") String token, @Path("id") String bookingId);
    
    @POST("bookings")
    Call<ApiResponse<Booking>> createBooking(@Header("Authorization") String token, @Body Booking booking);
    
    @PUT("bookings/{id}")
    Call<ApiResponse<Booking>> updateBooking(@Header("Authorization") String token, 
                                            @Path("id") String bookingId, @Body Booking booking);
    
    @DELETE("bookings/{id}")
    Call<ApiResponse<Void>> deleteBooking(@Header("Authorization") String token, @Path("id") String bookingId);
    
    @PUT("bookings/{id}/status")
    Call<ApiResponse<Booking>> updateBookingStatus(@Header("Authorization") String token, 
                                                  @Path("id") String bookingId, 
                                                  @Body Map<String, String> status);
    
    // Payment endpoints
    @GET("payments")
    Call<ApiResponse<Map<String, Object>>> getPayments(@Header("Authorization") String token,
                                                      @QueryMap Map<String, String> params);
    
    @GET("payments/{id}")
    Call<ApiResponse<Payment>> getPayment(@Header("Authorization") String token, @Path("id") String paymentId);
    
    @POST("payments")
    Call<ApiResponse<Map<String, Object>>> createPayment(@Header("Authorization") String token, 
                                                        @Body Map<String, Object> payment);
    
    @POST("payments/{id}/process")
    Call<ApiResponse<Map<String, Object>>> processPayment(@Header("Authorization") String token, 
                                                         @Path("id") String paymentId);
    
    @PUT("payments/{id}/confirm")
    Call<ApiResponse<Payment>> confirmPayment(@Header("Authorization") String token, @Path("id") String paymentId);
    
    @GET("payments/stats/overview")
    Call<ApiResponse<Map<String, Object>>> getPaymentStats(@Header("Authorization") String token);
    
    // VNPay & Bank Transfer
    @POST("payments/vnpay/create")
    Call<ApiResponse<Map<String, Object>>> createVNPayPayment(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );
    
    @POST("payments/bank-transfer")
    Call<ApiResponse<Map<String, Object>>> createBankTransferPayment(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );
    
    // Invoice endpoints
    @GET("invoices")
    Call<ApiResponse<Map<String, Object>>> getInvoices(@Header("Authorization") String token,
                                                      @QueryMap Map<String, String> params);
    
    @GET("invoices/{id}")
    Call<ApiResponse<Map<String, Object>>> getInvoice(@Header("Authorization") String token, @Path("id") String invoiceId);
    
    @POST("invoices")
    Call<ApiResponse<Map<String, Object>>> createInvoice(@Header("Authorization") String token, 
                                                        @Body Map<String, Object> invoice);
    
    // Notification endpoints
    @GET("notifications")
    Call<ApiResponse<Map<String, Object>>> getNotifications(@Header("Authorization") String token,
                                                           @QueryMap Map<String, String> params);
    
    @PUT("notifications/{id}/read")
    Call<ApiResponse<Void>> markNotificationAsRead(@Header("Authorization") String token, @Path("id") String notificationId);
    
    @PUT("notifications/read-all")
    Call<ApiResponse<Void>> markAllNotificationsAsRead(@Header("Authorization") String token);
    
    // Statistics endpoints
    @GET("statistics/overview")
    Call<ApiResponse<Map<String, Object>>> getStatisticsOverview(@Header("Authorization") String token);
    
    @GET("statistics/revenue")
    Call<ApiResponse<Map<String, Object>>> getRevenueStatistics(@Header("Authorization") String token,
                                                               @QueryMap Map<String, String> params);
    
    @GET("statistics/rooms")
    Call<ApiResponse<Map<String, Object>>> getRoomStatistics(@Header("Authorization") String token,
                                                            @QueryMap Map<String, String> params);
    
    // Booking endpoints - Additional
    @PUT("bookings/{id}/confirm")
    Call<ApiResponse<Booking>> confirmBooking(@Header("Authorization") String token, @Path("id") String bookingId);
    
    @PUT("bookings/{id}/cancel")
    Call<ApiResponse<Booking>> cancelBooking(@Header("Authorization") String token, 
                                           @Path("id") String bookingId, 
                                           @Body Map<String, String> reason);
    
    @PUT("bookings/{id}/contract")
    Call<ApiResponse<Booking>> signContract(@Header("Authorization") String token, 
                                          @Path("id") String bookingId, 
                                          @Body Map<String, Object> contract);
    
    @GET("bookings/{id}/contract")
    Call<ApiResponse<Map<String, Object>>> getContract(@Header("Authorization") String token, @Path("id") String bookingId);
    
    @GET("bookings/stats/overview")
    Call<ApiResponse<Map<String, Object>>> getBookingStats(@Header("Authorization") String token);
    
    // Statistics endpoints - Additional
    @GET("statistics/dashboard")
    Call<ApiResponse<Map<String, Object>>> getDashboardStats(@Header("Authorization") String token);
    
    @GET("statistics/bookings")
    Call<ApiResponse<Map<String, Object>>> getBookingStatistics(@Header("Authorization") String token,
                                                               @QueryMap Map<String, String> params);
    
    @GET("statistics/users")
    Call<ApiResponse<Map<String, Object>>> getUserStatistics(@Header("Authorization") String token);
}
