package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.message.location.LocationMessageMetadata;
import com.layer.xdk.ui.message.location.LocationMessageModel;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.util.Locale;

public class GoogleMapsOpenMapActionHandler extends ActionHandler {

    private static final String ACTION_EVENT = "open-map";

    public GoogleMapsOpenMapActionHandler(LayerClient layerClient) {
        super(layerClient, ACTION_EVENT);
    }

    @Override
    public void performAction(@NonNull Context context, @NonNull MessageModel model) {
        JsonObject data = model.getActionData();

        Gson gson = new Gson();
        LocationMessageMetadata actionMetadata = gson.fromJson(data, LocationMessageMetadata.class);
        LocationMessageMetadata modelMetadata = null;
        if (model instanceof LocationMessageModel) {
            modelMetadata = ((LocationMessageModel) model).getMetadata();
        }

        String title = actionMetadata.mTitle;

        if (title == null && modelMetadata != null) {
            title = modelMetadata.mTitle;
        }

        Uri googleMapsUri = constructGoogleMapsUri(title, actionMetadata);
        if (googleMapsUri == null && modelMetadata != null) {
            // Try the model metadata
            googleMapsUri = constructGoogleMapsUri(title, modelMetadata);
        }
        if (googleMapsUri == null) {
            if (Log.isLoggable(Log.INFO)) {
                Log.i("No location data to form an intent. Model: " + model);
            }
            return;
        }

        Intent openMapsIntent = new Intent(Intent.ACTION_VIEW, googleMapsUri);

        if (openMapsIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(openMapsIntent);
        } else {
            notifyUnresolvedIntent(context, openMapsIntent);
        }
    }

    private Uri constructGoogleMapsUri(String title, @NonNull LocationMessageMetadata metadata) {
        if (metadata.mLatitude != null && metadata.mLongitude != null) {
            return constructGoogleMapsUri(metadata.mLatitude, metadata.mLongitude, title);
        }
        return constructGoogleMapsUri(metadata.getFormattedAddress());
    }

    /**
     * Constructs a URI for an address per the scheme
     * <a href="https://developer.android.com/guide/components/intents-common.html#Maps">here </a>.
     *
     * @param address the address to send for the maps intent
     * @return a Uri to use with the {@link Intent#ACTION_VIEW} intent, null if the address is empty
     */
    @Nullable
    private Uri constructGoogleMapsUri(@Nullable String address) {
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        String queryString = String.format(Locale.US, "geo:0,0?q=%s", Uri.encode(address));
        return Uri.parse(queryString);
    }

    /**
     * Constructs a URI for a lat/long with option label per the scheme
     * <a href="https://developer.android.com/guide/components/intents-common.html#Maps">here </a>.
     *
     * @param latitude latitude to use for the maps intent
     * @param longitude longitude to use for the maps intent
     * @param markerTitle optional label to set on the location
     * @return a Uri to use with the {@link Intent#ACTION_VIEW} intent
     */
    private Uri constructGoogleMapsUri(double latitude, double longitude,
            @Nullable String markerTitle) {
        String queryString;
        if (TextUtils.isEmpty(markerTitle)) {
            queryString = String.format(Locale.US, "geo:0,0?q=%f,%f", latitude,
                longitude);
        } else {
            queryString = String.format(Locale.US, "geo:0,0?q=%f,%f(%s)", latitude,
                    longitude, markerTitle);
        }
        return Uri.parse(queryString);
    }
}
