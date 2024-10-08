package com.app.mederrand.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.databinding.ReceiverMessagesRowBinding;
import com.app.mederrand.databinding.SenderMessageRowBinding;
import com.app.mederrand.models.local.MessageDataModel;
import com.app.mederrand.utils.AnyTime;
import com.app.mederrand.utils.Utilities;

import java.util.List;
import java.util.Objects;

public class UserChatAdapter extends RecyclerView.Adapter<UserChatAdapter.ViewHolder> {
    private static final int VIEW_TYPE_SENDER = 0;
    private static final int VIEW_TYPE_RECEIVER = 1;

    Context context;
    String userId;
    List<MessageDataModel> messagesList;

    public UserChatAdapter(Context context, List<MessageDataModel> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
        userId = Utilities.getString(context, "userId");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_SENDER) {
            return new ViewHolder(SenderMessageRowBinding.inflate(LayoutInflater.from(context), parent, false));
        }
        else {
            return new ViewHolder(ReceiverMessagesRowBinding.inflate(LayoutInflater.from(context), parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageDataModel message = messagesList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENDER){
            // Work With Sender Messages Here
            SenderMessageRowBinding binding = holder.senderBinding;
            binding.senderMessageText.setText(message.getMessage());
            binding.textSenderTime.setText(new AnyTime().getTime(Long.parseLong(message.getTime())));
        }
        else {
            ReceiverMessagesRowBinding binding = holder.receiverBinding;
            binding.textReceiverName.setText(message.getUser().getName());
            binding.receiverTextMessage.setText(message.getMessage());
            binding.textReceiverTime.setText(new AnyTime().getTime(Long.parseLong(message.getTime())));
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (Objects.equals(messagesList.get(position).getSenderId(), userId)) {
            return VIEW_TYPE_SENDER;
        } else {
            return VIEW_TYPE_RECEIVER;
        }
    }

    public void add(MessageDataModel r) {
        messagesList.add(r);
        notifyItemInserted(messagesList.size() - 1);
    }

    public void addAll(List<MessageDataModel> moveResults) {
        for (MessageDataModel result : moveResults) {
            add(result);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        SenderMessageRowBinding senderBinding;
        ReceiverMessagesRowBinding receiverBinding;

        public ViewHolder(SenderMessageRowBinding binding) {
            super(binding.getRoot());
            this.senderBinding = binding;
        }

        public ViewHolder(ReceiverMessagesRowBinding binding) {
            super(binding.getRoot());
            this.receiverBinding = binding;
        }

    }
}
