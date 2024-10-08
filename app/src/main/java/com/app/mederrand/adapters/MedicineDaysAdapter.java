package com.app.mederrand.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.models.local.MedicineDayDataModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MedicineDaysAdapter extends RecyclerView.Adapter<MedicineDaysAdapter.ViewHolder> {

    final Context context;
    final List<MedicineDayDataModel> list;

    public MedicineDaysAdapter( Context context, List<MedicineDayDataModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.medicine_day_item  , parent , false);
        return new ViewHolder(v);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineDayDataModel dataModel  = list.get(position);
        if (dataModel != null){

            if (dataModel.isSelected()){
                holder.itemView.setBackgroundResource(R.drawable.bg_app_color_dark_3sdp_grey_outline);
                holder.textDay.setTextColor(ContextCompat.getColor(context, R.color.black));
            }else {
                holder.itemView.setBackgroundResource(R.drawable.bg_3sdp_edit_text_border_color);
                holder.textDay.setTextColor(ContextCompat.getColor(context, R.color.edit_text_hint_color));
            }

            holder.textDay.setText(dataModel.getName());

            holder.itemView.setOnClickListener(view -> {
                if (!dataModel.isSelected()){
                    holder.itemView.setBackgroundResource(R.drawable.bg_app_color_dark_3sdp_grey_outline);
                    holder.textDay.setTextColor(ContextCompat.getColor(context, R.color.black));
                    dataModel.setSelected(true);
                }else {
                    holder.itemView.setBackgroundResource(R.drawable.bg_3sdp_edit_text_border_color);
                    holder.textDay.setTextColor(ContextCompat.getColor(context, R.color.edit_text_hint_color));
                    dataModel.setSelected(false);
                }
                notifyDataSetChanged();
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textDay =  itemView.findViewById(R.id.textDay);
        }
    }

}