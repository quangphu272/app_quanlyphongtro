package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.example.appquanlytimtro.utils.Constants;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings;
    private OnBookingClickListener listener;
    
    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onBookingAction(Booking booking, String action);
    }
    
    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }
    
    @Override
    public int getItemCount() {
        return bookings.size();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomTitle, tvCheckIn, tvCheckOut, tvTotalAmount, tvStatus, tvBookingDate;
        private Button btnAction;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.chipStatus);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            btnAction = itemView.findViewById(R.id.btnAction);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBookingClick(bookings.get(position));
                    }
                }
            });
            
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Booking booking = bookings.get(position);
                        String action = getActionForStatus(booking.getStatus());
                        listener.onBookingAction(booking, action);
                    }
                }
            });
        }
        
        public void bind(Booking booking) {
            // Set room title
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());
            } else {
                tvRoomTitle.setText("Phòng trọ");
            }
            
            // Set dates
            // Get booking details
            if (booking.getBookingDetails() != null) {
                tvCheckIn.setText("Nhận phòng: " + formatDate(booking.getBookingDetails().getCheckInDate()));
                tvCheckOut.setText("Trả phòng: " + formatDate(booking.getBookingDetails().getCheckOutDate()));
            }
            
            // Set total amount
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
            double totalAmount = booking.getPricing() != null ? booking.getPricing().getTotalAmount() : 0;
            String amount = formatter.format(totalAmount) + " VNĐ";
            tvTotalAmount.setText("Tổng tiền: " + amount);
            
            // Set status
            tvStatus.setText(getStatusText(booking.getStatus()));
            tvStatus.setTextColor(getStatusColor(booking.getStatus()));
            
            // Set booking date
            tvBookingDate.setText("Đặt ngày: " + formatDate(booking.getCreatedAt()));
            
            // Set action button
            String action = getActionForStatus(booking.getStatus());
            btnAction.setText(action);
            btnAction.setVisibility(action.isEmpty() ? View.GONE : View.VISIBLE);
        }
        
        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "N/A";
            }
            
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                // If parsing fails, return the original string
                if (dateString.length() > 10) {
                    return dateString.substring(0, 10);
                }
                return dateString;
            }
        }
        
        private String getStatusText(String status) {
            switch (status) {
                case Constants.BOOKING_STATUS_PENDING:
                    return "Chờ xác nhận";
                case Constants.BOOKING_STATUS_CONFIRMED:
                    return "Đã xác nhận";
                case Constants.BOOKING_STATUS_DEPOSIT_PAID:
                    return "Đã đặt cọc";
                case Constants.BOOKING_STATUS_ACTIVE:
                    return "Đang thuê";
                case Constants.BOOKING_STATUS_COMPLETED:
                    return "Hoàn thành";
                case Constants.BOOKING_STATUS_CANCELLED:
                    return "Đã hủy";
                default:
                    return status;
            }
        }
        
        private int getStatusColor(String status) {
            switch (status) {
                case Constants.BOOKING_STATUS_PENDING:
                    return itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
                case Constants.BOOKING_STATUS_CONFIRMED:
                case Constants.BOOKING_STATUS_DEPOSIT_PAID:
                case Constants.BOOKING_STATUS_ACTIVE:
                    return itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                case Constants.BOOKING_STATUS_COMPLETED:
                    return itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
                case Constants.BOOKING_STATUS_CANCELLED:
                    return itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                default:
                    return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
        }
        
        private String getActionForStatus(String status) {
            switch (status) {
                case Constants.BOOKING_STATUS_PENDING:
                    return "Hủy đặt";
                case Constants.BOOKING_STATUS_CONFIRMED:
                    return "Thanh toán";
                case Constants.BOOKING_STATUS_DEPOSIT_PAID:
                    return "Xem chi tiết";
                case Constants.BOOKING_STATUS_ACTIVE:
                    return "Xem chi tiết";
                case Constants.BOOKING_STATUS_COMPLETED:
                    return "Đánh giá";
                default:
                    return "";
            }
        }
    }
}
