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
import com.example.appquanlytimtro.models.Room;
import com.google.android.material.chip.Chip;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    
    private List<Room> rooms;
    private OnRoomClickListener listener;
    
    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }
    
    public RoomAdapter(List<Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }
    
    @Override
    public int getItemCount() {
        return rooms.size();
    }
    
    public void updateRooms(List<Room> newRooms) {
        this.rooms.clear();
        this.rooms.addAll(newRooms);
        notifyDataSetChanged();
    }
    
    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvTitle, tvAddress, tvPrice, tvArea, tvViews;
        private Chip chipRoomType, chipStatus;
        
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvViews = itemView.findViewById(R.id.tvViews);
            chipRoomType = itemView.findViewById(R.id.chipRoomType);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRoomClick(rooms.get(position));
                    }
                }
            });
        }
        
        public void bind(Room room) {
            tvTitle.setText(room.getTitle());
            
            // Set address
            if (room.getAddress() != null) {
                String address = "";
                if (room.getAddress().getStreet() != null && !room.getAddress().getStreet().isEmpty()) {
                    address += room.getAddress().getStreet() + ", ";
                }
                if (room.getAddress().getWard() != null && !room.getAddress().getWard().isEmpty()) {
                    address += room.getAddress().getWard() + ", ";
                }
                if (room.getAddress().getDistrict() != null && !room.getAddress().getDistrict().isEmpty()) {
                    address += room.getAddress().getDistrict() + ", ";
                }
                if (room.getAddress().getCity() != null && !room.getAddress().getCity().isEmpty()) {
                    address += room.getAddress().getCity();
                }
                // Remove trailing comma and space
                if (address.endsWith(", ")) {
                    address = address.substring(0, address.length() - 2);
                }
                tvAddress.setText(address);
            }
            
            // Set price
            if (room.getPrice() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                String price = formatter.format(room.getPrice().getMonthly()) + " VNĐ/tháng";
                tvPrice.setText(price);
            }
            
            // Set area
            tvArea.setText(String.format("%.0f m²", room.getArea()));
            
            // Set views
            tvViews.setText(room.getViews() + " lượt xem");
            
            // Set room type
            if (room.getRoomType() != null) {
                String roomTypeText = getRoomTypeText(room.getRoomType());
                chipRoomType.setText(roomTypeText);
            }
            
            // Set status
            String status = room.getStatus();
            if (status == null) status = "active";
            
            switch (status.toLowerCase()) {
                case "active":
                    chipStatus.setText("Còn trống");
                    chipStatus.setChipBackgroundColorResource(R.color.success);
                    break;
                case "rented":
                    chipStatus.setText("Đã cho thuê");
                    chipStatus.setChipBackgroundColorResource(R.color.info);
                    break;
                case "maintenance":
                    chipStatus.setText("Bảo trì");
                    chipStatus.setChipBackgroundColorResource(R.color.warning);
                    break;
                default:
                    chipStatus.setText("Không xác định");
                    chipStatus.setChipBackgroundColorResource(R.color.surface_variant);
                    break;
            }
            
            // Set room image
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                String imageUrl = room.getImages().get(0).getUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    // Convert relative URL to full URL
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
        
        private String getRoomTypeText(String roomType) {
            switch (roomType) {
                case "studio":
                    return "Studio";
                case "1bedroom":
                    return "1 phòng ngủ";
                case "2bedroom":
                    return "2 phòng ngủ";
                case "3bedroom":
                    return "3 phòng ngủ";
                default:
                    return roomType;
            }
        }
    }
}