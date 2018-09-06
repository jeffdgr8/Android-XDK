package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.file.FileMessageModel;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

public class OpenFileActionHandler extends ActionHandler {
    private static final String ACTION_EVENT_OPEN_FILE = "open-file";
    private static final String ACTION_DATA_URI = "uri";
    private static final String ACTION_DATA_FILE_MIME_TYPE = "file_mime_type";

    public OpenFileActionHandler(LayerClient layerClient) {
        super(layerClient, ACTION_EVENT_OPEN_FILE);
    }

    @Override
    public void performAction(@NonNull Context context, @NonNull MessageModel model) {
        Uri uri = getFileUri(model);
        if (uri == null) {
            if (Log.isLoggable(Log.INFO)) {
                Log.i("Ignoring action as no URI is available");
            }
            return;
        }

        String mimeType = getFileMimeType(model);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, context.getString(R.string.xdk_ui_open_file_action_handler_activity_picker_title));
        context.startActivity(chooser);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected Uri getFileUri(@NonNull MessageModel model) {
        JsonObject actionData = model.getActionData();
        Uri uri = null;
        if (actionData.has(ACTION_DATA_URI)) {
            uri = Uri.parse(actionData.get(ACTION_DATA_URI).getAsString());
        } else if (model instanceof FileMessageModel) {
            uri = ((FileMessageModel) model).getFileUri();
        }

        return uri;
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected String getFileMimeType(@NonNull MessageModel model) {
        JsonObject actionData = model.getActionData();
        String mimeType = null;
        if (actionData.has(ACTION_DATA_FILE_MIME_TYPE)) {
            mimeType = actionData.get(ACTION_DATA_FILE_MIME_TYPE).getAsString();
        } else if (model instanceof FileMessageModel) {
            mimeType = ((FileMessageModel) model).getFileMimeType();
        }
        return mimeType;
    }
}
