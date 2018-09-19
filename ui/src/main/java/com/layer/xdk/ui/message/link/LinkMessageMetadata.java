package com.layer.xdk.ui.message.link;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.MessageMetadata;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class LinkMessageMetadata extends MessageMetadata {

    @SerializedName("author")
    public String mAuthor;
    @SerializedName("description")
    public String mDescription;
    @SerializedName("title")
    public String mTitle;
    @SerializedName("image_url")
    public String mImageUrl;
    @SerializedName("url")
    public String mUrl;
}
