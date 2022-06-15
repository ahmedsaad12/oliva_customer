package com.apps.oliva_customer.mvvm;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.apps.oliva_customer.model.OrderModel;
import com.apps.oliva_customer.model.SingleOrderDataModel;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.remote.Api;
import com.apps.oliva_customer.tags.Tags;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ActivityOrderDetialsMvvm extends AndroidViewModel {
    private static final String TAG = "ActivityorderDetialsMvvm";
    private Context context;


    private MutableLiveData<Boolean> isLoadingLivData;
    private MutableLiveData<OrderModel> orderModelMutableLiveData;
    private CompositeDisposable disposable = new CompositeDisposable();


    public ActivityOrderDetialsMvvm(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();
    }


    public MutableLiveData<Boolean> getIsLoading() {
        if (isLoadingLivData == null) {
            isLoadingLivData = new MutableLiveData<>();
        }
        return isLoadingLivData;
    }

    public MutableLiveData<OrderModel> getOrder() {
        if (orderModelMutableLiveData == null) {
            orderModelMutableLiveData = new MutableLiveData<>();
        }
        return orderModelMutableLiveData;
    }

    public void getorderDetials(String id, UserModel userModel) {
        isLoadingLivData.postValue(true);
        Api.getService(Tags.base_url)
                .getSingleOrders(userModel.getData().getAccess_token(), id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new SingleObserver<Response<SingleOrderDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SingleOrderDataModel> response) {
                        isLoadingLivData.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200) {

                                orderModelMutableLiveData.postValue(response.body().getData());

                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        isLoadingLivData.postValue(false);
                        //Log.e(TAG, "onError: ", e);
                    }
                });

    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();

    }

}
