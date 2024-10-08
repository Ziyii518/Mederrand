package com.app.mederrand.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.apis.models.medicines.MedicineDataModel;
import com.app.mederrand.apis.models.medicines.OrderDataModel;
import com.app.mederrand.databinding.ListItemMedicineBinding;
import com.app.mederrand.databinding.ListItemOrderBinding;
import com.app.mederrand.utils.Utilities;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    final Context context;
    final List<OrderDataModel> ordersList;
    final OrdersAdapterCallBack callBack;

    public OrdersAdapter(Context context, List<OrderDataModel> ordersList, OrdersAdapterCallBack callBack) {
        this.context = context;
        this.ordersList = ordersList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemOrderBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderDataModel orderDataModel = ordersList.get(position);
        if (orderDataModel !=null){
            holder.binding.textOrderName.setText(orderDataModel.getMedicineName());
            holder.binding.textPrice.setText(orderDataModel.getOrderTotalPrice());
            holder.binding.textQuantity.setText(orderDataModel.getOrderQuantity());

            Utilities.loadImage(context, orderDataModel.getMedicineImage(), holder.binding.orderImage, holder.binding.imageLoading);
            holder.itemView.setOnClickListener(view -> callBack.onItemClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemOrderBinding binding;

        public ViewHolder(ListItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OrdersAdapterCallBack {
        void onItemClick(int position);
    }
}