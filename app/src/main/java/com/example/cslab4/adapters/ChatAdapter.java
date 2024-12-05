package com.example.cslab4.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cslab4.databinding.ItemContainerReceivedMessageBinding;
import com.example.cslab4.databinding.ItemContainerSentMessageBinding;
import com.example.cslab4.models.ChatMessage;

import java.util.List;

/**
 * Adapter class for managing chat messages in a RecyclerView.
 * Handles both sent and received messages with different view types.
 * Supports displaying profile images for received messages.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String sendId;

    // Constants for view types
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    /**
     * Constructs a new ChatAdapter.
     *
     * @param chatMessages List of chat messages to display
     * @param receiverProfileImage Profile image of the message receiver
     * @param sendId ID of the message sender for differentiating message types
     */
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String sendId)  {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.sendId = sendId;
    }

    /**
     * Creates appropriate ViewHolder based on message type (sent or received).
     *
     * @param parent Parent ViewGroup
     * @param viewType Type of view (sent or received message)
     * @return ViewHolder for the appropriate message type
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerSentMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceiverMessageViewHolder(ItemContainerReceivedMessageBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

    }

    /**
     * Binds chat message data to the appropriate ViewHolder.
     *
     * @param holder ViewHolder to bind data to
     * @param position Position of the message in the dataset
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        } else {
            ((ReceiverMessageViewHolder)holder)
                    .setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    /**
     * Determines the type of view needed for the message at given position.
     *
     * @param position Position of the message in the dataset
     * @return VIEW_TYPE_SENT for sent messages, VIEW_TYPE_RECEIVED for received messages
     */
    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(sendId)){
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    /**
     * ViewHolder class for sent messages.
     * Handles the layout and data binding for messages sent by the current user.
     */
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        /**
         * Binds message data to the sent message layout.
         *
         * @param chatMessage Message data to display
         */
        void setData(ChatMessage chatMessage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    /**
     * ViewHolder class for received messages.
     * Handles the layout and data binding for messages received from other users.
     * Includes functionality for displaying the sender's profile image.
     */
    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerReceivedMessageBinding binding;

        public ReceiverMessageViewHolder(ItemContainerReceivedMessageBinding
                                                 itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        /**
         * Binds message data and profile image to the received message layout.
         *
         * @param chatMessage Message data to display
         * @param receiverProfileImage Profile image of the message sender
         */
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage) {
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }
}