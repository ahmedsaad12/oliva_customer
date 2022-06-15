package com.apps.oliva_customer.mvvm;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.apps.oliva_customer.model.OrderDataModel;
import com.apps.oliva_customer.model.OrderModel;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.remote.Api;
import com.apps.oliva_customer.tags.Tags;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ActivityMyOrdersMvvm extends AndroidViewModel {
    private static final String TAG = "ActivitymyordersMvvm";
    private Context context;


    private MutableLiveData<Boolean> isLoadingLiveData;
    private MutableLiveData<List<OrderModel>> listMutableLiveData;
    private CompositeDisposable disposable = new CompositeDisposable();


    public ActivityMyOrdersMvvm(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }


    public MutableLiveData<Boolean> getIsLoading() {
        if (isLoadingLiveData == null) {
            isLoadingLiveData = new MutableLiveData<>();
        }
        return isLoadingLiveData;
    }

    public MutableLiveData<List<OrderModel>> getOrders() {
        if (listMutableLiveData == null) {
            listMutableLiveData = new MutableLiveData<>();
        }
        return listMutableLiveData;
    }

    public void getOrders(UserModel userModel) {


        isLoadingLiveData.setValue(true);

        Api.getService(Tags.base_url)
                .getMyOrders(userModel.getData().getAccess_token())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<OrderDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<OrderDataModel> response) {
                        isLoadingLiveData.postValue(false);
                     //   Log.e("oooo",response.code()+"");
                        if (response.isSuccessful() && response.body() != null) {
                          //  Log.e("sss", response.body().getStatus() + "");
                            if (response.body().getStatus() == 200) {
                                // List<ProductModel> list = response.body().getData();
                                listMutableLiveData.setValue(response.body().getData());
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        isLoadingLiveData.setValue(false);
                        Log.e("sss", e.toString());

                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();

    }

}
