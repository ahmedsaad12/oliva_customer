package com.apps.oliva_customer.mvvm;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.model.StatusResponse;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.remote.Api;
import com.apps.oliva_customer.share.Common;
import com.apps.oliva_customer.tags.Tags;
import com.google.firebase.iid.FirebaseInstanceId;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class HomeActivityMvvm extends AndroidViewModel {
    private Context context;
    public MutableLiveData<Boolean> logout = new MutableLiveData<>();

    public MutableLiveData<String> firebase = new MutableLiveData<>();

    private CompositeDisposable disposable = new CompositeDisposable();

    public HomeActivityMvvm(@NonNull Application application) {
        super(application);
        context = application.getApplicationContext();


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

    public void updateFirebase(Context context, UserModel userModel) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener((Activity) context, task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
             //   Log.e("sssss", token);
                Api.getService(Tags.base_url).updateFirebasetoken(token, userModel.getData().getUser().getId() + "", "android").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).unsubscribeOn(Schedulers.io()).subscribe(new SingleObserver<Response<StatusResponse>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(@NonNull Response<StatusResponse> statusResponseResponse) {
                        if (statusResponseResponse.isSuccessful()) {
                          //  Log.e("lllll", statusResponseResponse.body().getStatus() + "");
                            if (statusResponseResponse.body().getStatus() == 200) {
                                firebase.postValue(token);
                                Log.e("token", "updated successfully");
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable throwable) {

                    }
                });
            }
        });


    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
