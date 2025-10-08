package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;
    private List<Booking> filteredBookings;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onBookingStatusChange(Booking booking, String newStatus);
        void onPaymentClick(Booking booking);
    }

    public BookingAdapter(List<Booking> bookings, OnBookingClickListener listener) {
        this.bookings = bookings;
        this.filteredBookings = new ArrayList<>(bookings);
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
        android.util.Log.d("BookingAdapter", "onBindViewHolder called for position: " + position + ", total items: " + filteredBookings.size());
        Booking booking = filteredBookings.get(position);
        holder.bind(booking, listener);
    }

    @Override
    public int getItemCount() {
        return filteredBookings.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        android.util.Log.d("BookingAdapter", "updateBookings called with " + newBookings.size() + " bookings");
        
        // Create a copy to avoid reference issues
        List<Booking> bookingsCopy = new ArrayList<>(newBookings);
        
        this.bookings.clear();
        this.bookings.addAll(bookingsCopy);
        this.filteredBookings.clear();
        this.filteredBookings.addAll(bookingsCopy);
        
        android.util.Log.d("BookingAdapter", "After update - bookings: " + this.bookings.size() + ", filteredBookings: " + this.filteredBookings.size());
        notifyDataSetChanged();
    }

    public void filterByStatus(String status) {
        filteredBookings.clear();
        if (status == null) {
            filteredBookings.addAll(bookings);
        } else {
            for (Booking booking : bookings) {
                if (status.equals(booking.getStatus())) {
                    filteredBookings.add(booking);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomTitle;
        private TextView tvRoomAddress;
        private TextView tvCheckInDate;
        private TextView tvCheckOutDate;
        private TextView tvTotalAmount;
        private TextView tvDuration;
        private Chip chipStatus;
        private TextView tvLandlordName;
        private LinearLayout layoutActionButtons;
        private MaterialButton btnPayment;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomTitle = itemView.findViewById(R.id.tvRoomTitle);
            tvRoomAddress = itemView.findViewById(R.id.tvRoomAddress);
            tvCheckInDate = itemView.findViewById(R.id.tvCheckInDate);
            tvCheckOutDate = itemView.findViewById(R.id.tvCheckOutDate);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            tvLandlordName = itemView.findViewById(R.id.tvLandlordName);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnPayment = itemView.findViewById(R.id.btnPayment);
        }

        public void bind(Booking booking, OnBookingClickListener listener) {
            // Room info
            if (booking.getRoom() != null) {
                tvRoomTitle.setText(booking.getRoom().getTitle());
                if (booking.getRoom().getAddress() != null) {
                    String address = booking.getRoom().getAddress().getStreet() + ", " +
                                   booking.getRoom().getAddress().getWard() + ", " +
                                   booking.getRoom().getAddress().getDistrict() + ", " +
                                   booking.getRoom().getAddress().getCity();
                    tvRoomAddress.setText(address);
                }
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

            // Landlord
            if (booking.getLandlord() != null) {
                tvLandlordName.setText("Chủ trọ: " + booking.getLandlord().getFullName());
            }

            // Status
            chipStatus.setText(getStatusText(booking.getStatus()));
            chipStatus.setChipBackgroundColorResource(getStatusColor(booking.getStatus()));

            // Action buttons based on status
            setupActionButtons(booking, listener);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
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

        private void setupActionButtons(Booking booking, OnBookingClickListener listener) {
            String status = booking.getStatus();
            
            // Reset button visibility
            layoutActionButtons.setVisibility(View.GONE);
            
            switch (status) {
                case "pending":
                    layoutActionButtons.setVisibility(View.VISIBLE);
                    btnPayment.setVisibility(View.VISIBLE);
                    btnPayment.setText("Thanh toán");
                    break;
                case "confirmed":
                case "deposit_paid":
                case "active":
                case "completed":
                case "cancelled":
                    // No action buttons for these statuses
                    break;
            }

            // Set click listener for payment button
            btnPayment.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(booking);
                }
            });
        }
    }
}