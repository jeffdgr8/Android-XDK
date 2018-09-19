package com.layer.xdk.ui.message;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

/**
 * Basic metadata all messages can have. Gson is used to serialize/deserialize this class.
 */
@SuppressWarnings("unused")
public class MessageMetadata {

    @SerializedName("action")
    public Action mAction;

    @SerializedName("custom_data")
    public JsonObject mCustomData;
}
