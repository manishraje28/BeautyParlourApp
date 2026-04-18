package com.example.beautyparlourapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdminFooterFragment extends Fragment {

    // Define colors
    private final int COLOR_ACTIVE = 0xFFF28482; // Pinkish Red from Admin Theme
    private final int COLOR_INACTIVE = 0xFFA5A5A5; // Gray

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_footer, container, false);

        // Get views
        LinearLayout tabBookings = view.findViewById(R.id.tab_admin_bookings);
        LinearLayout tabServices = view.findViewById(R.id.tab_admin_services);
        LinearLayout tabOffers = view.findViewById(R.id.tab_admin_offers);
        LinearLayout tabProfile = view.findViewById(R.id.tab_admin_profile);
        LinearLayout tabGallery = view.findViewById(R.id.tab_admin_gallery);

        ImageView icBookings = view.findViewById(R.id.ic_admin_bookings);
        ImageView icServices = view.findViewById(R.id.ic_admin_services);
        ImageView icOffers = view.findViewById(R.id.ic_admin_offers);
        ImageView icProfile = view.findViewById(R.id.ic_admin_profile);
        ImageView icGallery = view.findViewById(R.id.ic_admin_gallery);

        TextView tvBookings = view.findViewById(R.id.tv_admin_bookings);
        TextView tvServices = view.findViewById(R.id.tv_admin_services);
        TextView tvOffers = view.findViewById(R.id.tv_admin_offers);
        TextView tvProfile = view.findViewById(R.id.tv_admin_profile);
        TextView tvGallery = view.findViewById(R.id.tv_admin_gallery);

        // Determine active activity to highlight correctly
        String currentActivity = getActivity().getClass().getSimpleName();

        switch (currentActivity) {
            case "AdminDashboardActivity":
                setActive(icBookings, tvBookings);
                break;
            case "AdminServicesActivity":
                setActive(icServices, tvServices);
                break;
            case "AdminOffersActivity":
                setActive(icOffers, tvOffers);
                break;
            case "ProfileActivity":
                setActive(icProfile, tvProfile);
                break;
            case "AdminGalleryActivity":
                setActive(icGallery, tvGallery);
                break;
        }

        // Setup navigation listeners
        tabBookings.setOnClickListener(v -> navigateTo(AdminDashboardActivity.class));
        tabServices.setOnClickListener(v -> navigateTo(AdminServicesActivity.class));
        tabOffers.setOnClickListener(v -> navigateTo(AdminOffersActivity.class));
        tabGallery.setOnClickListener(v -> navigateTo(AdminGalleryActivity.class));
        
        tabProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));

        return view;
    }

    private void setActive(ImageView icon, TextView text) {
        icon.setColorFilter(COLOR_ACTIVE);
        text.setTextColor(COLOR_ACTIVE);
    }

    private void navigateTo(Class<?> destinationClass) {
        if (getActivity() != null && !getActivity().getClass().equals(destinationClass)) {
            Intent intent = new Intent(getActivity(), destinationClass);
            // Disable animation for instant tab switching feel
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); 
            startActivity(intent);
            getActivity().overridePendingTransition(0, 0);
            
            // Note: We don't generally finish() here so backstack works, 
            // but for top-level nav you might want to clear top.
        }
    }
}
