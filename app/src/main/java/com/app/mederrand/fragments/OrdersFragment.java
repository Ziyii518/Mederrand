package com.app.mederrand.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.activities.medicine.OrderDetailActivity;
import com.app.mederrand.adapters.OrdersAdapter;
import com.app.mederrand.apis.models.medicines.OrderDataModel;
import com.app.mederrand.databinding.FragmentOrdersBinding;
import com.app.mederrand.utils.CustomBannerToast;
import com.app.mederrand.utils.Utilities;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class OrdersFragment extends Fragment implements OrdersAdapter.OrdersAdapterCallBack {
    FragmentOrdersBinding binding;
    Context context;
    List<OrderDataModel> ordersList = new ArrayList<>();
    OrdersAdapter ordersAdapter;

    String userType, userId, orderStatus;

    ActivityResultLauncher<Intent> onHireUserLauncher;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater);
        init();
        return binding.getRoot();
    }

    private void init() {
        context = requireActivity();
        userType = Utilities.getString(context, "userType");
        userId = Utilities.getString(context, "userId");

        initLaunchers();

        initTabLayout();

        if (ordersList.isEmpty()){
            binding.mainLayout.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            fetchOrdersForUser(userType, userId, orderStatus);
        }else {
            showOrdersRecyclerview(binding.ordersRecyclerview, ordersList);
        }

    }

    private void initTabLayout(){
        // Show All Tabs only for Buyer and Pharmacist
        if (userType.equals("Buyer") || userType.equals("Pharmacist")){
            orderStatus = "Pending";
            addTab( "Pending", binding.tabLayout, true);
            addTab( "Accepted", binding.tabLayout, false);
            addTab("Out For Delivery", binding.tabLayout, false);
            addTab( "Delivered", binding.tabLayout, false);
            addTab( "Completed", binding.tabLayout, false);
            addTab( "Cancelled", binding.tabLayout, false);
        }else {
            orderStatus = "Out For Delivery";
            addTab("Out For Delivery", binding.tabLayout, true);
            addTab( "Delivered", binding.tabLayout, false);
            addTab( "Completed", binding.tabLayout, false);
        }

        for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
            if (binding.tabLayout.getTabAt(i) != null) {
                TooltipCompat.setTooltipText(Objects.requireNonNull(binding.tabLayout.getTabAt(i)).view, null);
            }
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                orderStatus = Objects.requireNonNull(tab.getText()).toString();
                binding.mainLayout.setVisibility(View.GONE);
                binding.loading.setVisibility(View.VISIBLE);
                fetchOrdersForUser(userType, userId, orderStatus);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void addTab(String tabName, TabLayout tabLayout, boolean isSelected){
        tabLayout.addTab(tabLayout.newTab().setText(tabName), isSelected);
    }


    public void fetchOrdersForUser(final String userRole, final String userId, final String status) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        Query query;
        if (userRole.equals("Buyer")) {
            query = ordersRef.orderByChild("buyerId").equalTo(userId);
        } else if (userRole.equals("Pharmacist")) {
            query = ordersRef.orderByChild("pharmacyId").equalTo(userId);
        } else {
            query = ordersRef.orderByChild("driverId").equalTo(userId);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ordersList = new ArrayList<>();
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    OrderDataModel order = orderSnapshot.getValue(OrderDataModel.class);
                    if (order != null && order.getOrderStatus().equals(status)) {
                        ordersList.add(order);
                    }
                }
                showOrdersRecyclerview(binding.ordersRecyclerview, ordersList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded()){
                    CustomBannerToast.showFailureToast(binding.getRoot(), requireActivity(), "Something went wrong !",  "Error while fetching orders list");
                }

            }
        });
    }

    private void showOrdersRecyclerview(RecyclerView recyclerView, List<OrderDataModel> ordersList){
        binding.loading.setVisibility(View.GONE);
        binding.mainLayout.setVisibility(View.VISIBLE);

        if (ordersList.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            binding.layoutEmptyScreen.getRoot().setVisibility(View.VISIBLE);
            binding.layoutEmptyScreen.emptyImage.setImageResource(R.drawable.no_orders_ic);
            binding.layoutEmptyScreen.textEmptyScreenHeading.setText(R.string.text_no_orders_yet);
            binding.layoutEmptyScreen.textEmptyScreenDescription.setText(R.string.text_no_orders_yet_description);
        }else {
            binding.layoutEmptyScreen.getRoot().setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            ordersAdapter = new OrdersAdapter(context, ordersList, this);
            recyclerView.setAdapter(ordersAdapter);
        }
    }


    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("ordersDetail", ordersList.get(position));

        onHireUserLauncher.launch(intent);
        requireActivity().overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }

    private void initLaunchers(){
        onHireUserLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                binding.mainLayout.setVisibility(View.GONE);
                binding.loading.setVisibility(View.VISIBLE);
                fetchOrdersForUser(userType, userId, orderStatus);
            }
        });
    }
}