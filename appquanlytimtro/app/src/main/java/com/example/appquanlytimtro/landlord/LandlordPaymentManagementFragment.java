package com.example.appquanlytimtro.landlord;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;

public class LandlordPaymentManagementFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText("Quản lý thanh toán - Chủ trọ");
        
        return view;
    }
}