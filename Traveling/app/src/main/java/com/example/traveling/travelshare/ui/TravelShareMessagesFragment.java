package com.example.traveling.travelshare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;
import com.example.traveling.travelshare.data.TravelShareDataProvider;
import com.example.traveling.travelshare.domain.TravelShareConversation;
import com.example.traveling.travelshare.domain.TravelShareGroup;
import com.example.traveling.travelshare.domain.TravelShareMessageRepository;
import com.example.traveling.travelshare.domain.TravelShareSessionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TravelShareMessagesFragment extends Fragment {

    private TravelShareMessageRepository messageRepository;
    private TravelShareSessionRepository sessionRepository;
    private TextView helperText;
    
    private ImageButton addButton;
    private LinearLayout addGroupContainer;
    
    private EditText groupNameInput;
    private EditText groupMembersInput;
    private Button createGroupButton;
    
    private ListView listMerged;

    public TravelShareMessagesFragment() {
        super(R.layout.fragment_travelshare_messages);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionRepository = TravelShareDataProvider.sessionRepository();
        messageRepository = TravelShareDataProvider.messageRepository();

        helperText = view.findViewById(R.id.travelshare_messages_helper_text);
        
        addButton = view.findViewById(R.id.travelshare_messages_add_btn);
        addGroupContainer = view.findViewById(R.id.travelshare_messages_add_layout);
        
        groupNameInput = view.findViewById(R.id.travelshare_messages_group_name_input);
        groupMembersInput = view.findViewById(R.id.travelshare_messages_group_members_input);
        createGroupButton = view.findViewById(R.id.travelshare_messages_group_create_button);
        
        listMerged = view.findViewById(R.id.travelshare_messages_list);

        if (!sessionRepository.isAuthenticated()) {
            helperText.setText(R.string.travelshare_messages_need_login);
            addButton.setEnabled(false);
            groupNameInput.setEnabled(false);
            groupMembersInput.setEnabled(false);
            createGroupButton.setEnabled(false);
            listMerged.setAdapter(null);
            listMerged.setEnabled(false);
            return;
        }

        helperText.setText(R.string.travelshare_messages_connected_hint);
        addButton.setEnabled(true);
        groupNameInput.setEnabled(true);
        groupMembersInput.setEnabled(true);
        createGroupButton.setEnabled(true);
        listMerged.setEnabled(true);

        refreshList();

        addButton.setOnClickListener(v -> {
            if (addGroupContainer.getVisibility() == View.VISIBLE) {
                addGroupContainer.setVisibility(View.GONE);
            } else {
                addGroupContainer.setVisibility(View.VISIBLE);
            }
        });

        createGroupButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString();
            if (TextUtils.isEmpty(groupName)) {
                groupNameInput.setError(getString(R.string.travelshare_groups_name_required));
                return;
            }

            List<String> initialMembers = parseMembers(groupMembersInput.getText().toString());
            TravelShareGroup createdGroup = messageRepository.createGroup(
                    groupName,
                    sessionRepository.getDisplayName(),
                    initialMembers
            );

            if (createdGroup == null) {
                Toast.makeText(requireContext(), R.string.travelshare_groups_create_error, Toast.LENGTH_SHORT).show();
                return;
            }

            groupNameInput.setText("");
            groupMembersInput.setText("");
            addGroupContainer.setVisibility(View.GONE);
            refreshList();
            Toast.makeText(requireContext(), R.string.travelshare_groups_created, Toast.LENGTH_SHORT).show();
            openGroupDetail(createdGroup.getId());
        });

        listMerged.setOnItemClickListener((parent, itemView, position, id) -> {
            Object item = listMerged.getAdapter().getItem(position);
            if (item instanceof TravelShareConversation) {
                openThread(((TravelShareConversation) item).getId());
            } else if (item instanceof TravelShareGroup) {
                openGroupDetail(((TravelShareGroup) item).getId());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (messageRepository == null || sessionRepository == null) {
            return;
        }

        if (!sessionRepository.isAuthenticated()) {
            helperText.setText(R.string.travelshare_messages_need_login);
            listMerged.setAdapter(null);
            return;
        }

        helperText.setText(R.string.travelshare_messages_connected_hint);
        refreshList();
    }

    private void refreshList() {
        List<TravelShareConversation> conversations = messageRepository.getConversations();
        List<TravelShareGroup> groups = messageRepository.getGroups();
        
        List<Object> mergedList = new ArrayList<>();
        mergedList.addAll(conversations);
        mergedList.addAll(groups);
        
        // You could sort mergedList by recent activity here if you have dates, but for now we just append.
        
        TravelShareMessageAdapter adapter = new TravelShareMessageAdapter(
                requireContext(),
                mergedList,
                sessionRepository.getDisplayName()
        );
        listMerged.setAdapter(adapter);
    }

    private List<String> parseMembers(String rawMembers) {
        List<String> members = new ArrayList<>();
        if (TextUtils.isEmpty(rawMembers)) {
            return members;
        }

        for (String candidate : Arrays.asList(rawMembers.split(","))) {
            String normalized = candidate.trim();
            if (!normalized.isEmpty() && !members.contains(normalized)) {
                members.add(normalized);
            }
        }
        return members;
    }

    private void openThread(String conversationId) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelShareMessageThreadFragment.newInstance(conversationId))
                .addToBackStack("messages_thread")
                .commit();
    }

    private void openGroupDetail(String groupId) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.travelshare_fragment_container, TravelShareGroupDetailFragment.newInstance(groupId))
                .addToBackStack("group_detail")
                .commit();
    }
}