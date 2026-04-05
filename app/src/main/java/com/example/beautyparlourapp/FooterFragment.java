package com.example.beautyparlourapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FooterFragment extends Fragment {

    private ImageView ivHome, ivServices, ivBook, ivProfile;
    private TextView tvHome, tvServices, tvBook, tvProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

        View home = view.findViewById(R.id.nav_home);
        View services = view.findViewById(R.id.nav_services);
        View book = view.findViewById(R.id.nav_book);
        View profile = view.findViewById(R.id.nav_profile);

        ivHome = view.findViewById(R.id.iv_nav_home);
        ivServices = view.findViewById(R.id.iv_nav_services);
        ivBook = view.findViewById(R.id.iv_nav_book);
        ivProfile = view.findViewById(R.id.iv_nav_profile);

        tvHome = view.findViewById(R.id.tv_nav_home);
        tvServices = view.findViewById(R.id.tv_nav_services);
        tvBook = view.findViewById(R.id.tv_nav_book);
        tvProfile = view.findViewById(R.id.tv_nav_profile);

        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        services.setOnClickListener(v -> navigateTo(ServicesActivity.class));
        book.setOnClickListener(v -> navigateTo(BookingActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        highlightCurrentTab();

        return view;
    }

    private void highlightCurrentTab() {
        if (getActivity() == null) return;
        Class<?> currentActivity = getActivity().getClass();

        // Colors
        int colorActive = Color.parseColor("#CFA066"); // Premium Gold
        int colorInactive = Color.parseColor("#999999"); // Gray

        // Reset all to inactive
        setTabState(ivHome, tvHome, colorInactive, false);
        setTabState(ivServices, tvServices, colorInactive, false);
        setTabState(ivBook, tvBook, colorInactive, false);
        setTabState(ivProfile, tvProfile, colorInactive, false);

        // Highlight based on current activity
        if (currentActivity.equals(HomeActivity.class)) {
            setTabState(ivHome, tvHome, colorActive, true);
        } else if (currentActivity.equals(ServicesActivity.class)) {
            setTabState(ivServices, tvServices, colorActive, true);
        } else if (currentActivity.equals(BookingActivity.class)) {
            setTabState(ivBook, tvBook, colorActive, true);
        } else if (currentActivity.equals(ProfileActivity.class)) {
            setTabState(ivProfile, tvProfile, colorActive, true);
        }
    }

    private void setTabState(ImageView iv, TextView tv, int color, boolean isActive) {
        iv.setColorFilter(color);
        tv.setTextColor(color);
        if (isActive) {
            tv.setTypeface(null, Typeface.BOLD);
            iv.animate().scaleX(1.15f).scaleY(1.15f).setDuration(200).start();
        } else {
            tv.setTypeface(null, Typeface.NORMAL);
            iv.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
        }
    }

    private void navigateTo(Class<?> destination) {
        if (getActivity() == null) {
            return;
        }

        if (getActivity().getClass().equals(destination)) {
            return;
        }

        Intent intent = new Intent(getActivity(), destination);
        startActivity(intent);
    }
}
