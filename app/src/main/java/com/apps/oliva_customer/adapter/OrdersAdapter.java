package com.apps.oliva_customer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.oliva_customer.R;
import com.apps.oliva_customer.databinding.OrderRowBinding;
import com.apps.oliva_customer.model.OrderModel;
import com.apps.oliva_customer.uis.activity_my_orders.MyOrderActivity;
import com.apps.oliva_customer.uis.activity_previous_orders.PreviousOrderActivity;

import java.util.List;

import io.paperdb.Paper;

public class OrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;

    public OrdersAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.order_row, parent, false);
        return new MyHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setLang(getLang());
        myHolder.binding.setModel(list.get(position));
        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (context instanceof MyOrderActivity) {
                    MyOrderActivity myOrderActivity = (MyOrderActivity) context;
                    myOrderActivity.show(list.get(holder.getLayoutPosition()));
                } else if (context instanceof PreviousOrderActivity) {
                    PreviousOrderActivity myOrderActivity = (PreviousOrderActivity) context;
                    myOrderActivity.show(list.get(holder.getLayoutPosition()));
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


    public static class MyHolder extends RecyclerView.ViewHolder {
        public OrderRowBinding binding;

        public MyHolder(OrderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    protected String getLang() {
        Paper.init(context);
        String lang = Paper.book().read("lang", "ar");
        return lang;

    }

    public void updateList(List<OrderModel> list) {
        if (list != null) {
            this.list = list;

        }
        notifyDataSetChanged();
    }
}