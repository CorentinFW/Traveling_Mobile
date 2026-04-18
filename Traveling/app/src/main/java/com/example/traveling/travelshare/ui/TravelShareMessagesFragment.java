package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

import java.util.ArrayList;
import java.util.List;

public class TravelShareMessagesFragment extends Fragment {

    public TravelShareMessagesFragment() {
        super(R.layout.fragment_travelshare_messages);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();
        TravelShareMessageRepository messageRepository = TravelShareDataProvider.messageRepository();

        TextView helperText = view.findViewById(R.id.travelshare_messages_helper_text);
        ListView listView = view.findViewById(R.id.travelshare_messages_list);

        if (!sessionRepository.isAuthenticated()) {
            helperText.setText(R.string.travelshare_messages_need_login);
            listView.setAdapter(null);
            listView.setEnabled(false);
            return;
        }

        helperText.setText(R.string.travelshare_messages_connected_hint);
        listView.setEnabled(true);

        List<TravelShareConversation> conversations = messageRepository.getConversations();
        List<String> rows = new ArrayList<>();
        for (TravelShareConversation conversation : conversations) {
            rows.add(buildConversationPreview(conversation));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                rows
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, itemView, position, id) -> openThread(conversations.get(position).getId()));
    }

    private String buildConversationPreview(TravelShareConversation conversation) {
        String conversationType = conversation.isGroupConversation()
                ? getString(R.string.travelshare_messages_type_group)
                : getString(R.string.travelshare_messages_type_private);
        return conversation.getTitle() + " (" + conversationType + ")"
                + "\n" + conversation.getLastMessagePreview()
                + "\n" + conversation.getLastActivityLabel();
    }

    private void openThread(String conversationId) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelShareMessageThreadFragment.newInstance(conversationId))
                .addToBackStack("messages_thread")
                .commit();
    }
}
