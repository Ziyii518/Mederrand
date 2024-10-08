package com.app.mederrand.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.apis.models.inbox.InboxDataModel;
import com.app.mederrand.databinding.ListItemInboxBinding;
import com.app.mederrand.utils.AnyTime;

import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    final Context context;
    final List<InboxDataModel> inboxList;
    final InboxAdapterCallBack callBack;

    public InboxAdapter(Context context, List<InboxDataModel> inboxList, InboxAdapterCallBack callBack) {
        this.context = context;
        this.inboxList = inboxList;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ListItemInboxBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InboxDataModel inboxDataModel = inboxList.get(position);
        if (inboxDataModel !=null){
            holder.binding.textTime.setText(new AnyTime().getTime(Long.parseLong(inboxDataModel.getMessageTime())));
            holder.binding.setInboxModel(inboxDataModel);
            holder.itemView.setOnClickListener(view -> callBack.onItemClick(position));
        }

    }

    @Override
    public int getItemCount() {
        return inboxList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ListItemInboxBinding binding;

        public ViewHolder(ListItemInboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface InboxAdapterCallBack {
        void onItemClick(int position);
    }
}