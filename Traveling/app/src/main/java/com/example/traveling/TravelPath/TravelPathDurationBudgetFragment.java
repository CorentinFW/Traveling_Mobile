package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathDurationBudgetFragment extends Fragment {

    private static final String PREFS_NAME = "travelpath_duration_budget_state";
    private static final String KEY_DURATION_ID = "duration_id";
    private static final String KEY_MULTIPLE_DAYS_VALUE = "multiple_days_value";
    private static final String KEY_VISIT_START_POSITION = "visit_start_position";
    private static final String KEY_BUDGET_MIN = "budget_min";
    private static final String KEY_BUDGET_MAX = "budget_max";

    private RadioGroup durationGroup;
    private RadioButton multipleDaysRadio;
    private EditText multipleDaysInput;
    private Spinner visitStartSpinner;
    private SeekBar budgetSlider;
    private EditText budgetMinInput;
    private EditText budgetMaxInput;

    private boolean isUpdatingFromSlider;
    private boolean isUpdatingFromText;

    public TravelPathDurationBudgetFragment() {
        super(R.layout.fragment_travelpath_duration_budget);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindViews(view);
        setupVisitStartChoices();
        restoreSavedState();
        setupDurationSelection();
        setupBudgetSync();
        setupContinueButton(view);
    }

    private void bindViews(@NonNull View rootView) {
        durationGroup = rootView.findViewById(R.id.travelpath_duration_group);
        multipleDaysRadio = rootView.findViewById(R.id.travelpath_duration_multiple_days);
        multipleDaysInput = rootView.findViewById(R.id.travelpath_multiple_days_input);
        visitStartSpinner = rootView.findViewById(R.id.travelpath_visit_start_spinner);
        budgetSlider = rootView.findViewById(R.id.travelpath_budget_slider);
        budgetMinInput = rootView.findViewById(R.id.travelpath_budget_min_input);
        budgetMaxInput = rootView.findViewById(R.id.travelpath_budget_max_input);
    }

    private void setupVisitStartChoices() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.travelpath_visit_start_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visitStartSpinner.setAdapter(adapter);
    }

    private void setupDurationSelection() {
        updateMultipleDaysInputState();
        durationGroup.setOnCheckedChangeListener((group, checkedId) -> updateMultipleDaysInputState());
    }

    private void updateMultipleDaysInputState() {
        boolean isMultipleDaysSelected = multipleDaysRadio.isChecked();
        multipleDaysInput.setEnabled(isMultipleDaysSelected);
        if (!isMultipleDaysSelected) {
            multipleDaysInput.setText("");
        }
    }

    private void setupBudgetSync() {
        budgetSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser || isUpdatingFromText) {
                    return;
                }
                isUpdatingFromSlider = true;
                int minValue = parseNonNegativeInt(budgetMinInput, 0);
                int maxValue = Math.max(progress, minValue);
                setEditTextValue(budgetMaxInput, maxValue);
                isUpdatingFromSlider = false;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing to do.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing to do.
            }
        });

        budgetMinInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromSlider || isUpdatingFromText) {
                    return;
                }
                normalizeMinAndMaxFromInputs();
            }
        });

        budgetMaxInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromSlider || isUpdatingFromText) {
                    return;
                }
                normalizeMinAndMaxFromInputs();
            }
        });

        normalizeMinAndMaxFromInputs();
    }

    private void normalizeMinAndMaxFromInputs() {
        isUpdatingFromText = true;

        int minValue = parseNonNegativeInt(budgetMinInput, 0);
        int maxValue = parseNonNegativeInt(budgetMaxInput, budgetSlider.getProgress());

        if (minValue > maxValue) {
            maxValue = minValue;
        }

        setEditTextValue(budgetMinInput, minValue);
        setEditTextValue(budgetMaxInput, maxValue);

        int sliderPosition = Math.min(maxValue, budgetSlider.getMax());
        budgetSlider.setProgress(sliderPosition);

        isUpdatingFromText = false;
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

    private void setEditTextValue(@NonNull EditText input, int value) {
        String newValue = String.valueOf(Math.max(0, value));
        String currentValue = input.getText() != null ? input.getText().toString() : "";
        if (newValue.equals(currentValue)) {
            return;
        }
        input.setText(newValue);
        input.setSelection(newValue.length());
    }

    private void setupContinueButton(@NonNull View rootView) {
        Button continueButton = rootView.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            normalizeMinAndMaxFromInputs();
            saveState();

            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showVisitDateScreen();
            }
        });
    }

    private void restoreSavedState() {
        SharedPreferences preferences = getPreferences();

        String defaultDurationId = getResources().getResourceEntryName(R.id.travelpath_duration_half_day);
        String savedDurationId = preferences.getString(KEY_DURATION_ID, defaultDurationId);
        int durationResId = resolveIdFromEntryName(savedDurationId);
        if (durationResId == View.NO_ID) {
            durationResId = R.id.travelpath_duration_half_day;
        }
        durationGroup.check(durationResId);

        String savedDaysValue = preferences.getString(KEY_MULTIPLE_DAYS_VALUE, "");
        multipleDaysInput.setText(savedDaysValue);
        updateMultipleDaysInputState();

        int savedSpinnerPosition = preferences.getInt(KEY_VISIT_START_POSITION, 0);
        int spinnerItemCount = visitStartSpinner.getCount();
        if (spinnerItemCount > 0) {
            int validPosition = Math.max(0, Math.min(savedSpinnerPosition, spinnerItemCount - 1));
            visitStartSpinner.setSelection(validPosition);
        }

        int savedMin = Math.max(0, preferences.getInt(KEY_BUDGET_MIN, 0));
        int savedMax = Math.max(0, preferences.getInt(KEY_BUDGET_MAX, budgetSlider.getProgress()));
        setEditTextValue(budgetMinInput, savedMin);
        setEditTextValue(budgetMaxInput, savedMax);
        normalizeMinAndMaxFromInputs();
    }

    private int resolveIdFromEntryName(@Nullable String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return View.NO_ID;
        }
        return getResources().getIdentifier(entryName, "id", requireContext().getPackageName());
    }

    private void saveState() {
        int checkedId = durationGroup.getCheckedRadioButtonId();
        String checkedIdName = checkedId != View.NO_ID
                ? getResources().getResourceEntryName(checkedId)
                : getResources().getResourceEntryName(R.id.travelpath_duration_half_day);

        String multipleDaysValue = multipleDaysRadio.isChecked()
                ? (multipleDaysInput.getText() != null ? multipleDaysInput.getText().toString().trim() : "")
                : "";

        int budgetMin = parseNonNegativeInt(budgetMinInput, 0);
        int budgetMax = parseNonNegativeInt(budgetMaxInput, budgetSlider.getProgress());
        if (budgetMin > budgetMax) {
            budgetMax = budgetMin;
        }

        getPreferences().edit()
                .putString(KEY_DURATION_ID, checkedIdName)
                .putString(KEY_MULTIPLE_DAYS_VALUE, multipleDaysValue)
                .putInt(KEY_VISIT_START_POSITION, visitStartSpinner.getSelectedItemPosition())
                .putInt(KEY_BUDGET_MIN, budgetMin)
                .putInt(KEY_BUDGET_MAX, budgetMax)
                .apply();
    }

    @NonNull
    private SharedPreferences getPreferences() {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Nothing to do.
        }
    }
}

