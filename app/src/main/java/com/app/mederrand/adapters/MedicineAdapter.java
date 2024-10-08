package com.app.mederrand.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.apis.models.medicines.MedicineDataModel;

import com.app.mederrand.databinding.ListItemMedicineBinding;
import com.app.mederrand.utils.Utilities;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {

    final Context context;
    final List<MedicineDataModel> medicinesList;
    final MedicineAdapterCallBack callBack;

    public MedicineAdapter(Context context, List<MedicineDataModel> medicinesList, MedicineAdapterCallBack callBack) {
        this.context = context;
        this.medicinesList = medicinesList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemMedicineBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicineDataModel medicineDataModel = medicinesList.get(position);
        if (medicineDataModel !=null){
            String medicineName = medicineDataModel.getName() + " ( " + medicineDataModel.getUserFullName() +" ) ";
            holder.binding.textMedicineName.setText(medicineName);
            holder.binding.textPrice.setText(medicineDataModel.getPrice());
            holder.binding.textStock.setText(medicineDataModel.getStock());

            Utilities.loadImage(context, medicineDataModel.getImage(), holder.binding.medicineImage, holder.binding.imageLoading);
            holder.itemView.setOnClickListener(view -> callBack.onItemClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return medicinesList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemMedicineBinding binding;

        public ViewHolder(ListItemMedicineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface MedicineAdapterCallBack {
        void onItemClick(int position);
    }
}