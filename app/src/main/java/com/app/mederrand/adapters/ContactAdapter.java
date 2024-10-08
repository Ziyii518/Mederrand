package com.app.mederrand.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.app.mederrand.R;
import com.app.mederrand.models.local.ContactsModel;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    final Context context ;
    ArrayList<ContactsModel> list;
    final InviteFriendContactAdapterCallBacks callBack;

    public ContactAdapter(Context context, ArrayList<ContactsModel> list, InviteFriendContactAdapterCallBacks callBack) {
        this.context = context;
        this.list = list;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.design_invite_contact_friend_row,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ContactsModel singleContact = list.get(position);

        if (singleContact !=null){
            if (!singleContact.getUserName().isEmpty()){
                if (singleContact.getUserName().length()>2){
                    holder.textFirstTwoLetters.setText(singleContact.getUserName().substring(0,2));
                }else {
                    holder.textFirstTwoLetters.setText(singleContact.getUserName());
                }
            }else {
                if (singleContact.getUserPhoneNumber().length()>2){
                    holder.textFirstTwoLetters.setText(singleContact.getUserPhoneNumber().substring(0,2));
                }else {
                    holder.textFirstTwoLetters.setText(singleContact.getUserPhoneNumber());
                }
            }

            holder.tvName.setText(singleContact.getUserName());
            holder.tvPhoneNumber.setText(singleContact.getUserPhoneNumber());

            holder.btnInvite.setOnClickListener(view -> callBack.onInviteFriendContact(singleContact));

        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<ContactsModel> filteredList) {
        list = filteredList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final RelativeLayout btnInvite;
        final TextView tvName;
        final TextView tvPhoneNumber;
        final TextView textFirstTwoLetters;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            btnInvite = itemView.findViewById(R.id.btnInvite);
            textFirstTwoLetters = itemView.findViewById(R.id.textFirstTwoLetters);
        }
    }

    public interface  InviteFriendContactAdapterCallBacks{
        void onInviteFriendContact(ContactsModel contactsModel);
    }
}