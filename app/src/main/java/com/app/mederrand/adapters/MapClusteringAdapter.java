package com.app.mederrand.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.apis.models.clustering.MederrandMapClusterModel;
import com.app.mederrand.databinding.ListItemDriverBinding;
import com.app.mederrand.models.local.UserDataModel;
import com.app.mederrand.utils.Utilities;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MapClusteringAdapter extends RecyclerView.Adapter<MapClusteringAdapter.ViewHolder> {

    final Context context;
    final List<MederrandMapClusterModel> list;
    final MapClusteringAdapterCallBack callBack;


    public MapClusteringAdapter(Context context, List<MederrandMapClusterModel> list, MapClusteringAdapterCallBack callBack) {
        this.context = context;
        this.list = list;
        this.callBack = callBack;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemDriverBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        MederrandMapClusterModel mapFilterDataModel = list.get(position);
        if (mapFilterDataModel !=null){
            UserDataModel userDataModel = mapFilterDataModel.getUserDataModel();
            if (userDataModel !=null) {
                holder.binding.textDriverName.setText(userDataModel.getName());
                holder.binding.textUserAddress.setText(userDataModel.getUserAddress());

                Utilities.loadImage(context, userDataModel.getProfileImage(), holder.binding.driverImage, holder.binding.imageLoading);
                holder.itemView.setOnClickListener(view -> callBack.onClusteringUserClicked(userDataModel.getUserId()));
            }

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {

        ListItemDriverBinding binding;

        public ViewHolder(ListItemDriverBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface MapClusteringAdapterCallBack {
        void onClusteringUserClicked(String userId);
    }
}