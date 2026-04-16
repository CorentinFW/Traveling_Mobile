package com.example.traveling;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_TITLE = "arg_title";
    private static final String ARG_SUBTITLE = "arg_subtitle";
    private static final String ARG_DESCRIPTION = "arg_description";
    private static final String ARG_TITLE_RES = "arg_title_res";
    private static final String ARG_SUBTITLE_RES = "arg_subtitle_res";
    private static final String ARG_DESCRIPTION_RES = "arg_description_res";

    public static PlaceholderFragment newInstance(String title, String subtitle, String description) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String title = resolveTextArg(args, ARG_TITLE, ARG_TITLE_RES);
        String subtitle = resolveTextArg(args, ARG_SUBTITLE, ARG_SUBTITLE_RES);
        String description = resolveTextArg(args, ARG_DESCRIPTION, ARG_DESCRIPTION_RES);

        TextView titleView = view.findViewById(R.id.placeholder_title);
        TextView subtitleView = view.findViewById(R.id.placeholder_subtitle);
        TextView descriptionView = view.findViewById(R.id.placeholder_description);

        titleView.setText(title);
        subtitleView.setText(subtitle);
        descriptionView.setText(description);
    }

    private String resolveTextArg(@Nullable Bundle args, @NonNull String textKey, @NonNull String resKey) {
        if (args == null) {
            return "";
        }

        String text = args.getString(textKey);
        if (text != null && !text.isEmpty()) {
            return text;
        }

        int resId = args.getInt(resKey, 0);
        if (resId != 0) {
            return getString(resId);
        }
        return "";
    }
}


