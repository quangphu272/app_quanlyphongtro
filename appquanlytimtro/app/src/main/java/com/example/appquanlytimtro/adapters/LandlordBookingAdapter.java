package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LandlordBookingAdapter extends RecyclerView.Adapter<LandlordBookingAdapter.BookingViewHolder> {
    
    private List<Booking> bookings;
    private OnBookingActionListener listener;
    
    public interface OnBookingActionListener {
        void onBookingClick(Booking booking);
        void onConfirmBooking(Booking booking);
        void onRejectBooking(Booking booking);
        void onViewContract(Booking booking);
    }
    
    public LandlordBookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_landlord_booking, parent, false);
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
    
    public void updateBookings(List<Booking> newBookings) {
        this.bookings.clear();
        this.bookings.addAll(newBookings);
        notifyDataSetChanged();
    }
    
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvRoomTitle, tvTenantName, tvTenantPhone, tvBookingDate;
        private TextView tvCheckInDate, tvCheckOutDate, tvTotalAmount, tvDuration;
        private Chip chipStatus;
        private MaterialButton btnConfirm, btnReject, btnViewContract;
        
        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvTenantPhone = itemView.findViewById(R.id.tvTenantPhone);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnViewContract = itemView.findViewById(R.id.btnViewContract);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onBookingClick(bookings.get(position));
                    }
                }
            });
            
            btnConfirm.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onConfirmBooking(bookings.get(position));
                    }
                }
            });
            
            btnReject.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRejectBooking(bookings.get(position));
                    }
                }
            });
            
            btnViewContract.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onViewContract(bookings.get(position));
                    }
                }
            });
        }
        
        public void bind(Booking booking) {
            // Room information
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());
                
                // Room image
                if (booking.getRoom().getImages() != null && !booking.getRoom().getImages().isEmpty()) {
                    String imageUrl = booking.getRoom().getImages().get(0).getUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (!imageUrl.startsWith("http")) {
                            imageUrl = "http://10.0.2.2:5000" + imageUrl;
                        }
                        Glide.with(itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_room_placeholder)
                                .error(R.drawable.ic_room_placeholder)
                                .centerCrop()
                                .into(ivRoomImage);
                    } else {
                        ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
                    }
                } else {
                    ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
                }
            }
            
            // Tenant information
            if (booking.getTenant() != null) {
                tvTenantName.setText(booking.getTenant().getFullName());
                tvTenantPhone.setText(booking.getTenant().getPhone());
            }
            
            // Booking dates
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            if (booking.getBookingDetails() != null) {
                try {
                    if (booking.getBookingDetails().getCheckInDate() != null) {
                        tvCheckInDate.setText("Nhận phòng: " + booking.getBookingDetails().getCheckInDate());
                    }
                    if (booking.getBookingDetails().getCheckOutDate() != null) {
                        tvCheckOutDate.setText("Trả phòng: " + booking.getBookingDetails().getCheckOutDate());
                    }
                    tvDuration.setText("Thời hạn: " + booking.getBookingDetails().getDuration() + " tháng");
                } catch (Exception e) {
                    tvCheckInDate.setText("Nhận phòng: N/A");
                    tvCheckOutDate.setText("Trả phòng: N/A");
                    tvDuration.setText("Thời hạn: N/A");
                }
            }
            
            // Booking date
            try {
                if (booking.getCreatedAt() != null) {
                    tvBookingDate.setText("Ngày đặt: " + booking.getCreatedAt());
                }
            } catch (Exception e) {
                tvBookingDate.setText("Ngày đặt: N/A");
            }
            
            // Total amount
            if (booking.getPricing() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                String amount = formatter.format(booking.getPricing().getTotalAmount()) + " VNĐ";
                tvTotalAmount.setText("Tổng tiền: " + amount);
            }
            
            // Status
            String status = booking.getStatus();
            if (status == null) status = "pending";
            
            switch (status.toLowerCase()) {
                case "pending":
                    chipStatus.setText("Chờ xác nhận");
                    chipStatus.setChipBackgroundColorResource(R.color.warning);
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                    btnViewContract.setVisibility(View.GONE);
                    break;
                case "confirmed":
                    chipStatus.setText("Đã xác nhận");
                    chipStatus.setChipBackgroundColorResource(R.color.info);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.VISIBLE);
                    break;
                case "active":
                    chipStatus.setText("Đang thuê");
                    chipStatus.setChipBackgroundColorResource(R.color.success);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.VISIBLE);
                    break;
                case "completed":
                    chipStatus.setText("Hoàn thành");
                    chipStatus.setChipBackgroundColorResource(R.color.success);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.VISIBLE);
                    break;
                case "cancelled":
                    chipStatus.setText("Đã hủy");
                    chipStatus.setChipBackgroundColorResource(R.color.error);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.GONE);
                    break;
                case "rejected":
                    chipStatus.setText("Đã từ chối");
                    chipStatus.setChipBackgroundColorResource(R.color.error);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.GONE);
                    break;
                default:
                    chipStatus.setText("Không xác định");
                    chipStatus.setChipBackgroundColorResource(R.color.surface_variant);
                    btnConfirm.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                    btnViewContract.setVisibility(View.GONE);
                    break;
            }
        }
    }
}

