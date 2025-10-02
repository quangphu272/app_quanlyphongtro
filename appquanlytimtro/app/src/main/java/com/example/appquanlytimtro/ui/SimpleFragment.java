package com.example.appquanlytimtro.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appquanlytimtro.R;

public class SimpleFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";

    public static SimpleFragment newInstance(String title) {
        SimpleFragment fragment = new SimpleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple, container, false);
        TextView titleView = view.findViewById(R.id.tvTitle);
        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_TITLE, "");
            titleView.setText(title);
        }
        return view;
    }
}


