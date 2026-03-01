package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FooterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_footer, container, false);

        View home = view.findViewById(R.id.nav_home);
        View services = view.findViewById(R.id.nav_services);
        View book = view.findViewById(R.id.nav_book);
        View profile = view.findViewById(R.id.nav_profile);

        home.setOnClickListener(v -> navigateTo(HomeActivity.class));
        services.setOnClickListener(v -> navigateTo(ServicesActivity.class));
        book.setOnClickListener(v -> navigateTo(BookingActivity.class));
        profile.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        return view;
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
