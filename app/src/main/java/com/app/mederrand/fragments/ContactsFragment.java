package com.app.mederrand.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.adapters.ContactAdapter;
import com.app.mederrand.databinding.FragmentContactsBinding;
import com.app.mederrand.models.local.ContactsModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ContactsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ContactAdapter.InviteFriendContactAdapterCallBacks{
    private  final String[] CONTACTS_PERMISSION = new String[]{Manifest.permission.READ_CONTACTS};
    public static final String[] PROJECTION_NUMBERS = new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
    public static final String[] PROJECTION_DETAILS =  new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.PHOTO_URI};

    FragmentContactsBinding binding;
    Context context;
    ContactAdapter contactAdapter ;
    ArrayList<ContactsModel> contactsList = new ArrayList<>();
    Map<Long, List<String>> phones = new HashMap<>();

    ActivityResultLauncher<Intent> gallerySettingsIntentLauncher;
    ActivityResultLauncher<String[]> requestContactPermissionLauncher, requestContactPermissionLauncherEnd;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater);
        init();
        return binding.getRoot();
    }

    private void init(){
        context = requireActivity();

        binding.loading.setVisibility(View.VISIBLE);
        binding.mainLayout.setVisibility(View.GONE);
        binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);

        initLaunchers();
        initClickListeners();
        checkContactsPermissions();
    }

    private void initClickListeners(){

        binding.edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

    }

    private void filter(String text) {
        ArrayList<ContactsModel> filteredList = new ArrayList<>();
        for (ContactsModel item : contactsList) {
            if (item.getUserName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        contactAdapter.filterList(filteredList);
    }

    private void checkContactsPermissions() {
        if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            initLoaderManager();
        } else {
            requestContactPermissionLauncher.launch(CONTACTS_PERMISSION);
        }

    }

    private void initLaunchers(){
        requestContactPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean permissionGranted = false;
            for (int i = 0; i < result.size(); i++) {
                if (Boolean.TRUE.equals(result.get(CONTACTS_PERMISSION[i]))) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) {
                initLoaderManager();
            } else {
                binding.loading.setVisibility(View.GONE);
                binding.mainLayout.setVisibility(View.GONE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.VISIBLE);

                binding.layoutPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    gallerySettingsIntentLauncher.launch(intent);
                });
            }
        });

        requestContactPermissionLauncherEnd = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result ->  {
            boolean permissionGranted = false;
            for (int i = 0; i < result.size(); i++) {
                if (Boolean.TRUE.equals(result.get(CONTACTS_PERMISSION[i]))) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) {
                showContactsRecyclerView(binding.contactsRecyclerview, contactsList);
            } else {
                binding.loading.setVisibility(View.GONE);
                binding.mainLayout.setVisibility(View.GONE);
                binding.layoutPermissionDenied.getRoot().setVisibility(View.VISIBLE);

                binding.layoutPermissionDenied.btnAppSettings.setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    gallerySettingsIntentLauncher.launch(intent);
                });
            }
        });
        gallerySettingsIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> checkContactsPermissions());


    }

    private void initLoaderManager(){
        binding.loading.setVisibility(View.VISIBLE);
        binding.mainLayout.setVisibility(View.GONE);
        binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 0) {
            return new CursorLoader(context, ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION_NUMBERS,
                    null,
                    null,
                    null
            );
        }
        return new CursorLoader(
                context,
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION_DETAILS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case 0 -> {
                phones = new HashMap<>();
                if (data != null) {
                    while (!data.isClosed() && data.moveToNext()) {
                        long contactId = data.getLong(0);
                        String phone = data.getString(1);
                        List<String> list;
                        if (phones.containsKey(contactId)) {
                            list = phones.get(contactId);
                        } else {
                            list = new ArrayList<>();
                            phones.put(contactId, list);
                        }
                        if (list != null) {
                            list.add(phone);
                        }
                    }
                    data.close();
                }
                LoaderManager.getInstance(this).initLoader(1, null, this);
            }
            case 1 -> {
                if (data != null) {
                    while (!data.isClosed() && data.moveToNext()) {
                        long contactId = data.getLong(0);
                        String name = data.getString(1);
                        List<String> contactPhones = phones.get(contactId);
                        if (contactPhones != null) {
                            for (String phone : contactPhones) {
                                addContact(new ContactsModel(name, phone));
                            }
                        }
                    }
                    data.close();

                    if (context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        showContactsRecyclerView(binding.contactsRecyclerview, contactsList);
                    } else {
                        requestContactPermissionLauncherEnd.launch(CONTACTS_PERMISSION);
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    protected void addContact(ContactsModel contact) {
        contactsList.add(contact);
    }


    private void showContactsRecyclerView(RecyclerView recyclerView, ArrayList<ContactsModel> contactsList) {
        removeDuplicatesAndMakeThemAlphabetically();

        binding.loading.setVisibility(View.GONE);
        binding.layoutPermissionDenied.getRoot().setVisibility(View.GONE);
        binding.mainLayout.setVisibility(View.VISIBLE);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context , LinearLayoutManager.VERTICAL , false));
        contactAdapter = new ContactAdapter(context , contactsList, this);
        recyclerView.setAdapter(contactAdapter);
    }

    @Override
    public void onInviteFriendContact(ContactsModel singleContact) {
        String smsBody = "Hello I am inviting you to join Mederrand App";
        String phoneNumber="smsto:"+ singleContact.getUserPhoneNumber();
        Uri uri = Uri.parse(phoneNumber);
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, smsBody);
        startActivity(sendIntent);
        requireActivity().overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);
    }

    private void removeDuplicatesAndMakeThemAlphabetically(){
        ArrayList<ContactsModel> oldContacts;
        oldContacts = contactsList;

        for (int i = 0; i < oldContacts.size(); i++) {
            if (Objects.equals(oldContacts.get(i).getUserName(), contactsList.get(i).getUserName())){
                contactsList.remove(contactsList.get(i));
            }
        }
        contactsList.sort(Comparator.comparing(ContactsModel::getUserName));
    }

}