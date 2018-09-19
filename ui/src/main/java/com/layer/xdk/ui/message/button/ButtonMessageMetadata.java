package com.layer.xdk.ui.message.button;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.MessageMetadata;

import java.util.List;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ButtonMessageMetadata extends MessageMetadata {

    @SerializedName("buttons")
    public List<ButtonMetadata> mButtonMetadata;
}
