package com.apps.oliva_customer.mvvm;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;


import com.apps.oliva_customer.R;
import com.apps.oliva_customer.model.SettingDataModel;
import com.apps.oliva_customer.model.SettingModel;
import com.apps.oliva_customer.model.StatusResponse;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.model.UserSettingsModel;
import com.apps.oliva_customer.remote.Api;
import com.apps.oliva_customer.share.Common;
import com.apps.oliva_customer.tags.Tags;
import com.apps.oliva_customer.uis.activity_payment.PaymentActivity;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class FragmentProfileGeneralMvvm extends AndroidViewModel {


    private CompositeDisposable disposable = new CompositeDisposable();
    private MutableLiveData<Boolean> isLoadingLivData;
    private MutableLiveData<SettingModel> mutableLiveData;
    public MutableLiveData<Boolean> logout = new MutableLiveData<>();

    public FragmentProfileGeneralMvvm(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<SettingModel> getMutableLiveData() {
        if (mutableLiveData == null) {
            mutableLiveData = new MutableLiveData<>();
        }
        return mutableLiveData;
    }


    public MutableLiveData<Boolean> getIsLoading() {
        if (isLoadingLivData == null) {
            isLoadingLivData = new MutableLiveData<>();
        }
        return isLoadingLivData;
    }

    public void getSetting() {


        isLoadingLivData.setValue(true);

        Api.getService(Tags.base_url)
                .getAbout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Response<SettingModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<SettingModel> response) {
                        isLoadingLivData.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus() == 200) {

                                mutableLiveData.setValue(response.body());
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        isLoadingLivData.setValue(false);
                    }
                });
    }

    public void logout(Context context, UserModel userModel) {
        ProgressDialog dialog = Common.createProgressDialog(context, context.getResources().getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url).logout(userModel.getData().getAccess_token() + "", userModel.getData().getFirebase_token()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()).subscribe(new SingleObserver<Response<StatusResponse>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable.add(d);
            }

            @Override
            public void onSuccess(@NonNull Response<StatusResponse> statusResponseResponse) {
                dialog.dismiss();
                if (statusResponseResponse.isSuccessful()) {
                    Log.e("logout", statusResponseResponse.code() + "" + statusResponseResponse.body().getStatus());
                    if (statusResponseResponse.body().getStatus() == 200) {
                        logout.postValue(true);
                    }
                }
            }

            @Override
            public void onError(@NonNull Throwable throwable) {
                dialog.dismiss();
            }
        });
    }

}
