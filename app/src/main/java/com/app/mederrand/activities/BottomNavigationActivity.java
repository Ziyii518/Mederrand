package com.app.mederrand.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.app.mederrand.R;
import com.app.mederrand.activities.auth.SplashActivity;
import com.app.mederrand.databinding.ActivityBottomNavigationBinding;
import com.app.mederrand.utils.Utilities;

import java.util.Objects;

public class BottomNavigationActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityBottomNavigationBinding binding;
    Context context;

    NavController navController;
    NavDestination currentDestination;
    AlertDialog logoutDialog;

    String userType, manageText;
    private static final float END_SCALE = 0.7f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBottomNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init(){
        Utilities.hideNavAndStatusBarNew(BottomNavigationActivity.this);
        context = BottomNavigationActivity.this;
        userType = Utilities.getString(context, "userType");

        if (userType.equals("Pharmacist")){
            manageText = "Manage Medicines";
            binding.navigationView.getMenu().findItem(R.id.menu_manageFragment).setVisible(true);
            updateManageText(binding.navigationView.getMenu().findItem(R.id.menu_manageFragment), manageText);
        }else {
            binding.navigationView.getMenu().findItem(R.id.menu_manageFragment).setVisible(false);
        }


        navController = Navigation.findNavController(this, R.id.navHostFragment);

        NavigationUI.setupWithNavController(binding.navigationView, navController);

        initClickListeners();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                R.string.text_open,
                R.string.text_close
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Scale the View based on current slide offset
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                binding.mainContainer.setScaleX(offsetScale);
                binding.mainContainer.setScaleY(offsetScale);

                // Translate the View, accounting for the scaled width
                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff =  binding.mainContainer.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                binding.mainContainer.setTranslationX(xTranslation);
                binding.mainContainer.setCardElevation(5.0f);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                binding.mainContainer.setCardElevation(0f);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                binding.mainContainer.setCardElevation(5.0f);
            }
        };

        binding.drawerLayout.addDrawerListener(mDrawerToggle);

        binding.drawerLayout.setScrimColor(ContextCompat.getColor(context, android.R.color.transparent));
      
        binding.drawerLayout.setDrawerElevation(0);
        binding.drawerLayout.setElevation(0f);

        View headerView =  binding.navigationView.getHeaderView(0);
        TextView fullName = headerView.findViewById(R.id.textFullName);
        TextView userEmail = headerView.findViewById(R.id.textUserEmail);
        fullName.setText(Utilities.getString(context, "userFullName"));
        userEmail.setText(Utilities.getString(context, "userEmail"));


        // If the type is buyer we will show the home or market button other wise it will be hidden from Pharmacist or Driver
        if (userType.equals("Buyer")){
            binding.btnHome.setVisibility(View.VISIBLE);
            navController.navigate(R.id.homeFragment);
        }else {
            binding.btnHome.setVisibility(View.GONE);
        }

    }

    private void initClickListeners(){
        binding.btnDrawer.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START, true));

        binding.navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_profileFragment) {
                if (Objects.requireNonNull(currentDestination).getId() != R.id.profileFragment){
                    navController.navigate(R.id.profileFragment);
                }
            } else if (item.getItemId() == R.id.menu_contactsFragment) {
                if (Objects.requireNonNull(currentDestination).getId() != R.id.contactsFragment){
                    navController.navigate(R.id.contactsFragment);
                }
            } else if (item.getItemId() == R.id.menu_manageFragment) {
                if (Objects.requireNonNull(currentDestination).getId() != R.id.manageMedicinesFragment){
                    navController.navigate(R.id.manageMedicinesFragment);
                }
            }
            // Close the drawer
            binding.drawerLayout.closeDrawer(GravityCompat.START, false);

            return true;
        });

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> setIconsForCurrentDestination());


        binding.btnHome.setOnClickListener(this);
        binding.btnOrders.setOnClickListener(this);
        binding.btnInbox.setOnClickListener(this);

        binding.btnLogout.setOnClickListener(view -> requestForSendCodePopup());
    }

    private void requestForSendCodePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog);
        final View customLayout = LayoutInflater.from(context).inflate(R.layout.popup_yes_no, null);
        builder.setView(customLayout);

        TextView text_sure = customLayout.findViewById(R.id.text_sure);
        TextView text_blockThisPerson = customLayout.findViewById(R.id.text_blockThisPerson);

        int sendCode, areYouSure;
        sendCode = R.string.text_logout_with_exclamation;
        areYouSure = R.string.text_are_you_sure_you_want_to_logout ;


        text_blockThisPerson.setText(sendCode);
        text_sure.setText(areYouSure);

        TextView btn_yes = customLayout.findViewById(R.id.text_Yes);
        TextView text_No = customLayout.findViewById(R.id.text_No);

        text_No.setOnClickListener(view -> logoutDialog.dismiss());

        btn_yes.setOnClickListener(view -> {
            logout();
            logoutDialog.dismiss();
        });

        logoutDialog = builder.create();
        Objects.requireNonNull(logoutDialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
        logoutDialog.show();
        logoutDialog.setCancelable(true);
    }

    private void logout(){
        Utilities.clearSharedPref(context);
        finishAffinity();
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("finish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnHome){
            if (Objects.requireNonNull(currentDestination).getId() != R.id.homeFragment){
                navController.navigate(R.id.homeFragment);
            }
        }
        else if (id ==  R.id.btnInbox){
            if (Objects.requireNonNull(currentDestination).getId() != R.id.inboxFragment){
                navController.navigate(R.id.inboxFragment);
            }
        }else if (id ==  R.id.btnOrders){
            if (Objects.requireNonNull(currentDestination).getId() != R.id.ordersFragment){
                navController.navigate(R.id.ordersFragment);
            }
        }
    }


    private void setIconsForCurrentDestination() {
        // Get the current destination
        currentDestination = navController.getCurrentDestination();
        changeNavigationColors(Objects.requireNonNull(currentDestination).getId());
    }



    private void changeNavigationColors(int selected) {

        updateFragmentViews(selected, R.id.homeFragment, binding.textHome, binding.icHome, R.color.app_color_dark, R.color.colorDoveGray, R.drawable.home_selected_ic, R.drawable.home_ic);
        updateFragmentViews(selected, R.id.ordersFragment, binding.textOrders, binding.icOrders, R.color.app_color_dark, R.color.colorDoveGray, R.drawable.alarm_selected_ic, R.drawable.alarm_ic);
        updateFragmentViews(selected, R.id.inboxFragment, binding.textInbox, binding.icInbox, R.color.app_color_dark, R.color.colorDoveGray, R.drawable.community_selected_ic, R.drawable.community_ic);

        // Working for the Navigation View
        updateMenuItem(binding.navigationView.getMenu().findItem(R.id.menu_profileFragment), "Profile", selected, R.id.profileFragment);
        updateMenuItem(binding.navigationView.getMenu().findItem(R.id.menu_contactsFragment), "Contacts", selected, R.id.contactsFragment);
        if (userType.equals("Pharmacist")){
            updateMenuItem(binding.navigationView.getMenu().findItem(R.id.menu_manageFragment), manageText, selected, R.id.manageMedicinesFragment);
        }

    }

    private void updateFragmentViews(int selected, int selectedId, TextView textView, ImageView imageView, int selectedColor, int unselectedColor, int selectedDrawable, int unselectedDrawable) {
        if (selected == selectedId) {
            textView.setTextColor(ContextCompat.getColor(context, selectedColor));
            imageView.setImageResource(selectedDrawable);
        } else {
            textView.setTextColor(ContextCompat.getColor(context, unselectedColor));
            imageView.setImageResource(unselectedDrawable);
        }
    }


    private void updateMenuItem(MenuItem menuItem, String title, int selectedItemId, int itemId) {
        SpannableString spannable = new SpannableString(title);

        if (selectedItemId == itemId) {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, spannable.length(), 0);
        } else {
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, spannable.length(), 0);
        }

        menuItem.setTitle(spannable);
    }

    private void updateManageText(MenuItem menuItem, String title) {
        SpannableString spannable = new SpannableString(title);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)), 0, spannable.length(), 0);
        menuItem.setTitle(spannable);
    }
}