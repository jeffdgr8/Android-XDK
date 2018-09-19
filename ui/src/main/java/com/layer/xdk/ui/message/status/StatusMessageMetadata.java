package com.layer.xdk.ui.message.status;


import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.MessageMetadata;

/**
 * Metadata for a status message
 */
public class StatusMessageMetadata extends MessageMetadata {

    @SerializedName("text")
    public String mText;
}
