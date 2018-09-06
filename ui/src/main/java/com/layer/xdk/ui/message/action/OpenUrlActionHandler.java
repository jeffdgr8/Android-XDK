package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.message.image.ImageMessageMetadata;
import com.layer.xdk.ui.message.image.ImageMessageModel;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.popup.ImagePopupActivity;
import com.layer.xdk.ui.message.model.MessageModel;

public class OpenUrlActionHandler extends ActionHandler {

    private static final String KEY_URL = "url";

    private static final String KEY_MIME_TYPE = "mime-type";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_ORIENTATION = "orientation";

    private Intent mBrowserIntent;

    public OpenUrlActionHandler(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        super(layerClient, "open-url");
        mBrowserIntent = new Intent(Intent.ACTION_VIEW);
        ImagePopupActivity.init(layerClient, imageCacheWrapper);
    }

    @Override
    public void performAction(@NonNull Context context, @NonNull MessageModel model) {
        JsonObject data = model.getActionData();

        String url = getUrl(model, data);

        if (data.has(KEY_MIME_TYPE) || model instanceof ImageMessageModel) {
            openPopupImage(context, url, model, data);
        } else {
            openUrl(context, url);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected String getUrl(@NonNull MessageModel model, JsonObject actionData) {
        if (actionData.has(KEY_URL)) {
            return actionData.get(KEY_URL).getAsString();
        } else if (model instanceof Actionable) {
            return ((Actionable) model).getUrl();
        }

        return null;
    }

    private void openUrl(final Context context, String url) {
        mBrowserIntent.setData(Uri.parse(url));
        if (mBrowserIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mBrowserIntent);
        } else {
            notifyUnresolvedIntent(context, mBrowserIntent);
        }
    }

    private void openPopupImage(Context context, String url, MessageModel model, JsonObject data) {
        Intent intent = new Intent(context, ImagePopupActivity.class);
        ImagePopupActivity.Parameters parameters = new ImagePopupActivity.Parameters();
        parameters.source(url);

        int width = 0;
        int height = 0;
        if (data.has(KEY_WIDTH)) {
            width = data.get(KEY_WIDTH).getAsInt();
        }
        if (data.has(KEY_HEIGHT)) {
            height = data.get(KEY_HEIGHT).getAsInt();
        }

        if ((width == 0 || height == 0) && model instanceof ImageMessageModel) {
            Pair<Integer, Integer> fallback = ((ImageMessageModel) model).getDimensionsForAction();
            if (width == 0 && fallback.first != null) {
                width = fallback.first;
            }
            if (height == 0 && fallback.second != null) {
                height = fallback.second;
            }
        }
        parameters.dimensions(height, width);

        if (data.has(KEY_ORIENTATION)) {
            parameters.orientation(data.get(KEY_ORIENTATION).getAsInt());
        } else if (model instanceof ImageMessageModel) {
            ImageMessageMetadata metadata = ((ImageMessageModel) model).getMetadata();
            if (metadata != null) {
                parameters.orientation(metadata.mOrientation);
            }
        }

        intent.putExtra(ImagePopupActivity.EXTRA_PARAMS, parameters);
        context.startActivity(intent);
    }

    /**
     * Interface a model should implement if it supports the open URL action.
     */
    public interface Actionable {
        String getUrl();
    }
}
