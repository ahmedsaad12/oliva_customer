package com.apps.oliva_customer.uis.activity_my_orders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.adapter.OrdersAdapter;
import com.apps.oliva_customer.databinding.ActivityCategoryDetialsBinding;
import com.apps.oliva_customer.databinding.ActivityOrdersBinding;
import com.apps.oliva_customer.model.OrderModel;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.mvvm.ActivityMyOrdersMvvm;
import com.apps.oliva_customer.preferences.Preferences;
import com.apps.oliva_customer.uis.activity_base.BaseActivity;
import com.apps.oliva_customer.uis.activity_order_detials.OrderDetialsActivity;

import java.util.ArrayList;
import java.util.List;

public class MyOrderActivity extends BaseActivity {
    private ActivityOrdersBinding binding;
    private ActivityMyOrdersMvvm activityMyOrdersMvvm;
    private UserModel userModel;
    private Preferences preferences;
    private OrdersAdapter ordersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_orders);
        initView();

    }

    private void initView() {
        ordersAdapter = new OrdersAdapter(this);
        preferences = Preferences.getInstance();
        userModel = getUserModel();
        activityMyOrdersMvvm = ViewModelProviders.of(this).get(ActivityMyOrdersMvvm.class);
        //  setUpToolbar(binding.toolbar, getString(R.string.contact_us), R.color.white, R.color.black);
        binding.setLang(getLang());
        activityMyOrdersMvvm.getIsLoading().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    binding.swipeRefresh.setRefreshing(true);
                    binding.progBar.setVisibility(View.VISIBLE);
                } else {
                    binding.swipeRefresh.setRefreshing(false);
                    binding.progBar.setVisibility(View.GONE);
                }
            }
        });
        activityMyOrdersMvvm.getOrders().observe(this, new Observer<List<OrderModel>>() {
            @Override
            public void onChanged(List<OrderModel> orderModels) {
                if (orderModels != null&&orderModels.size()>0) {
                    ordersAdapter.updateList(orderModels);
                    binding.cardNoData.setVisibility(View.GONE);
                } else {

                    ordersAdapter.updateList(new ArrayList<>());
                    binding.cardNoData.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.recView.setLayoutManager(new GridLayoutManager(this, 1));
        binding.recView.setAdapter(ordersAdapter);
        binding.llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.swipeRefresh.setOnRefreshListener(() -> {
            activityMyOrdersMvvm.getOrders(getUserModel());
        });
        activityMyOrdersMvvm.getOrders(userModel);
    }


    public void show(OrderModel orderModel) {
        Intent intent = new Intent(this, OrderDetialsActivity.class);
        intent.putExtra("order_id", orderModel.getId());
        startActivity(intent);
    }
}