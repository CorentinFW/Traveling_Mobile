package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareMessage;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

import java.util.ArrayList;
import java.util.List;

public class TravelShareMessageThreadFragment extends Fragment {

    private static final String ARG_CONVERSATION_ID = "conversation_id";

    public static TravelShareMessageThreadFragment newInstance(String conversationId) {
        Bundle args = new Bundle();
        args.putString(ARG_CONVERSATION_ID, conversationId);

        TravelShareMessageThreadFragment fragment = new TravelShareMessageThreadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public TravelShareMessageThreadFragment() {
        super(R.layout.fragment_travelshare_message_thread);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String conversationId = getArguments() == null ? null : getArguments().getString(ARG_CONVERSATION_ID);
        TravelShareMessageRepository messageRepository = TravelShareDataProvider.messageRepository();
        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();

        TextView titleView = view.findViewById(R.id.travelshare_thread_title);
        TextView helperText = view.findViewById(R.id.travelshare_thread_helper_text);
        ListView listView = view.findViewById(R.id.travelshare_thread_messages_list);
        EditText input = view.findViewById(R.id.travelshare_thread_input);
        Button sendButton = view.findViewById(R.id.travelshare_thread_send_button);

        if (conversationId == null) {
            titleView.setText(R.string.travelshare_thread_not_found);
            helperText.setText(R.string.travelshare_thread_not_found);
            input.setEnabled(false);
            sendButton.setEnabled(false);
            return;
        }

        TravelShareConversation conversation = messageRepository.getConversationById(conversationId);
        if (conversation == null) {
            titleView.setText(R.string.travelshare_thread_not_found);
            helperText.setText(R.string.travelshare_thread_not_found);
            input.setEnabled(false);
            sendButton.setEnabled(false);
            return;
        }

        titleView.setText(conversation.getTitle());
        refreshMessages(listView, messageRepository.getMessages(conversationId));

        if (!sessionRepository.isAuthenticated()) {
            helperText.setText(R.string.travelshare_messages_need_login);
            input.setEnabled(false);
            sendButton.setEnabled(false);
            sendButton.setText(R.string.travelshare_add_need_login_cta);
            return;
        }

        helperText.setText(R.string.travelshare_thread_connected_hint);
        sendButton.setOnClickListener(v -> {
            TravelShareMessage sent = messageRepository.sendMessage(
                    conversationId,
                    sessionRepository.getDisplayName(),
                    input.getText().toString()
            );

            if (sent == null) {
                Toast.makeText(requireContext(), R.string.travelshare_thread_empty_message, Toast.LENGTH_SHORT).show();
                return;
            }

            input.setText("");
            refreshMessages(listView, messageRepository.getMessages(conversationId));
        });
    }

    private void refreshMessages(ListView listView, List<TravelShareMessage> messages) {
        List<String> rows = new ArrayList<>();
        for (TravelShareMessage message : messages) {
            rows.add(message.getSenderName() + " - " + message.getSentAtLabel() + "\n" + message.getText());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                rows
        );
        listView.setAdapter(adapter);
        if (!rows.isEmpty()) {
            listView.setSelection(rows.size() - 1);
        }
    }
}

