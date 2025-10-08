package com.example.appquanlytimtro.payments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;

public class PaymentListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Danh sách thanh toán - Khách thuê");
    }
}