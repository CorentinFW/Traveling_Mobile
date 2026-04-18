package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathEffortFragment extends Fragment {

    private static final String PREFS_NAME = "travelpath_effort_state";
    private static final String KEY_EFFORT_ID = "effort_id";
    private static final String KEY_ADULT_COUNT = "adult_count";
    private static final String KEY_CHILDREN_COUNT = "children_count";
    private static final String KEY_CHECKED_OPTIONS = "checked_options";

    private RadioGroup effortGroup;
    private EditText adultCountInput;
    private EditText childrenCountInput;
    private CheckBox withChildren;
    private CheckBox withSenior;
    private CheckBox withPrmAccessibility;
    private CheckBox sensitiveCold;
    private CheckBox sensitiveHeat;
    private CheckBox sensitiveHumidity;

    private boolean isNormalizingInputs;

    public TravelPathEffortFragment() {
        super(R.layout.fragment_travelpath_effort);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupInputValidation();
        setupChildrenToggleBehavior();
        restoreState();
        setupContinueButton(view);
    }

    private void bindViews(@NonNull View rootView) {
        effortGroup = rootView.findViewById(R.id.travelpath_effort_group);
        adultCountInput = rootView.findViewById(R.id.travelpath_adult_count_input);
        childrenCountInput = rootView.findViewById(R.id.travelpath_children_count_input);

        withChildren = rootView.findViewById(R.id.travelpath_with_children);
        withSenior = rootView.findViewById(R.id.travelpath_with_senior);
        withPrmAccessibility = rootView.findViewById(R.id.travelpath_with_prm_accessibility);
        sensitiveCold = rootView.findViewById(R.id.travelpath_sensitive_cold);
        sensitiveHeat = rootView.findViewById(R.id.travelpath_sensitive_heat);
        sensitiveHumidity = rootView.findViewById(R.id.travelpath_sensitive_humidity);
    }

    private void setupInputValidation() {
        adultCountInput.addTextChangedListener(new NonNegativeWatcher(adultCountInput));
        childrenCountInput.addTextChangedListener(new NonNegativeWatcher(childrenCountInput));
    }

    private void setupChildrenToggleBehavior() {
        withChildren.setOnCheckedChangeListener((buttonView, isChecked) -> updateChildrenInputState(isChecked));
    }

    private void updateChildrenInputState(boolean isEnabled) {
        childrenCountInput.setEnabled(isEnabled);
        if (!isEnabled) {
            setInputValue(childrenCountInput, 0);
        }
    }

    private void restoreState() {
        SharedPreferences preferences = getPreferences();

        String defaultEffortId = getResources().getResourceEntryName(R.id.travelpath_effort_low);
        String savedEffortId = preferences.getString(KEY_EFFORT_ID, defaultEffortId);
        int effortId = resolveIdFromEntryName(savedEffortId);
        if (effortId == View.NO_ID) {
            effortId = R.id.travelpath_effort_low;
        }
        effortGroup.check(effortId);

        int adultCount = Math.max(0, preferences.getInt(KEY_ADULT_COUNT, 0));
        int childrenCount = Math.max(0, preferences.getInt(KEY_CHILDREN_COUNT, 0));
        setInputValue(adultCountInput, adultCount);
        setInputValue(childrenCountInput, childrenCount);

        Set<String> checkedOptions = preferences.getStringSet(KEY_CHECKED_OPTIONS, new HashSet<>());
        applyCheckedState(withChildren, checkedOptions);
        applyCheckedState(withSenior, checkedOptions);
        applyCheckedState(withPrmAccessibility, checkedOptions);
        applyCheckedState(sensitiveCold, checkedOptions);
        applyCheckedState(sensitiveHeat, checkedOptions);
        applyCheckedState(sensitiveHumidity, checkedOptions);

        updateChildrenInputState(withChildren.isChecked());
        if (withChildren.isChecked()) {
            setInputValue(childrenCountInput, childrenCount);
        }
    }

    private void applyCheckedState(@NonNull CheckBox checkBox, @NonNull Set<String> checkedOptions) {
        String optionId = getResources().getResourceEntryName(checkBox.getId());
        checkBox.setChecked(checkedOptions.contains(optionId));
    }

    private void setupContinueButton(@NonNull View rootView) {
        Button continueButton = rootView.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            normalizeInputValues();
            saveState();

            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showSummaryScreen();
            }
        });
    }

    private void normalizeInputValues() {
        int adultCount = parseNonNegativeInt(adultCountInput, 0);
        setInputValue(adultCountInput, adultCount);

        int childrenCount = withChildren.isChecked()
                ? parseNonNegativeInt(childrenCountInput, 0)
                : 0;
        setInputValue(childrenCountInput, childrenCount);
    }

    private void saveState() {
        int checkedEffortId = effortGroup.getCheckedRadioButtonId();
        String checkedEffortName = checkedEffortId != View.NO_ID
                ? getResources().getResourceEntryName(checkedEffortId)
                : getResources().getResourceEntryName(R.id.travelpath_effort_low);

        int adultCount = parseNonNegativeInt(adultCountInput, 0);
        int childrenCount = withChildren.isChecked()
                ? parseNonNegativeInt(childrenCountInput, 0)
                : 0;

        Set<String> checkedOptions = new HashSet<>();
        for (CheckBox checkBox : getAllOptionCheckBoxes()) {
            if (!checkBox.isChecked()) {
                continue;
            }
            checkedOptions.add(getResources().getResourceEntryName(checkBox.getId()));
        }

        getPreferences().edit()
                .putString(KEY_EFFORT_ID, checkedEffortName)
                .putInt(KEY_ADULT_COUNT, adultCount)
                .putInt(KEY_CHILDREN_COUNT, childrenCount)
                .putStringSet(KEY_CHECKED_OPTIONS, checkedOptions)
                .apply();
    }

    @NonNull
    private List<CheckBox> getAllOptionCheckBoxes() {
        return Arrays.asList(
                withChildren,
                withSenior,
                withPrmAccessibility,
                sensitiveCold,
                sensitiveHeat,
                sensitiveHumidity
        );
    }

    private int parseNonNegativeInt(@NonNull EditText input, int fallback) {
        String raw = input.getText() != null ? input.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            return Math.max(0, fallback);
        }

        try {
            long value = Long.parseLong(raw);
            if (value < 0L) {
                return 0;
            }
            if (value > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return (int) value;
        } catch (NumberFormatException ignored) {
            return Math.max(0, fallback);
        }
    }

    private void setInputValue(@NonNull EditText input, int value) {
        String normalized = String.valueOf(Math.max(0, value));
        String current = input.getText() != null ? input.getText().toString() : "";
        if (normalized.equals(current)) {
            return;
        }

        isNormalizingInputs = true;
        input.setText(normalized);
        input.setSelection(normalized.length());
        isNormalizingInputs = false;
    }

    private int resolveIdFromEntryName(@Nullable String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return View.NO_ID;
        }
        return getResources().getIdentifier(entryName, "id", requireContext().getPackageName());
    }

    @NonNull
    private SharedPreferences getPreferences() {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private final class NonNegativeWatcher implements TextWatcher {

        private final EditText input;

        private NonNegativeWatcher(@NonNull EditText input) {
            this.input = input;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do.
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isNormalizingInputs) {
                return;
            }
            int normalizedValue = parseNonNegativeInt(input, 0);
            setInputValue(input, normalizedValue);
        }
    }
}

