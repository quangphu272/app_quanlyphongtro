package com.example.appquanlytimtro.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.Payment;
import com.example.appquanlytimtro.utils.Constants;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    
    private List<Payment> payments;
    private OnPaymentClickListener listener;
    private String userRole;
    
    public interface OnPaymentClickListener {
        void onPaymentClick(Payment payment);
        void onPaymentAction(Payment payment, String action);
    }
    
    public PaymentAdapter(List<Payment> payments, OnPaymentClickListener listener, String userRole) {
        this.payments = payments;
        this.listener = listener;
        this.userRole = userRole;
    }
    
    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payment payment = payments.get(position);
        holder.bind(payment);
    }
    
    @Override
    public int getItemCount() {
        return payments.size();
    }
    
    class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAmount, tvType, tvStatus, tvDate, tvDescription;
        private Button btnAction;
        
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            tvStatus = itemView.findViewById(R.id.chipStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
        
        public void bind(Payment payment) {
            // Set amount
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvAmount.setText(formatter.format(payment.getAmount()));
            
            // Set type
            tvType.setText(getTypeText(payment.getType()));
            
            // Set status
            tvStatus.setText(getStatusText(payment.getStatus()));
            tvStatus.setTextColor(getStatusColor(payment.getStatus()));
            
            // Set date
            // Use payment date or current date as fallback
            String dateText = "Ngày tạo: " + formatDate(new java.util.Date());
            tvDate.setText(dateText);
            
            // Set description
            tvDescription.setText(payment.getDescription() != null ? payment.getDescription() : "Không có mô tả");
            
            // Set action button
            String actionText = getActionText(payment.getStatus());
            btnAction.setText(actionText);
            btnAction.setVisibility(actionText.isEmpty() ? View.GONE : View.VISIBLE);
            
            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentClick(payment);
                }
            });
            
            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentAction(payment, getActionType(payment.getStatus()));
                }
            });
        }
        
        private String getTypeText(String type) {
            switch (type) {
                case "deposit":
                    return "Tiền cọc";
                case "monthly_rent":
                    return "Tiền thuê hàng tháng";
                case "utilities":
                    return "Tiền điện nước";
                case "penalty":
                    return "Phí phạt";
                case "refund":
                    return "Hoàn tiền";
                default:
                    return type;
            }
        }
        
        private String getStatusText(String status) {
            switch (status) {
                case "pending":
                    return "Chờ thanh toán";
                case "completed":
                    return "Đã thanh toán";
                case "failed":
                    return "Thanh toán thất bại";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return status;
            }
        }
        
        private int getStatusColor(String status) {
            switch (status) {
                case "pending":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
                case "completed":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                case "failed":
                    return itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                case "cancelled":
                    return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
                default:
                    return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
        }
        
        private String getActionText(String status) {
            switch (status) {
                case "pending":
                    return "Thanh toán";
                case "completed":
                    return "Xem chi tiết";
                case "failed":
                    return "Thử lại";
                default:
                    return "";
            }
        }
        
        private String getActionType(String status) {
            switch (status) {
                case "pending":
                    return "pay";
                case "completed":
                case "failed":
                    return "view";
                default:
                    return "view";
            }
        }
        
        private String formatDate(Date date) {
            if (date == null) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return sdf.format(date);
        }
    }
}
