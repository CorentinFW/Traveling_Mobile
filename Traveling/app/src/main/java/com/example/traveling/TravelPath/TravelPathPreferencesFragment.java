package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathPreferencesFragment extends Fragment {

    private static final String PREFS_NAME = "travelpath_preferences_state";
    private static final String KEY_SELECTED_CATEGORY_IDS = "selected_category_ids";

    private final List<CheckBox> categoryCheckBoxes = new ArrayList<>();

    public TravelPathPreferencesFragment() {
        super(R.layout.fragment_travelpath_preferences);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindCategoryCheckBoxes(view);
        restoreSelectedCategories();
        setupContinueButton(view);
    }

    private void bindCategoryCheckBoxes(@NonNull View rootView) {
        categoryCheckBoxes.clear();
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_restaurant));
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_culture));
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_monument));
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_leisure));
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_shopping));
        categoryCheckBoxes.add(rootView.findViewById(R.id.travelpath_category_events));
    }

    private void restoreSelectedCategories() {
        Set<String> selectedIds = getPreferences().getStringSet(KEY_SELECTED_CATEGORY_IDS, new HashSet<>());
        for (CheckBox checkBox : categoryCheckBoxes) {
            String categoryId = getResources().getResourceEntryName(checkBox.getId());
            checkBox.setChecked(selectedIds.contains(categoryId));
        }
    }

    private void setupContinueButton(@NonNull View rootView) {
        Button continueButton = rootView.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            saveSelectedCategories();

            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showResultsScreen();
            }
        });
    }

    private void saveSelectedCategories() {
        Set<String> selectedIds = new HashSet<>();
        for (CheckBox checkBox : categoryCheckBoxes) {
            if (!checkBox.isChecked()) {
                continue;
            }
            selectedIds.add(getResources().getResourceEntryName(checkBox.getId()));
        }

        getPreferences().edit().putStringSet(KEY_SELECTED_CATEGORY_IDS, selectedIds).apply();
    }

    @NonNull
    private SharedPreferences getPreferences() {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

