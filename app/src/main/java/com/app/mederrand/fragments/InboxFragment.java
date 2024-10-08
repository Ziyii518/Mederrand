package com.app.mederrand.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.activities.MessagesActivity;
import com.app.mederrand.adapters.InboxAdapter;
import com.app.mederrand.apis.models.inbox.InboxDataModel;
import com.app.mederrand.databinding.FragmentInboxBinding;
import com.app.mederrand.utils.Utilities;
import com.app.mederrand.utils.services.InboxUsersFetchService;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment implements InboxUsersFetchService.OnInboxUsersFetchedListener, InboxAdapter.InboxAdapterCallBack {
    FragmentInboxBinding binding;
    Context context;

    InboxUsersFetchService inboxUsersFetchService;
    List<InboxDataModel> inboxList = new ArrayList<>();
    InboxAdapter inboxAdapter;

    String userId;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentInboxBinding.inflate(inflater);
        init();
        return binding.getRoot();
    }

    private void init() {
        context = requireActivity();
        userId = Utilities.getString(context, "userId");
        if (inboxList.isEmpty()){
            binding.mainLayout.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            inboxUsersFetchService = new InboxUsersFetchService(userId);
            inboxUsersFetchService.fetchInboxUsers(this);
        }else {
            showInboxRecyclerview(binding.inboxRecyclerview, inboxList);
        }
    }


    @Override
    public void onInboxUsersFetched(List<InboxDataModel> inboxUsers) {
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);
        inboxList = inboxUsers;

        showInboxRecyclerview(binding.inboxRecyclerview, inboxList);
    }

    private void showInboxRecyclerview(RecyclerView recyclerView, List<InboxDataModel> inboxList){
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);

        if (inboxList.isEmpty()){
            binding.layoutEmptyScreen.getRoot().setVisibility(View.VISIBLE);
            binding.layoutEmptyScreen.emptyImage.setImageResource(R.drawable.no_messages_ic);
            binding.layoutEmptyScreen.textEmptyScreenHeading.setText(R.string.text_no_messages_yet);
            binding.layoutEmptyScreen.textEmptyScreenDescription.setText(R.string.text_no_messages_yet_description);
            recyclerView.setVisibility(View.GONE);
        }else {
            binding.layoutEmptyScreen.getRoot().setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setHasFixedSize(true);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, GridLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            inboxAdapter = new InboxAdapter(context, inboxList, this);
            recyclerView.setAdapter(inboxAdapter);
        }
    }


    @Override
    public void onInboxUsersFetchFailed(String errorMessage) {

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.putExtra("otherUserName", inboxList.get(position).getUserDataModel().getName());
        intent.putExtra("otherUserId", inboxList.get(position).getUserDataModel().getUserId());
        intent.putExtra("otherFcmToken", inboxList.get(position).getUserDataModel().getDeviceToken());
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }
}

