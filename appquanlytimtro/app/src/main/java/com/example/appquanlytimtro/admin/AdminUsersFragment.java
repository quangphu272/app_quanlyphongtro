package com.example.appquanlytimtro.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appquanlytimtro.R;
import com.example.appquanlytimtro.models.ApiResponse;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersFragment extends Fragment implements UsersAdapter.OnUserClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private UsersAdapter adapter;
    private final List<User> users = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_admin_users, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);
        progressBar = v.findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UsersAdapter(users);
        adapter.setOnUserClickListener(this);
        recyclerView.setAdapter(adapter);
        loadUsers();
        return v;
    }

    private void loadUsers() {
        showLoading(true);
        RetrofitClient client = RetrofitClient.getInstance(requireContext());
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "50");
        client.getApiService().getUsers("Bearer " + client.getToken(), params).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Object>>> call, Response<ApiResponse<Map<String, Object>>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    Object listObj = response.body().getData().get("users");
                    if (listObj instanceof List<?>) {
                        users.clear();
                        for (Object o : (List<?>) listObj) {
                            String json = gson.toJson(o);
                            User u = gson.fromJson(json, User.class);
                            users.add(u);
                        }
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.load_failed, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onUserClick(User user) {
        // Handle user click - could show user details or edit dialog
        Toast.makeText(getContext(), "Clicked: " + user.getFullName(), Toast.LENGTH_SHORT).show();
    }
}


