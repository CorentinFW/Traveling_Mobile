package com.example.traveling.TravelPath;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.traveling.R;

public class TravelPathVisitDateFragment extends Fragment {

    private static final String PREFS_NAME = "travelpath_visit_date_state";
    private static final String KEY_YEAR = "visit_year";
    private static final String KEY_MONTH = "visit_month";
    private static final String KEY_DAY = "visit_day";

    private DatePicker visitDatePicker;

    public TravelPathVisitDateFragment() {
        super(R.layout.fragment_travelpath_visit_date);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        visitDatePicker = view.findViewById(R.id.travelpath_visit_date_picker);
        restoreOrSetTodayDate();
        setupContinueButton(view);
    }

    private void restoreOrSetTodayDate() {
        SharedPreferences preferences = getPreferences();
        Calendar today = Calendar.getInstance();

        int year = preferences.getInt(KEY_YEAR, today.get(Calendar.YEAR));
        int month = preferences.getInt(KEY_MONTH, today.get(Calendar.MONTH));
        int day = preferences.getInt(KEY_DAY, today.get(Calendar.DAY_OF_MONTH));

        visitDatePicker.updateDate(year, month, day);
    }

    private void setupContinueButton(@NonNull View rootView) {
        Button continueButton = rootView.findViewById(R.id.travelpath_continue_button);
        continueButton.setOnClickListener(v -> {
            saveSelectedDate();

            Fragment parent = getParentFragment();
            if (parent instanceof TravelPathMainFragment) {
                ((TravelPathMainFragment) parent).showEffortScreen();
            }
        });
    }

    private void saveSelectedDate() {
        getPreferences()
                .edit()
                .putInt(KEY_YEAR, visitDatePicker.getYear())
                .putInt(KEY_MONTH, visitDatePicker.getMonth())
                .putInt(KEY_DAY, visitDatePicker.getDayOfMonth())
                .apply();
    }

    @NonNull
    private SharedPreferences getPreferences() {
        Context context = requireContext().getApplicationContext();
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}

