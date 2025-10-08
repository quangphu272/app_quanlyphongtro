package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.AdminBookingViewHolder> {

    private List<Booking> bookings;
    private OnBookingActionListener listener;

    public interface OnBookingActionListener {
        void onViewBookingDetails(Booking booking);
        void onDeleteBooking(Booking booking);
    }

    public AdminBookingAdapter(List<Booking> bookings, OnBookingActionListener listener) {
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_booking, parent, false);
        return new AdminBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminBookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class AdminBookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomTitle;
        private TextView tvTenantName;
        private TextView tvLandlordName;
        private TextView tvCheckInDate;
        private TextView tvCheckOutDate;
        private TextView tvTotalAmount;
        private TextView tvDuration;
        private Chip chipStatus;
        private MaterialButton btnViewDetails;
        private MaterialButton btnDelete;

        public AdminBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvLandlordName = itemView.findViewById(R.id.tvLandlordName);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Booking booking, OnBookingActionListener listener) {
            // Room info
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());
            }

            // Tenant info
            if (booking.getTenant() != null) {
                tvTenantName.setText("Khách thuê: " + booking.getTenant().getFullName());
            }

            // Landlord info
            if (booking.getLandlord() != null) {
                tvLandlordName.setText("Chủ trọ: " + booking.getLandlord().getFullName());
            }

            // Booking details
            if (booking.getBookingDetails() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvCheckInDate.setText(sdf.format(booking.getBookingDetails().getCheckInDate()));
                tvCheckOutDate.setText(sdf.format(booking.getBookingDetails().getCheckOutDate()));
                tvDuration.setText(booking.getBookingDetails().getDuration() + " tháng");
            }

            // Pricing
            if (booking.getPricing() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                tvTotalAmount.setText(formatter.format(booking.getPricing().getTotalAmount()) + " VNĐ");
            }

            // Status
            chipStatus.setText(getStatusText(booking.getStatus()));
            chipStatus.setChipBackgroundColorResource(getStatusColor(booking.getStatus()));

            // Set click listeners
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewBookingDetails(booking);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteBooking(booking);
                }
            });
        }

        private String getStatusText(String status) {
            switch (status) {
                case "pending": return "Chờ xác nhận";
                case "confirmed": return "Đã xác nhận";
                case "deposit_paid": return "Đã thanh toán";
                case "active": return "Đang hoạt động";
                case "completed": return "Đã hoàn thành";
                case "cancelled": return "Đã hủy";
                default: return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "pending": return R.color.warning;
                case "confirmed": return R.color.info;
                case "deposit_paid": return R.color.success;
                case "active": return R.color.primary;
                case "completed": return R.color.success;
                case "cancelled": return R.color.error;
                default: return R.color.on_surface_variant;
            }
        }
    }
}
