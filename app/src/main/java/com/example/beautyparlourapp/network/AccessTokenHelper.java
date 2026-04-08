package com.example.beautyparlourapp.network;

import android.content.Context;
import com.example.beautyparlourapp.R;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.Arrays;

public class AccessTokenHelper {
    public static String getAccessToken(Context context) {
        try {
            // Read serviceAccountKey.json from res/raw/ folder
            InputStream stream = context.getResources().openRawResource(R.raw.serviceaccountkey);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}