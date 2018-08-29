package com.layer.xdk.ui.message.adapter;

import static com.google.common.truth.Truth.assertThat;

import static junit.framework.Assert.fail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Query;
import com.layer.xdk.test.common.stub.AnnouncementStub;
import com.layer.xdk.test.common.stub.ConversationStub;
import com.layer.xdk.test.common.stub.MessagePartStub;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.model.MessageModelManager;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MessageModelDataSourceTest {

    @Test
    public void testReceiveAnnouncementChangeNotInvalidated() {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);

        // Get the change listener
        ArgumentCaptor<LayerChangeEventListener> argument = ArgumentCaptor.forClass(LayerChangeEventListener.class);
        verify(mockLayerClient).registerEventListener(argument.capture());

        // Create a change for an announcement and pass it to the listener
        MessagePartStub messagePartStub = new MessagePartStub();
        AnnouncementStub announcementStub = new AnnouncementStub();
        messagePartStub.mMessage = announcementStub;
        announcementStub.mMessageParts.add(messagePartStub);

        LayerChange insertChange = new LayerChange(LayerChange.Type.INSERT, messagePartStub, null,
                null, null);
        argument.getValue().onChangeEvent(new LayerChangeEvent(mockLayerClient, Collections.singletonList(insertChange)));

        // Ensure it is not invalidated
        assertThat(messageModelDataSource.isInvalid()).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadInitialNoQueryResults() {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        when(mockMessageModelManager.getNewModel(any(Message.class))).thenReturn(mock(MessageModel.class));

        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);


        when(mockLayerClient.executeQueryForObjects(any(Query.class)))
                .thenReturn(Collections.emptyList());
        when(mockLayerClient.executeQueryForCount(any(Query.class))).thenReturn(2L);

        PositionalDataSource.LoadInitialParams initialParams = new PositionalDataSource
                .LoadInitialParams(20, 40, 30, false);

        messageModelDataSource.loadInitial(initialParams,
                new PositionalDataSource.LoadInitialCallback<MessageModel>() {
                    @Override
                    public void onResult(@NonNull List<MessageModel> data, int position,
                            int totalCount) {
                        fail("DataSource should be invalidated since count doesn't match returned size");
                    }

                    @Override
                    public void onResult(@NonNull List<MessageModel> data, int position) {
                        fail("DataSource should be invalidated since count doesn't match returned size");
                    }
                });
        assertThat(messageModelDataSource.isInvalid()).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadRangeNoQueryResults() throws InterruptedException {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        when(mockMessageModelManager.getNewModel(any(Message.class))).thenReturn(
                mock(MessageModel.class));

        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);


        when(mockLayerClient.executeQueryForObjects(any(Query.class)))
                .thenReturn(Collections.emptyList());
        when(mockLayerClient.executeQueryForCount(any(Query.class))).thenReturn(2L);

        final CountDownLatch latch = new CountDownLatch(1);
        messageModelDataSource.loadRange(new PositionalDataSource.LoadRangeParams(20, 40),
                new PositionalDataSource.LoadRangeCallback<MessageModel>() {
                    @Override
                    public void onResult(@NonNull List<MessageModel> data) {
                        assertThat(data).isEmpty();
                        latch.countDown();
                    }
                });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadRangeWithExtraAtBeginningAndEnd() throws InterruptedException {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);

        final List<MessageModel> mockModels = mockQueryResults(4, mockMessageModelManager, mockLayerClient);
        when(mockLayerClient.executeQueryForCount(any(Query.class))).thenReturn(15L);

        final CountDownLatch latch = new CountDownLatch(1);
        messageModelDataSource.loadRange(new PositionalDataSource.LoadRangeParams(12, 2),
                new PositionalDataSource.LoadRangeCallback<MessageModel>() {
                    @Override
                    public void onResult(@NonNull List<MessageModel> data) {
                        assertThat(data).containsExactly(mockModels.get(1), mockModels.get(2));
                        latch.countDown();
                    }
                });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadRangeWithExtraAtBeginning() throws InterruptedException {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);


        final List<MessageModel> mockModels = mockQueryResults(3, mockMessageModelManager, mockLayerClient);
        when(mockLayerClient.executeQueryForCount(any(Query.class))).thenReturn(15L);

        final CountDownLatch latch = new CountDownLatch(1);
        messageModelDataSource.loadRange(new PositionalDataSource.LoadRangeParams(13, 2),
                new PositionalDataSource.LoadRangeCallback<MessageModel>() {
                    @Override
                    public void onResult(@NonNull List<MessageModel> data) {
                        assertThat(data).containsExactly(mockModels.get(1), mockModels.get(2));
                        latch.countDown();
                    }
                });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadRangeWithExtraAtEnd() throws InterruptedException {
        LayerClient mockLayerClient = mock(LayerClient.class);
        ConversationStub conversationStub = new ConversationStub();
        MessageModelManager mockMessageModelManager = mock(MessageModelManager.class);
        GroupingCalculator mockGroupingCalculator = mock(GroupingCalculator.class);

        MessageModelDataSource messageModelDataSource = new MessageModelDataSource(mockLayerClient,
                conversationStub,
                null,
                mockMessageModelManager,
                mockGroupingCalculator);


        final List<MessageModel> mockModels = mockQueryResults(3, mockMessageModelManager, mockLayerClient);
        when(mockLayerClient.executeQueryForCount(any(Query.class))).thenReturn(15L);

        final CountDownLatch latch = new CountDownLatch(1);
        messageModelDataSource.loadRange(new PositionalDataSource.LoadRangeParams(0, 2),
                new PositionalDataSource.LoadRangeCallback<MessageModel>() {
                    @Override
                    public void onResult(@NonNull List<MessageModel> data) {
                        assertThat(data).containsExactly(mockModels.get(0), mockModels.get(1));
                        latch.countDown();
                    }
                });

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @SuppressWarnings("unchecked")
    private List<MessageModel> mockQueryResults(int count, MessageModelManager mockModelManager, LayerClient mockLayerClient) {
        List<MessageModel> mockModels = new ArrayList<>(count);
        List<Message> queryResults = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            mockModels.add(mock(MessageModel.class));
            queryResults.add(mock(Message.class));
        }

        List<MessageModel> nextReturnValues = new ArrayList<>(mockModels);

        MessageModel firstMockModel = null;
        if (!nextReturnValues.isEmpty()) {
            firstMockModel = nextReturnValues.remove(0);
        }

        MessageModel[] nextReturnModels = new MessageModel[nextReturnValues.size()];
        nextReturnValues.toArray(nextReturnModels);

        when(mockModelManager.getNewModel(any(Message.class))).thenReturn(firstMockModel, nextReturnModels);

        when(mockLayerClient.executeQueryForObjects(any(Query.class)))
                .thenReturn(queryResults);
        return mockModels;
    }
}
