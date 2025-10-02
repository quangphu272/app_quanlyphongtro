package com.example.appquanlytimtro.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.User;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> users;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UsersAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardView;
        private ImageView ivAvatar;
        private TextView tvFullName;
        private TextView tvEmail;
        private TextView tvPhone;
        private Chip chipRole;
        private TextView tvJoinDate;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            chipRole = itemView.findViewById(R.id.chipRole);
            tvJoinDate = itemView.findViewById(R.id.tvJoinDate);
        }

        public void bind(User user) {
            tvFullName.setText(user.getFullName());
            tvEmail.setText(user.getEmail());
            tvPhone.setText(user.getPhone() != null ? user.getPhone() : "Chưa có");
            
            // Set role chip
            String roleText = getRoleText(user.getRole());
            chipRole.setText(roleText);
            chipRole.setChipBackgroundColorResource(getRoleColor(user.getRole()));
            
            // Set join date
            tvJoinDate.setText("Tham gia: " + (user.getCreatedAt() != null ? 
                user.getCreatedAt() : "Không xác định"));
            
            // Load avatar
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person);
            }
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(user);
                }
            });
        }
        
        private String getRoleText(String role) {
            switch (role) {
                case "admin":
                    return "Quản trị viên";
                case "landlord":
                    return "Chủ trọ";
                case "tenant":
                    return "Người thuê";
                default:
                    return "Không xác định";
            }
        }
        
        private int getRoleColor(String role) {
            switch (role) {
                case "admin":
                    return R.color.error_container;
                case "landlord":
                    return R.color.primary_container;
                case "tenant":
                    return R.color.secondary_container;
                default:
                    return R.color.surface_variant;
            }
        }
    }
}