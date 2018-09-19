package com.layer.xdk.ui.message.model;

import static com.google.common.truth.Truth.assertThat;

import static junit.framework.Assert.fail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.test.mock.MockContext;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.test.common.stub.ConversationStub;
import com.layer.xdk.test.common.stub.IdentityStub;
import com.layer.xdk.test.common.stub.LayerClientStub;
import com.layer.xdk.test.common.stub.MessagePartStub;
import com.layer.xdk.test.common.stub.MessageStub;
import com.layer.xdk.ui.message.MessageMetadata;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;

@RunWith(RobolectricTestRunner.class)
public class MessageModelTest {
    private static LayerClientStub sLayerClientStub;
    private static IdentityStub sAlice;
    private static IdentityStub sBob;
    private static Context sMockContext;

    @BeforeClass
    public static void setUpGlobal() {
        sAlice = new IdentityStub();
        sBob = new IdentityStub();
        sLayerClientStub = new LayerClientStub();
        sLayerClientStub.mAuthenticatedUser = sAlice;
        sMockContext = new MockContext();
    }

    @Test
    public void testParseLegacyDefaultException() {
        MessageStub messageStub = createSimpleMessage();
        MessagePartStub messagePartStub = new MessagePartStub();
        messagePartStub.mMimeType = "text/plain";

        messageStub.mMessageParts.add(messagePartStub);
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        try {
            model.processPartsFromTreeRoot();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Ignored
        }
    }

    @Test
    public void testMimeType_simple() {
        MessageStub messageStub = createSimpleMessage();
        MessagePartStub messagePartStub = new MessagePartStub();
        messagePartStub.mMimeType = "application/vnd.layer.text+json; role=root";

        messageStub.mMessageParts.add(messagePartStub);
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.processPartsFromTreeRoot();

        assertThat(model.getMimeTypeTree()).isEqualTo("application/vnd.layer.text+json[]");
    }

    @Test
    public void testMimeType_legacy() {
        MessageStub messageStub = createSimpleMessage();
        MessagePartStub messagePartStub = new MessagePartStub();
        messagePartStub.mMimeType = "text/plain";

        messageStub.mMessageParts.add(messagePartStub);
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub) {
            @Override
            protected void processLegacyParts() {

            }
        };
        model.processPartsFromTreeRoot();

