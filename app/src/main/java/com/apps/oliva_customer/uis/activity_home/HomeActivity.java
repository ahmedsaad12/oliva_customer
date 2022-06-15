package com.apps.oliva_customer.uis.activity_home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;


import com.apps.oliva_customer.interfaces.Listeners;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.mvvm.HomeActivityMvvm;
import com.apps.oliva_customer.preferences.Preferences;
import com.apps.oliva_customer.tags.Tags;
import com.apps.oliva_customer.uis.activity_base.BaseActivity;

import com.apps.oliva_customer.R;

import com.apps.oliva_customer.databinding.ActivityHomeBinding;
import com.apps.oliva_customer.language.Language;
import com.apps.oliva_customer.uis.activity_cart.CartActivity;
import com.apps.oliva_customer.uis.activity_contact_us.ContactUsActivity;
import com.apps.oliva_customer.uis.activity_language.LanguageActivity;
import com.apps.oliva_customer.uis.activity_login.LoginActivity;
import com.apps.oliva_customer.uis.activity_my_orders.MyOrderActivity;
import com.apps.oliva_customer.uis.activity_notification.NotificationActivity;
import com.apps.oliva_customer.uis.activity_share.ShareActivity;
import com.apps.oliva_customer.uis.activity_sign_up.SignUpActivity;
import com.apps.oliva_customer.uis.activity_wallet.WalletActivity;
import com.apps.oliva_customer.uis.activity_favourite.FavouriteActivity;
import com.squareup.picasso.Picasso;

import io.paperdb.Paper;

public class HomeActivity extends BaseActivity implements Listeners.VerificationListener {
    private ActivityHomeBinding binding;
    private NavController navController;
    private HomeActivityMvvm homeActivityMvvm;
    private Preferences preferences;
    private UserModel userModel;
    private ActivityResultLauncher<Intent> launcher;
    private int req = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();


    }

    private void initView() {

        preferences = Preferences.getInstance();
        updateCartCount();
        userModel = preferences.getUserData(this);
        if (userModel != null) {
            binding.setModel(userModel);
        }
        homeActivityMvvm = ViewModelProviders.of(this).get(HomeActivityMvvm.class);


        setSupportActionBar(binding.toolBar);
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        NavigationUI.setupWithNavController(binding.toolBar, navController);
        NavigationUI.setupActionBarWithNavController(this, navController);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            updateCartCount();
            if (getUserModel() != null) {
                updateFirebase();
            }
        });

//        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
//            if (binding.toolBar.getNavigationIcon() != null) {
//                binding.toolBar.getNavigationIcon().setColorFilter(ContextCompat.getColor(HomeActivity.this, R.color.black), PorterDuff.Mode.SRC_ATOP);
//
//            }
//        });


//
//        toggle.setHomeAsUpIndicator(R.drawable.ic_menu);


        homeActivityMvvm.logout.observe(this, aBoolean -> {
            if (aBoolean) {
                logout();
            }
        });
        homeActivityMvvm.firebase.observe(this, token -> {
            if (getUserModel() != null) {
                UserModel userModel = getUserModel();
                userModel.getData().setFirebase_token(token);
                setUserModel(userModel);
            }
        });

        binding.imgNotification.setOnClickListener(v -> {
            if (getUserModel() != null) {
                Intent intent = new Intent(this, NotificationActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        });
        if (getUserModel() != null) {
            homeActivityMvvm.updateFirebase(this, getUserModel());
        }

        binding.flCart.setOnClickListener(v -> {
            Intent intent = new Intent(this, CartActivity.class);
            launcher.launch(intent);

        });

    }


    public void refreshActivity(String lang) {
        Paper.book().write("lang", lang);
        Language.setNewLocale(this, lang);
        new Handler()
                .postDelayed(() -> {

                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }, 500);


    }


    @Override
    public void onBackPressed() {
        int currentFragmentId = navController.getCurrentDestination().getId();
        if (currentFragmentId == R.id.home) {
            finish();

        } else {
            navController.popBackStack();
        }

    }

    @Override
    public void onVerificationSuccess() {

    }


    public void updateFirebase() {
        if (getUserModel() != null) {
            homeActivityMvvm.updateFirebase(this, getUserModel());
        }
    }

    private void logout() {

        clearUserModel(this);
        userModel = getUserModel();
        binding.setModel(null);
        navigationToLoginActivity();
    }

    private void navigationToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        launcher.launch(intent);

    }

    public void updateCartCount() {
        if (preferences.getCartData(this) != null) {
            binding.setCartCount(preferences.getCartData(this).getDetails().size() + "");
        } else {
            binding.setCartCount(String.valueOf(0));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
    }

    public void llogout() {
        homeActivityMvvm.logout(this, getUserModel());
    }
}
