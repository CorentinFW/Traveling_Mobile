package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareGroup;
import com.example.traveling.travelshare.domain.TravelShareMessage;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

public class TravelShareGroupDetailFragment extends Fragment {

    private static final String ARG_GROUP_ID = "group_id";

    public static TravelShareGroupDetailFragment newInstance(String groupId) {
        TravelShareGroupDetailFragment fragment = new TravelShareGroupDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    public TravelShareGroupDetailFragment() {
        super(R.layout.fragment_travelshare_group_detail);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TravelShareMessageRepository messageRepository = TravelShareDataProvider.messageRepository();
        TravelShareSessionRepository sessionRepository = TravelShareDataProvider.sessionRepository();
        String groupId = getArguments() == null ? null : getArguments().getString(ARG_GROUP_ID);
        TravelShareGroup group = groupId == null ? null : messageRepository.getGroupById(groupId);

        TextView titleView = view.findViewById(R.id.travelshare_group_detail_title);
        TextView helperText = view.findViewById(R.id.travelshare_group_detail_helper_text);
        LinearLayout membersContainer = view.findViewById(R.id.travelshare_group_detail_members_container);
        LinearLayout messagesContainer = view.findViewById(R.id.travelshare_group_detail_messages_container);
        EditText addMemberInput = view.findViewById(R.id.travelshare_group_detail_add_member_input);
        EditText messageInput = view.findViewById(R.id.travelshare_group_detail_input);
        Button addMemberButton = view.findViewById(R.id.travelshare_group_detail_add_member_button);
        Button leaveButton = view.findViewById(R.id.travelshare_group_detail_leave_button);
        Button sendButton = view.findViewById(R.id.travelshare_group_detail_send);

        if (group == null) {
            titleView.setText(R.string.travelshare_group_detail_not_found);
            helperText.setText(R.string.travelshare_group_detail_not_found);
            addMemberInput.setEnabled(false);
            messageInput.setEnabled(false);
            addMemberButton.setEnabled(false);
            leaveButton.setEnabled(false);
            sendButton.setEnabled(false);
            return;
        }

        titleView.setText(group.getName());
        renderMembers(group, membersContainer);
        renderMessages(group, messagesContainer);
        updateButtonsState(sessionRepository, addMemberInput, messageInput, addMemberButton, leaveButton, sendButton);

        if (!sessionRepository.isAuthenticated()) {
            helperText.setText(R.string.travelshare_group_detail_need_login);
            return;
        }

        helperText.setText(getString(R.string.travelshare_group_detail_connected_hint));

        addMemberButton.setOnClickListener(v -> {
            String memberName = addMemberInput.getText().toString();
            if (TextUtils.isEmpty(memberName)) {
                addMemberInput.setError(getString(R.string.travelshare_group_detail_member_empty));
                return;
            }
            boolean added = messageRepository.addMemberToGroup(group.getId(), memberName);
            if (!added) {
                Toast.makeText(requireContext(), R.string.travelshare_group_detail_member_exists, Toast.LENGTH_SHORT).show();
                return;
            }
            addMemberInput.setText("");
            renderMembers(group, membersContainer);
            renderMessages(group, messagesContainer);
        });

        leaveButton.setOnClickListener(v -> {
            boolean removed = messageRepository.removeMemberFromGroup(group.getId(), sessionRepository.getDisplayName());
            if (!removed) {
                Toast.makeText(requireContext(), R.string.travelshare_group_detail_owner_cannot_leave, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), R.string.travelshare_group_detail_left, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString();
            if (TextUtils.isEmpty(text)) {
                messageInput.setError(getString(R.string.travelshare_thread_empty_message));
                return;
            }
            TravelShareMessage sent = messageRepository.sendGroupMessage(
                    group.getId(),
                    sessionRepository.getDisplayName(),
                    text
            );
            if (sent == null) {
                Toast.makeText(requireContext(), R.string.travelshare_thread_empty_message, Toast.LENGTH_SHORT).show();
                return;
            }
            messageInput.setText("");
            renderMessages(group, messagesContainer);
            renderMembers(group, membersContainer);
        });
    }

    private void updateButtonsState(
            TravelShareSessionRepository sessionRepository,
            EditText addMemberInput,
            EditText messageInput,
            Button addMemberButton,
            Button leaveButton,
            Button sendButton
    ) {
        boolean connected = sessionRepository.isAuthenticated();
        addMemberInput.setEnabled(connected);
        messageInput.setEnabled(connected);
        addMemberButton.setEnabled(connected);
        leaveButton.setEnabled(connected);
        sendButton.setEnabled(connected);

        if (!connected) {
            addMemberButton.setText(R.string.travelshare_group_detail_need_login_cta);
            sendButton.setText(R.string.travelshare_group_detail_need_login_cta);
            leaveButton.setText(R.string.travelshare_group_detail_need_login_cta);
        }
    }

    private void renderMembers(TravelShareGroup group, LinearLayout container) {
        container.removeAllViews();
        for (String member : group.getMemberNames()) {
            TextView row = new TextView(requireContext());
            row.setText(getString(R.string.travelshare_group_detail_member_row, member));
            container.addView(row);
        }
    }

    private void renderMessages(TravelShareGroup group, LinearLayout container) {
        container.removeAllViews();
        for (TravelShareMessage message : group.getMessages()) {
            TextView row = new TextView(requireContext());
            row.setText(getString(
                    R.string.travelshare_group_detail_message_row,
                    message.getSenderName(),
                    message.getSentAtLabel(),
                    message.getText()
            ));
            container.addView(row);
        }
    }
}