        assertThat(model.getMimeTypeTree()).isEqualTo("text/plain[]");
    }

    @Test
    public void testHasMetadata_noMetadata() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        assertThat(model.getHasMetadata()).isFalse();
    }

    @Test
    public void testHasMetadata_withTitle() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.mTitle = "Title";
        assertThat(model.getHasMetadata()).isTrue();
    }

    @Test
    public void testHasMetadata_withFooter() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.mFooter = "Footer";
        assertThat(model.getHasMetadata()).isTrue();
    }

    @Test
    public void testHasMetadata_withDescription() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.mDescription = "Description";
        assertThat(model.getHasMetadata()).isTrue();
    }

    @Test
    public void testNoAction() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        assertThat(model.getActionEvent()).isNull();
        assertThat(model.getActionData().size()).isEqualTo(0);
    }

    @Test
    public void testActionOverride() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        Action action = new Action();
        action.setEvent("event");
        action.setData(new JsonObject());
        model.setAction(action);
        assertThat(model.getActionEvent()).isSameAs(action.getEvent());
        assertThat(model.getActionData()).isSameAs(action.getData());
    }

    @Test
    public void testActionMetadata() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setEvent("event");
        metadata.mAction.setData(new JsonObject());
        metadata.mAction.getData().addProperty("override", "metadata");
        model.setMetadata(metadata);
        assertThat(model.getActionEvent()).isSameAs(metadata.mAction.getEvent());
        assertThat(model.getActionData()).isSameAs(metadata.mAction.getData());
    }

    @Test
    public void testActionMetadataNoData() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setEvent("event");
        model.setMetadata(metadata);
        assertThat(model.getActionEvent()).isSameAs(metadata.mAction.getEvent());
        assertThat(model.getActionData()).isEqualTo(new JsonObject());
    }

    @Test
    public void testActionOverrideWithMetadata() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);

        Action action = new Action();
        action.setEvent("override event");
        action.setData(new JsonObject());
        model.setAction(action);

        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setEvent("metadata event");
        metadata.mAction.setData(new JsonObject());
        model.setMetadata(metadata);

        assertThat(model.getActionEvent()).isSameAs(action.getEvent());
        assertThat(model.getActionData()).isEqualTo(new JsonObject());
    }

    @Test
    public void testActionDataOverridesMetadataActionData() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);

        Action action = new Action();
        action.setData(new JsonObject());
        action.getData().addProperty("override", "action");
        model.setAction(action);

        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setData(new JsonObject());
        metadata.mAction.getData().addProperty("override", "metadata");
        model.setMetadata(metadata);

        assertThat(model.getActionData()).isEqualTo(action.getData());
    }

    @Test
    public void testDefaultActionEvent() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.setDefaultActionEvent("default");
        assertThat(model.getActionEvent()).isEqualTo("default");
    }

    @Test
    public void testActionEventOverridesDefaultActionEvent() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.setDefaultActionEvent("default");
        Action action = new Action();
        action.setEvent("action override");
        model.setAction(action);
        assertThat(model.getActionEvent()).isEqualTo("action override");
    }

    @Test
    public void testMetadataActionEventOverridesDefaultActionEvent() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.setDefaultActionEvent("default");

        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setEvent("metadata override");
        model.setMetadata(metadata);

        assertThat(model.getActionEvent()).isEqualTo("metadata override");
    }

    @Test
    public void testActionEventWithOnlyMetadataActionData() {
        MessageStub messageStub = createSimpleMessage();
        TestMessageModel model = new TestMessageModel(sMockContext, sLayerClientStub, messageStub);
        model.setDefaultActionEvent("default");

        Action action = new Action();
        action.setEvent("action event");
        model.setAction(action);

        MessageMetadata metadata = new MessageMetadata();
        metadata.mAction = new Action();
        metadata.mAction.setData(new JsonObject());
        metadata.mAction.getData().addProperty("override", "metadata");
        model.setMetadata(metadata);

        assertThat(model.getActionEvent()).isEqualTo(action.getEvent());
        assertThat(model.getActionData()).isEqualTo(metadata.mAction.getData());
    }

    @NonNull
    private MessageStub createSimpleMessage() {
        ConversationStub conversationStub = new ConversationStub();
        conversationStub.mParticipants = new HashSet<>(2);
        conversationStub.mParticipants.add(sAlice);
        conversationStub.mParticipants.add(sBob);
        MessageStub messageStub = new MessageStub();
        messageStub.mConversation = conversationStub;
        return messageStub;
    }


    private static class TestMessageModel extends MessageModel {
        String mTitle;
        String mDescription;
        String mFooter;

        public TestMessageModel(@NonNull Context context,
                @NonNull LayerClient layerClient,
                @NonNull Message message) {
            super(context, layerClient, message);
        }

        @Override
        protected void parse(@NonNull MessagePart messagePart) {}

        @Override
        public int getViewLayoutId() {
            return 0;
        }

        @Override
        public int getContainerViewLayoutId() {
            return 0;
        }

        @Override
        protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
            return false;
        }

        @Override
        public boolean getHasContent() {
            return false;
        }

        @Nullable
        @Override
        public String getPreviewText() {
            return null;
        }

        @Nullable
        @Override
        public String getTitle() {
            return mTitle;
        }

        @Nullable
        @Override
        public String getDescription() {
            return mDescription;
        }

        @Nullable
        @Override
        public String getFooter() {
            return mFooter;
        }
    }

}