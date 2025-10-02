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
import com.example.appquanlytimtro.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    
    private List<Room> rooms;
    private OnRoomClickListener listener;
    
    public interface OnRoomClickListener {
        void onRoomClick(Room room);
        void onRoomLike(Room room);
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
    
    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivRoomImage;
        private TextView tvTitle, tvAddress, tvPrice, tvArea, tvRoomType, tvRating;
        private ImageView ivLike;
        
        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRoomImage = itemView.findViewById(R.id.ivRoomImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvArea = itemView.findViewById(R.id.chipArea);
            tvRoomType = itemView.findViewById(R.id.chipRoomType);
            tvRating = itemView.findViewById(R.id.tvRating);
            ivLike = itemView.findViewById(R.id.ivLike);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRoomClick(rooms.get(position));
                    }
                }
            });
            
            ivLike.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onRoomLike(rooms.get(position));
                    }
                }
            });
        }
        
        public void bind(Room room) {
            tvTitle.setText(room.getTitle());
            
            // Set address
            if (room.getAddress() != null) {
                String address = room.getAddress().getStreet() + ", " + 
                               room.getAddress().getWard() + ", " + 
                               room.getAddress().getDistrict() + ", " + 
                               room.getAddress().getCity();
                tvAddress.setText(address);
            }
            
            // Set price
            if (room.getPrice() != null) {
                NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
                String price = formatter.format(room.getPrice().getMonthly()) + " VNĐ/tháng";
                tvPrice.setText(price);
            }
            
            // Set area
            tvArea.setText(room.getArea() + " m²");
            
            // Set room type
            tvRoomType.setText(getRoomTypeText(room.getRoomType()));
            
            // Set rating
            if (room.getRating() != null) {
                tvRating.setText(String.format("%.1f", room.getRating().getAverage()));
            } else {
                tvRating.setText("0.0");
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
                            .into(ivRoomImage);
                } else {
                    ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
                }
            } else {
                ivRoomImage.setImageResource(R.drawable.ic_room_placeholder);
            }
            
            // Set like status
            // This would need to be implemented based on user's liked rooms
            ivLike.setImageResource(android.R.drawable.btn_star_big_off);
        }
        
        private String getRoomTypeText(String roomType) {
            switch (roomType) {
                case Constants.ROOM_TYPE_STUDIO:
                    return "Studio";
                case Constants.ROOM_TYPE_1_BEDROOM:
                    return "1 phòng ngủ";
                case Constants.ROOM_TYPE_2_BEDROOM:
                    return "2 phòng ngủ";
                case Constants.ROOM_TYPE_3_BEDROOM:
                    return "3 phòng ngủ";
                case Constants.ROOM_TYPE_SHARED:
                    return "Phòng chung";
                default:
                    return roomType;
            }
        }
    }
}
