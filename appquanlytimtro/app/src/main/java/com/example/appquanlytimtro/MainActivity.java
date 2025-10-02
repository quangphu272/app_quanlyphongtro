package com.example.appquanlytimtro;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.appquanlytimtro.auth.LoginActivity;
import com.example.appquanlytimtro.bookings.BookingListActivity;
import com.example.appquanlytimtro.payments.PaymentListActivity;
import com.example.appquanlytimtro.rooms.RoomListActivity;
import com.example.appquanlytimtro.models.User;
import com.example.appquanlytimtro.network.RetrofitClient;
import com.example.appquanlytimtro.utils.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.example.appquanlytimtro.profile.ProfileFragment;
import com.example.appquanlytimtro.admin.AdminUsersFragment;
import com.example.appquanlytimtro.tenant.TenantHomeFragment;
import com.example.appquanlytimtro.landlord.LandlordDashboardFragment;
import com.example.appquanlytimtro.landlord.LandlordRoomManagementFragment;
import com.example.appquanlytimtro.landlord.LandlordBookingManagementFragment;
import com.example.appquanlytimtro.landlord.LandlordPaymentManagementFragment;
import com.example.appquanlytimtro.admin.AdminDashboardFragment;
import com.example.appquanlytimtro.admin.AdminBookingManagementFragment;
import com.example.appquanlytimtro.admin.AdminPaymentManagementFragment;

public class MainActivity extends AppCompatActivity {

    private RetrofitClient retrofitClient;
    private User currentUser;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        
        retrofitClient = RetrofitClient.getInstance(this);
        
        // Check if user is logged in
        if (!retrofitClient.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Delay to ensure views are properly initialized
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadUserData();
            setupBottomNavigation();
        }, 100);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Re-check login status when activity resumes
        if (!retrofitClient.isLoggedIn()) {
            navigateToLogin();
            return;
        }
        
        // Reload user data if needed
        if (currentUser == null) {
            loadUserData();
        }
    }
    
    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView == null) {
            // Layout loading error, redirect to login
            navigateToLogin();
            return;
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản Lý Tìm Trọ");
        }
    }
    
    private void loadUserData() {
        String userJson = retrofitClient.getUserData();
        android.util.Log.d("MainActivity", "Loading user data: " + (userJson != null));
        
        if (userJson != null && !userJson.isEmpty()) {
            try {
                Gson gson = new Gson();
                currentUser = gson.fromJson(userJson, User.class);
                
                android.util.Log.d("MainActivity", "User parsed: " + (currentUser != null));
                android.util.Log.d("MainActivity", "User role: " + (currentUser != null ? currentUser.getRole() : "null"));
                
                if (currentUser != null && currentUser.getRole() != null) {
                    // Initialize bottom navigation menu based on role
                    initBottomMenuByRole(currentUser.getRole());
                    
                    // Load default fragment based on role
                    loadDefaultFragment();
                    
                    // Select default tab
                    if (Constants.ROLE_TENANT.equals(currentUser.getRole())) {
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    } else {
                        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
                    }
                } else {
                    // Invalid user data, redirect to login
                    navigateToLogin();
                }
            } catch (Exception e) {
                // JSON parsing error, redirect to login
                navigateToLogin();
            }
        } else {
            // If no user data, redirect to login
            navigateToLogin();
        }
    }
    
    private void loadDefaultFragment() {
        if (currentUser == null) return;
        
        String role = currentUser.getRole();
        Fragment defaultFragment = null;
        
        if (Constants.ROLE_TENANT.equals(role)) {
            defaultFragment = new TenantHomeFragment();
        } else if (Constants.ROLE_LANDLORD.equals(role)) {
            defaultFragment = new LandlordDashboardFragment();
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            defaultFragment = new AdminDashboardFragment();
        }
        
        if (defaultFragment != null) {
            switchFragment(defaultFragment);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) return;
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            String role = currentUser != null ? currentUser.getRole() : "";
            
            if (id == R.id.nav_home || id == R.id.nav_dashboard) {
                // Dashboard/Home based on role
                if (Constants.ROLE_TENANT.equals(role)) {
                    switchFragment(new TenantHomeFragment());
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordDashboardFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminDashboardFragment());
                }
                return true;
            } else if (id == R.id.nav_search) {
                // Search rooms - only for tenants
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, RoomListActivity.class);
                    startActivity(intent);
                }
                return true;
            } else if (id == R.id.nav_manage_rooms || id == R.id.nav_rooms) {
                // Room management based on role
                if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordRoomManagementFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    Intent intent = new Intent(this, RoomListActivity.class);
                    startActivity(intent);
                }
                return true;
            } else if (id == R.id.nav_bookings) {
                // Bookings based on role
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, BookingListActivity.class);
                    startActivity(intent);
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordBookingManagementFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminBookingManagementFragment());
                }
                return true;
            } else if (id == R.id.nav_payments) {
                // Payments based on role
                if (Constants.ROLE_TENANT.equals(role)) {
                    Intent intent = new Intent(this, PaymentListActivity.class);
                    startActivity(intent);
                } else if (Constants.ROLE_LANDLORD.equals(role)) {
                    switchFragment(new LandlordPaymentManagementFragment());
                } else if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminPaymentManagementFragment());
                }
                return true;
            } else if (id == R.id.nav_users) {
                // User management - only for admin
                if (Constants.ROLE_ADMIN.equals(role)) {
                    switchFragment(new AdminUsersFragment());
                }
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void initBottomMenuByRole(String role) {
        if (bottomNavigationView == null) return;
        if (Constants.ROLE_TENANT.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_tenant, bottomNavigationView.getMenu());
        } else if (Constants.ROLE_LANDLORD.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_landlord, bottomNavigationView.getMenu());
        } else if (Constants.ROLE_ADMIN.equals(role)) {
            bottomNavigationView.getMenu().clear();
            getMenuInflater().inflate(R.menu.menu_admin, bottomNavigationView.getMenu());
        }
    }

    private void switchFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
    
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        retrofitClient.logout();
        navigateToLogin();
    }
}