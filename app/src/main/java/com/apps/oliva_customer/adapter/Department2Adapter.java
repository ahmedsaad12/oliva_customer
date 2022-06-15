package com.apps.oliva_customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.databinding.CategoryRowBinding;
import com.apps.oliva_customer.databinding.DepartmentItemRowBinding;
import com.apps.oliva_customer.model.DepartmentModel;
import com.apps.oliva_customer.uis.activity_home.fragments_home_navigaion.FragmentDepartment;
import com.apps.oliva_customer.uis.activity_home.fragments_home_navigaion.FragmentHome;

import java.util.List;

public class Department2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<DepartmentModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;

    public Department2Adapter(Context context, Fragment fragment) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DepartmentItemRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.department_item_row, parent, false);
        return new MyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fragment instanceof FragmentDepartment) {
                    FragmentDepartment fragmentHome = (FragmentDepartment) fragment;
                    fragmentHome.showcategory(list.get(holder.getLayoutPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void updateList(List<DepartmentModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public static class MyHolder extends RecyclerView.ViewHolder {
        public DepartmentItemRowBinding binding;

        public MyHolder(DepartmentItemRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
