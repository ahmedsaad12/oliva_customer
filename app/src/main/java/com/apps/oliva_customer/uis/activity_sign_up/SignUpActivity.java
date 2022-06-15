package com.apps.oliva_customer.uis.activity_sign_up;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.databinding.ActivitySignUpBinding;
import com.apps.oliva_customer.model.SignUpModel;
import com.apps.oliva_customer.model.UserModel;
import com.apps.oliva_customer.mvvm.ActivitySignupMvvm;
import com.apps.oliva_customer.preferences.Preferences;
import com.apps.oliva_customer.share.Common;
import com.apps.oliva_customer.tags.Tags;
import com.apps.oliva_customer.uis.activity_base.BaseActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class SignUpActivity extends BaseActivity {
    private ActivitySignUpBinding binding;
    private SignUpModel model;
    private Preferences preferences;
    private ActivitySignupMvvm activitySignupMvvm;
    private String phone_code, phone;
    private ActivityResultLauncher<Intent> launcher;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private int selectedReq = 0;
    private Uri uri = null;
    private UserModel userModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent.getStringExtra("phone_code") != null) {

            phone_code = intent.getStringExtra("phone_code");
            phone = intent.getStringExtra("phone");
        }
    }

    private void initView() {
        preferences = Preferences.getInstance();
        activitySignupMvvm = ViewModelProviders.of(this).get(ActivitySignupMvvm.class);
        model = new SignUpModel();
        userModel = getUserModel();
        if (userModel != null) {
            model.setFirst_name(userModel.getData().getUser().getName().split(" ")[0]);
            model.setSeconed_name(userModel.getData().getUser().getName().split(" ")[1]);
            binding.llCode.setVisibility(View.GONE);
            binding.checkbox.setVisibility(View.GONE);
            binding.fl.setVisibility(View.VISIBLE);
            binding.top.setVisibility(View.GONE);
            if (userModel.getData().getUser().getPhoto() != null) {
                binding.icon.setVisibility(View.GONE);
                //Log.e("ldlldl", Tags.base_url + userModel.getData().getPhoto());
                Picasso.get().load(Tags.base_url + userModel.getData().getUser().getPhoto()).into(binding.image);
            }
        } else {
            binding.fl.setVisibility(View.GONE);
            binding.top.setVisibility(View.VISIBLE);
        }
        binding.llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.setModel(model);
        binding.setLang(getLang());
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (selectedReq == READ_REQ) {
                    binding.icon.setVisibility(View.GONE);

                    uri = result.getData().getData();
                    File file = new File(Common.getImagePath(this, uri));
                    Picasso.get().load(file).fit().into(binding.image);

                } else if (selectedReq == CAMERA_REQ) {
                    Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                    binding.icon.setVisibility(View.GONE);
                    uri = getUriFromBitmap(bitmap);
                    if (uri != null) {
                        String path = Common.getImagePath(this, uri);

                        if (path != null) {
                            Picasso.get().load(new File(path)).fit().into(binding.image);

                        } else {
                            Picasso.get().load(uri).fit().into(binding.image);

                        }
                    }
                }
            }
        });

        activitySignupMvvm.userModelMutableLiveData.observe(this, userModel -> {
            setUserModel(userModel);
            setResult(RESULT_OK);
            finish();
        });

        binding.flImage.setOnClickListener(view -> {
            Common.CloseKeyBoard(this, binding.edtPhone);
            openSheet();
        });
        binding.flGallery.setOnClickListener(view -> {
            closeSheet();
            checkReadPermission();
        });

        binding.flCamera.setOnClickListener(view -> {
            closeSheet();
            checkCameraPermission();
        });

        binding.btnCancel.setOnClickListener(view -> closeSheet());

        binding.btnSignup.setOnClickListener(view -> {
            if (model.isDataValid(this)) {


                if (userModel == null) {

                    activitySignupMvvm.signupWithImage(this, model, phone_code, phone, uri);
                } else {
                    activitySignupMvvm.updateProfileWithImage(this, model, uri, userModel);
                }


            }
        });
        binding.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.llCode.setVisibility(View.VISIBLE);
                } else {
                    binding.llCode.setVisibility(View.GONE);
                }
                model.setIscheck(b);
                binding.setModel(model);
            }
        });
    }

    public void openSheet() {
        binding.expandLayout.setExpanded(true, true);
    }

    public void closeSheet() {
        binding.expandLayout.collapse(true);

    }

    public void checkReadPermission() {
        closeSheet();
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }

    public void checkCameraPermission() {

        closeSheet();

        if (ContextCompat.checkSelfPermission(this, write_permission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, camera_permission) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage(CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{camera_permission, write_permission}, CAMERA_REQ);
        }
    }

    private void SelectImage(int req) {
        selectedReq = req;
        Intent intent = new Intent();

        if (req == READ_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");

            launcher.launch(intent);

        } else if (req == CAMERA_REQ) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                launcher.launch(intent);
            } catch (SecurityException e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }


}