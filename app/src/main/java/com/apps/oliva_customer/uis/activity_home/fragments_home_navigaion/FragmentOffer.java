package com.apps.oliva_customer.uis.activity_home.fragments_home_navigaion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.adapter.OfferProductAdapter;
import com.apps.oliva_customer.adapter.SliderAdapter;
import com.apps.oliva_customer.databinding.FragmentOfferBinding;
import com.apps.oliva_customer.model.ProductModel;
import com.apps.oliva_customer.model.SliderDataModel;
import com.apps.oliva_customer.mvvm.FragmentOfferMvvm;
import com.apps.oliva_customer.uis.activity_base.BaseFragment;
import com.apps.oliva_customer.uis.activity_home.HomeActivity;
import com.apps.oliva_customer.uis.activity_product_detials.ProductDetialsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.disposables.CompositeDisposable;


public class FragmentOffer extends BaseFragment {
    private FragmentOfferBinding binding;
    private HomeActivity activity;
    private FragmentOfferMvvm fragmentOfferMvvm;
    private OfferProductAdapter offerProductAdapter;
    private SliderAdapter sliderAdapter;
    private List<SliderDataModel.SliderModel> sliderModelList;
    private CompositeDisposable disposable = new CompositeDisposable();
    private Timer timer;
    private ActivityResultLauncher<Intent> launcher;
    private int req;
    private int layoutPosition;

    public static FragmentOffer newInstance() {
        FragmentOffer fragment = new FragmentOffer();
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (HomeActivity) context;
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (req == 2 && result.getResultCode() == Activity.RESULT_OK) {
                activity.updateCartCount();
                fragmentOfferMvvm.getOffers(getLang(), getUserModel());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offer, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();

    }

    private void initView() {
        sliderModelList = new ArrayList<>();
        fragmentOfferMvvm = ViewModelProviders.of(this).get(FragmentOfferMvvm.class);

        fragmentOfferMvvm.getIsLoading().observe(activity, isLoading -> {
            if (isLoading) {
                binding.progBarSlider.setVisibility(View.VISIBLE);


            }
            // binding.swipeRefresh.setRefreshing(isLoading);
        });
        fragmentOfferMvvm.getSliderDataModelMutableLiveData().observe(activity, new androidx.lifecycle.Observer<SliderDataModel>() {
            @Override
            public void onChanged(SliderDataModel sliderDataModel) {

                if (sliderDataModel.getData() != null) {
                    binding.progBarSlider.setVisibility(View.GONE);
                    sliderModelList.clear();
                    sliderModelList.addAll(sliderDataModel.getData());
                    sliderAdapter.notifyDataSetChanged();
                    timer = new Timer();
                    timer.scheduleAtFixedRate(new MyTask(), 3000, 3000);
                }

            }
        });


        fragmentOfferMvvm.getOfferList().observe(activity, new androidx.lifecycle.Observer<List<ProductModel>>() {
            @Override
            public void onChanged(List<ProductModel> productModels) {
                if (productModels != null && productModels.size() > 0) {

                    offerProductAdapter.updateList(productModels);
                    binding.cardNoData.setVisibility(View.GONE);

                } else {

                    binding.cardNoData.setVisibility(View.VISIBLE);
                }
            }
        });


        offerProductAdapter = new OfferProductAdapter(activity, this, getUserModel());
        binding.recView.setLayoutManager(new GridLayoutManager(activity, 1));
        binding.recView.setAdapter(offerProductAdapter);


        sliderAdapter = new SliderAdapter(sliderModelList, activity);
        binding.pager.setAdapter(sliderAdapter);
        binding.pager.setClipToPadding(false);
        binding.pager.setPadding(20, 0, 20, 0);
        binding.pager.setPageMargin(20);
        fragmentOfferMvvm.getFav().observe(activity, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    List<ProductModel> productModelList = fragmentOfferMvvm.getOfferList().getValue();
                    ProductModel productModel = productModelList.get(layoutPosition);
                    if (productModel.isIs_favorite()) {
                        productModel.setIs_favorite(false);
                    } else {
                        productModel.setIs_favorite(true);
                    }
                    productModelList.set(layoutPosition, productModel);
                    offerProductAdapter.updateList(productModelList, layoutPosition);
                }
            }
        });
        fragmentOfferMvvm.getSlider();
        fragmentOfferMvvm.getOffers(getLang(), getUserModel());

    }

    public class MyTask extends TimerTask {
        @Override
        public void run() {
            activity.runOnUiThread(() -> {
                int current_page = binding.pager.getCurrentItem();
                if (current_page < sliderAdapter.getCount() - 1) {
                    binding.pager.setCurrentItem(binding.pager.getCurrentItem() + 1);
                } else {
                    binding.pager.setCurrentItem(0);

                }
            });

        }

    }

    public void showProductDetials(String productid) {
        req = 2;
        Intent intent = new Intent(activity, ProductDetialsActivity.class);
        intent.putExtra("proid", productid);
        launcher.launch(intent);
    }

    public void addremovefave(int layoutPosition) {
        this.layoutPosition = layoutPosition;
        fragmentOfferMvvm.addRemoveFavourite(fragmentOfferMvvm.getOfferList().getValue().get(layoutPosition).getId(), getUserModel());
    }
}