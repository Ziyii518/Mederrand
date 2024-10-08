package com.app.mederrand.utils.services;

import androidx.annotation.NonNull;

import com.app.mederrand.apis.models.inbox.InboxDataModel;
import com.app.mederrand.models.local.MessageDataModel;
import com.app.mederrand.models.local.UserDataModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxUsersFetchService {

    DatabaseReference usersRef;
    DatabaseReference chatRoomsRef;
    String currentUserUID;

    public InboxUsersFetchService(String currentUserUID) {
        this.currentUserUID = currentUserUID;
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chats");
    }

    public void fetchInboxUsers(final OnInboxUsersFetchedListener listener) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Map<String, UserDataModel> usersMap = new HashMap<>();

                // Populate usersMap with user information
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    UserDataModel user = userSnapshot.getValue(UserDataModel.class);
                    if (user != null) {
                        usersMap.put(user.getUserId(), user);
                    }
                }

                chatRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, String> lastMessagesMap = new HashMap<>();
                        Map<String, Long> timestampsMap = new HashMap<>();

                        // Iterate through each chat room
                        for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                            String chatRoomID = chatRoomSnapshot.getKey();

                            // Split the chat room ID to extract user IDs
                            String[] userIDs = chatRoomID.split("_");

                            // Determine if the current user is one of the participants
                            boolean currentUserInvolved = userIDs[0].equals(currentUserUID) || userIDs[1].equals(currentUserUID);

                            // Get the last message in the chat room
                            DataSnapshot lastMessageSnapshot = chatRoomSnapshot.getChildren().iterator().next();
                            MessageDataModel lastMessage = lastMessageSnapshot.getValue(MessageDataModel.class);

                            // Check if the chat room contains any messages and if the current user is involved
                            if (lastMessage != null && currentUserInvolved) {
                                // Determine the other user ID in the chat
                                String otherUserID = userIDs[0].equals(currentUserUID) ? userIDs[1] : userIDs[0];

                                // Update the last message and timestamp for this user
                                lastMessagesMap.put(otherUserID, lastMessage.getMessage());
                                timestampsMap.put(otherUserID, Long.parseLong(lastMessage.getTime()));
                            }
                        }

                        // Now lastMessagesMap contains the last message for each user
                        // You can use this map to fetch inbox users
                        List<InboxDataModel> inboxUsers = new ArrayList<>();
                        for (Map.Entry<String, String> entry : lastMessagesMap.entrySet()) {
                            String otherUserID = entry.getKey();
                            String lastMessage = entry.getValue();
                            Long timestamp = timestampsMap.get(otherUserID);

                            // Fetch user information from the map
                            UserDataModel otherUser = usersMap.get(otherUserID);

                            // Create an InboxUser object and add it to the list
                            if (otherUser != null) {
                                inboxUsers.add(new InboxDataModel(lastMessage, String.valueOf(timestamp), otherUser));
                            }
                        }

                        // Notify the listener with the inbox users
                        listener.onInboxUsersFetched(inboxUsers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        listener.onInboxUsersFetchFailed(databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                listener.onInboxUsersFetchFailed(databaseError.getMessage());
            }
        });
    }

    public interface OnInboxUsersFetchedListener {
        void onInboxUsersFetched(List<InboxDataModel> inboxUsers);
        void onInboxUsersFetchFailed(String errorMessage);
    }
}


