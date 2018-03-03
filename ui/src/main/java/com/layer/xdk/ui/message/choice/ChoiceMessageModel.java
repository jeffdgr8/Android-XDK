package com.layer.xdk.ui.message.choice;

import android.content.Context;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.response.ChoiceResponseModel;
import com.layer.xdk.ui.message.response.ResponseSummary;
import com.layer.xdk.ui.repository.MessageSenderRepository;
import com.layer.xdk.ui.util.json.AndroidFieldNamingStrategy;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChoiceMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.choice+json";

    private ChoiceMessageMetadata mMetadata;
    private ResponseSummary mResponseSummary;
    private Set<String> mSelectedChoices;
    private Gson mGson;

    public ChoiceMessageModel(Context context, LayerClient layerClient, Message message) {
        super(context, layerClient, message);
        mGson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();
        mSelectedChoices = new HashSet<>();
    }

    public Class<ChoiceMessageView> getRendererType() {
        return ChoiceMessageView.class;
    }

    @Override
    public int getViewLayoutId() {
        return 0;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_titled_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        mMetadata = mGson.fromJson(reader, ChoiceMessageMetadata.class);
        processSelections();
        notifyPropertyChanged(BR.choiceMessageMetadata);
    }

    @Override
    protected void processResponseSummaryPart(@NonNull MessagePart responseSummaryPart) {
        JsonReader reader = new JsonReader(new InputStreamReader(responseSummaryPart.getDataStream()));
        mResponseSummary = mGson.fromJson(reader, ResponseSummary.class);
        processSelections();
        notifyPropertyChanged(BR.selectedChoices);
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        String title = getAppContext().getString(R.string.xdk_ui_choice_message_model_default_title);
        if (mMetadata != null && mMetadata.getTitle() != null) {
            title = mMetadata.getTitle();
        }

        return title;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        return null;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.transparent;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (getHasContent() && mMetadata.getChoices().size() > 0) {
            return getTitle();
        }
        return null;
    }

    @Nullable
    @Bindable
    public ChoiceMessageMetadata getChoiceMessageMetadata() {
        return mMetadata;
    }

    @Bindable
    public Set<String> getSelectedChoices() {
        return new HashSet<>(mSelectedChoices);
    }

    @Bindable
    public boolean getIsEnabledForMe() {
        if (getLayerClient().getAuthenticatedUser() == null || mMetadata == null)
            return false;
        String myUserID = getLayerClient().getAuthenticatedUser().getId().toString();
        if (mMetadata.getEnabledFor() != null) {
            return mMetadata.getEnabledFor().contains(myUserID);
        }

        return true;
    }

    private void processSelections() {
        mSelectedChoices.clear();
        if (mResponseSummary != null && mResponseSummary.hasData()) {
            for (Map.Entry<String, JsonObject> participantResponses : mResponseSummary.getParticipantData().entrySet()) {
                if (participantResponses.getValue().has(mMetadata.getResponseName())) {
                    String[] ids = participantResponses.getValue().get(mMetadata.getResponseName()).getAsString().split(",");
                    mSelectedChoices.addAll(Arrays.asList(ids));
                    // If nothing is selected then we can get an empty string. Remove this
                    mSelectedChoices.remove("");
                }
            }
        } else if (mMetadata.getPreselectedChoice() != null) {
            mSelectedChoices.add(mMetadata.getPreselectedChoice());
        }
    }

    void sendResponse(@NonNull ChoiceMetadata choice, boolean selected, @NonNull Set<String> selectedChoices) {
        String userName = getIdentityFormatter().getDisplayName(getLayerClient().getAuthenticatedUser());
        String statusText;
        if (TextUtils.isEmpty(mMetadata.getName())) {
            statusText = getAppContext().getString(
                    selected ? R.string.xdk_ui_response_message_status_text_selected
                            : R.string.xdk_ui_response_message_status_text_deselected,
                    userName,
                    choice.getText());
        } else {
            statusText = getAppContext().getString(
                    selected ? R.string.xdk_ui_response_message_status_text_with_name_selected
                            : R.string.xdk_ui_response_message_status_text_with_name_deselected,
                    userName,
                    choice.getText(),
                    mMetadata.getName());
        }
        UUID rootPartId = UUID.fromString(getMessagePart().getId().getLastPathSegment());

        ChoiceResponseModel choiceResponseModel = new ChoiceResponseModel(getMessage().getId(),
                rootPartId, statusText);
        choiceResponseModel.addChoices(mMetadata.getResponseName(), selectedChoices);

        MessageSenderRepository messageSenderRepository = getMessageSenderRepository();
        messageSenderRepository.sendChoiceResponse(getMessage().getConversation(), choiceResponseModel);
    }

}
