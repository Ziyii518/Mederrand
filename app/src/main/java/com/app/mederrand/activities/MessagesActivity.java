package com.app.mederrand.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.mederrand.R;
import com.app.mederrand.adapters.UserChatAdapter;
import com.app.mederrand.databinding.ActivityMessagesBinding;
import com.app.mederrand.models.local.MessageDataModel;
import com.app.mederrand.models.local.MessageUserModel;
import com.app.mederrand.utils.MederrandApp;
import com.app.mederrand.utils.Utilities;
import com.app.mederrand.utils.notifications.FcmNotificationBuilder;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class MessagesActivity extends AppCompatActivity{

    ActivityMessagesBinding binding;
    Context context;
    OnBackPressedCallback onBackPressedCallback;

    UserChatAdapter userChatAdapter;
    List<MessageDataModel> messagesList = new ArrayList<>();

    String userId, fcmToken, userFullName, userName, userEmail, userProfileImage, otherUserName, otherUserId, otherFcmToken;

    ActivityResultLauncher<Intent> textToSpeechLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Utilities.setCustomStatusAndNavColor(MessagesActivity.this, R.color.app_color_dark, R.color.app_color_bg);
        context = MessagesActivity.this;

        initOnBackPressed();
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

        // GET CURRENT USER DATA
        userId = Utilities.getString(context, "userId");
        userFullName = Utilities.getString(context, "userFullName");
        userName = Utilities.getString(context, "userName");
        userEmail = Utilities.getString(context, "userEmail");
        userProfileImage = Utilities.getString(context, "userProfileImage");
        fcmToken = Utilities.getString(context, "fcmToken");

        // GET DATA FROM PREVIOUS SCREEN
        otherUserName = getIntent().getStringExtra("otherUserName");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherFcmToken = getIntent().getStringExtra("otherFcmToken");

        // Init Messages Reference

        binding.textFullName.setText(otherUserName);

        binding.mainLayout.setVisibility(View.GONE);
        binding.loading.setVisibility(View.VISIBLE);
        loadMessages(userId, otherUserId);

        initClickListeners();
        initLaunchers();
    }

    private void initClickListeners(){
        binding.btnBack.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        binding.btnSend.setOnClickListener(v -> {
            String message = binding.edMessage.getText().toString();
            if (!TextUtils.isEmpty(message)) {
                binding.edMessage.setEnabled(false);
                binding.messageIc.setVisibility(View.GONE);
                binding.sendMessageLoading.setVisibility(View.VISIBLE);
                binding.edMessage.setText("");
                addMessage(message);
            }
        });

        binding.btnMic.setOnClickListener(view -> startSpeech());
    }


    private void initLaunchers(){
        textToSpeechLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Here, no request code
                        if (result.getData() != null) {
                            ArrayList<String> resultData = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            String messageText =  Objects.requireNonNull(resultData).get(0);
                            binding.edMessage.setText(messageText);
                            binding.edMessage.setSelection(messageText.length());
                            binding.edMessage.requestFocus();
                        }
                    }
                });
    }

    //creates an intent for speech recognition
    //
    private void startSpeech() {
        //Specifies the language for recognition
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        //Provides a prompt message to be displayed to the user during speech recognition
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            textToSpeechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, " " + e.getMessage(),
                            Toast.LENGTH_SHORT)
                    .show();
        }
    }


    public void loadMessages(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("chats").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the chat room exists for both room types
                if (dataSnapshot.hasChild(room_type_1) || dataSnapshot.hasChild(room_type_2)) {
                    // Create ChildEventListener for room_type_1
                    ChildEventListener roomType1Listener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                            MessageDataModel chat = dataSnapshot.getValue(MessageDataModel.class);
                            if (chat != null) {
                                messagesList.add(chat);
                                // Show the updated list of messages in the RecyclerView
                                messagesList.sort(Comparator.comparingLong(message -> Long.parseLong(message.getTime())));
                                showAllMessagesRecycler(binding.recyclerviewMessages, messagesList);
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    };

                    // Create ChildEventListener for room_type_2
                    ChildEventListener roomType2Listener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                            MessageDataModel chat = dataSnapshot.getValue(MessageDataModel.class);
                            if (chat != null) {
                                messagesList.add(chat);
                                // Show the updated list of messages in the RecyclerView
                                messagesList.sort(Comparator.comparingLong(message -> Long.parseLong(message.getTime())));
                                showAllMessagesRecycler(binding.recyclerviewMessages, messagesList);
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {}

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {}

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    };

                    // Add ChildEventListeners for room_type_1 and room_type_2
                    if (dataSnapshot.hasChild(room_type_1)) {
                        databaseReference.child("chats").child(room_type_1).addChildEventListener(roomType1Listener);
                    }
                    if (dataSnapshot.hasChild(room_type_2)) {
                        databaseReference.child("chats").child(room_type_2).addChildEventListener(roomType2Listener);
                    }
                } else {
                    // Handle the case where the chat room doesn't exist
                    // You may show a message to the user indicating that there are no messages
                    binding.mainLayout.setVisibility(View.VISIBLE);
                    binding.loading.setVisibility(View.GONE);
                    Utilities.setCustomStatusAndNavColor(MessagesActivity.this, R.color.app_color_dark, R.color.white);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });

    }

    private void addMessage(String message) {
        MessageDataModel messageDataModel = new MessageDataModel(message, System.currentTimeMillis() +"", userId, otherUserId,new MessageUserModel(userId, userFullName, userName, userEmail, userProfileImage));
        sendMessageToFirebaseUser(messageDataModel, userFullName, fcmToken, otherFcmToken);
    }


    public void sendMessageToFirebaseUser(final MessageDataModel chat, final String senderName, String senderToken, final String receiverFirebaseToken) {
        final String room_type_1 = chat.getSenderId() + "_" + chat.getReceiverId();
        final String room_type_2 = chat.getReceiverId() + "_" + chat.getSenderId();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("chats").getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String messageId = UUID.randomUUID().toString();

                if (dataSnapshot.hasChild(room_type_1)) {
                    databaseReference.child("chats").child(room_type_1).child(messageId).setValue(chat);
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    databaseReference.child("chats").child(room_type_2).child(messageId).setValue(chat);
                } else {
                    databaseReference.child("chats").child(room_type_1).child(messageId).setValue(chat);
                }
                // send push notification to the receiver
                sendPushNotificationToReceiver(senderName, chat.getMessage(), chat.getSenderId(), senderToken, receiverFirebaseToken);
                binding.edMessage.setEnabled(true);
                binding.messageIc.setVisibility(View.VISIBLE);
                binding.sendMessageLoading.setVisibility(View.GONE);
                if (messagesList.isEmpty()){
                    loadMessages(userId, otherUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sendPushNotificationToReceiver(String username, String message, String uid, String firebaseToken, String receiverFirebaseToken) {
        FcmNotificationBuilder.initialize()
                .type("message")
                .title(username)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    private void showAllMessagesRecycler(RecyclerView recyclerView, List<MessageDataModel> listItems) {
        binding.mainLayout.setVisibility(View.VISIBLE);
        binding.loading.setVisibility(View.GONE);
        Utilities.setCustomStatusAndNavColor(MessagesActivity.this, R.color.app_color_dark, R.color.white);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        userChatAdapter = new UserChatAdapter(context, listItems);
        recyclerView.setAdapter(userChatAdapter);
        int scrollPosition = listItems.size()-1;
        recyclerView.scrollToPosition(scrollPosition);

    }

    private void initOnBackPressed(){
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.activity_enter, R.anim.activity_exit);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        MederrandApp.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MederrandApp.setChatActivityOpen(false);
    }

}