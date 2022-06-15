package com.apps.oliva_customer.mvvm;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.apps.oliva_customer.model.CartDataModel;
import com.apps.oliva_customer.model.ItemCartModel;
import com.apps.oliva_customer.model.ProductModel;
import com.apps.oliva_customer.model.SingleProductDataModel;
import com.apps.oliva_customer.model.StatusResponse;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.preferences.Preferences;
import com.apps.oliva_customer.remote.Api;
import com.apps.oliva_customer.tags.Tags;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ActivityProductDetialsMvvm extends AndroidViewModel {
    private static final String TAG = "ActivityProductMvvm";
    private Context context;
    private List<ItemCartModel> cartModelList;
    private CartDataModel cartDataModel;
    private Preferences preferences;
    private MutableLiveData<Boolean> isLoadingLivData;
    private MutableLiveData<Boolean> addremove;

    private MutableLiveData<Integer> amount;

    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<SingleProductDataModel> productDataModelMutableLiveData;


    public ActivityProductDetialsMvvm(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }

    public LiveData<Integer> getAmount() {
        if (amount == null) {
            amount = new MutableLiveData<>();

        }
        return amount;
    }

    public LiveData<SingleProductDataModel> getProductData() {
        if (productDataModelMutableLiveData == null) {
            productDataModelMutableLiveData = new MutableLiveData<>();

        }
        return productDataModelMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        if (isLoadingLivData == null) {
            isLoadingLivData = new MutableLiveData<>();
        }
        return isLoadingLivData;
    }

    public MutableLiveData<Boolean> getFav() {
        if (addremove == null) {
            addremove = new MutableLiveData<>();
        }
        return addremove;
    }

    public void getProductDetials(String lang, String id, UserModel userModel) {
        String token = null;
        if (userModel != null) {
            token = userModel.getData().getAccess_token();
        }
        isLoadingLivData.postValue(true);
        Api.getService(Tags.base_url)
                .getSingleProduct(token, id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new SingleObserver<Response<SingleProductDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SingleProductDataModel> response) {
                        isLoadingLivData.postValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200) {

                                productDataModelMutableLiveData.postValue(response.body());

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        isLoadingLivData.postValue(false);
                        Log.e(TAG, "onError: ", e);
                    }
                });

    }

    public void addRemoveFavourite(String id, UserModel userModel) {
        Api.getService(Tags.base_url)
                .addRemoveFav(userModel.getData().getAccess_token(), id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new SingleObserver<Response<StatusResponse>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<StatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200 || response.body().getStatus() == 201) {

                                addremove.postValue(true);

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });

    }

    public void add_to_cart(ProductModel productModel, int amount, Context context) {
        preferences = Preferences.getInstance();
        cartDataModel = preferences.getCartData(context);

        if (cartDataModel == null) {

            cartModelList = new ArrayList<>();
            cartDataModel = new CartDataModel();
            cartDataModel.setDetails(cartModelList);
        } else {
            if (cartDataModel.getDetails() == null) {
                cartModelList = new ArrayList<>();

            } else {
                cartModelList = cartDataModel.getDetails();

            }

        }
        int pos = isProductItemSelected(productModel.getId());

        if (pos == -1) {
            ItemCartModel itemCartModel = new ItemCartModel();
            itemCartModel.setProduct_id(productModel.getId());
            itemCartModel.setQty(amount);
            itemCartModel.setSubtotal(itemCartModel.getQty() * productModel.getPrice_tax());
            itemCartModel.setProduct_batch_id("");
            itemCartModel.setImage(productModel.getImage());
            itemCartModel.setTitle(productModel.getName());
            itemCartModel.setProduct_code(productModel.getCode());
            itemCartModel.setDiscount(0);
            itemCartModel.setNet_unit_price(productModel.getPrice_tax());
            itemCartModel.setProduct_code(productModel.getCode());
            itemCartModel.setSale_unit("عدد");
            itemCartModel.setTax(productModel.getPrice_tax());
            itemCartModel.setTax_rate(0);

            cartModelList.add(itemCartModel);

        } else {
            ItemCartModel itemCartModel = cartModelList.get(pos);
            itemCartModel.setQty(itemCartModel.getQty() + amount);
            itemCartModel.setSubtotal(itemCartModel.getQty() * productModel.getPrice_tax());
            cartModelList.set(pos, itemCartModel);
        }
        if (cartDataModel == null) {
            cartDataModel = new CartDataModel();
        }

        cartDataModel.setDetails(cartModelList);

        calculateTotalCost();
    }

    private void calculateTotalCost() {
        double total = 0.0;
        for (ItemCartModel cartModel : cartModelList) {
            total += cartModel.getSubtotal();
        }
        cartDataModel.setTotal_price(total);
        preferences.createUpdateCartData(context, cartDataModel);
        amount.postValue(cartModelList.size());
    }


    public int isProductItemSelected(String product_id) {

        int pos = -1;

        cartDataModel = preferences.getCartData(context);
        if (cartDataModel != null && cartDataModel.getDetails() != null) {
            cartModelList = cartDataModel.getDetails();
            for (int index = 0; index < cartModelList.size(); index++) {
                ItemCartModel cartModel = cartModelList.get(index);
                if (product_id.equals(cartModel.getId())) {
                    pos = index;
                    return pos;
                }
            }
        }


        return pos;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();

    }

}
