package com.apps.oliva_customer.uis.activity_contact_us;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.databinding.ActivityContactUsBinding;
import com.apps.oliva_customer.model.ContactUsModel;
import com.apps.oliva_customer.model.SettingModel;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.mvvm.ContactusActivityMvvm;
import com.apps.oliva_customer.preferences.Preferences;
import com.apps.oliva_customer.uis.activity_base.BaseActivity;

public class ContactUsActivity extends BaseActivity {
    private ActivityContactUsBinding binding;
    private ContactUsModel contactUsModel;
    private ContactusActivityMvvm contactusActivityMvvm;
    private UserModel userModel;
    private SettingModel settingModel;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contact_us);
        initView();

    }

    private void initView() {
        binding.setLang(getLang());

        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        binding.setSettingModel(settingModel);
        contactusActivityMvvm = ViewModelProviders.of(this).get(ContactusActivityMvvm.class);
//         setUpToolbar(binding.toolbar, getString(R.string.contact_us), R.color.white, R.color.black);


        contactUsModel = new ContactUsModel();
        if (userModel != null) {
            contactUsModel.setName(userModel.getData().getUser().getName());

        }
        binding.setContactUsModel(contactUsModel);

        binding.btnSend.setOnClickListener(view -> {
            if (contactUsModel.isDataValid(this)) {
                contactusActivityMvvm.contactus(this, contactUsModel);
            }
        });
        contactusActivityMvvm.send.observe(this, aBoolean -> {
            if (aBoolean) {
                Toast.makeText(ContactUsActivity.this, getResources().getString(R.string.suc), Toast.LENGTH_LONG).show();
                finish();
            }
        });
        binding.llBack.setOnClickListener(view -> finish());

    }


}