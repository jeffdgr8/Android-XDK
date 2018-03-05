package com.layer.xdk.ui.message.adapter2;


import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.Queryable;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.message.binder.BinderRegistry;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a {@link android.arch.paging.DataSource} to use with the paging library that loads
 * Messages from a {@link LayerClient}. This will convert them to {@link MessageModel} objects
 * before returning, thus doing the heavier computation of the conversion on the background thread.
 */
public class MessagesDataSource extends PositionalDataSource<MessageModel> {

    private final GroupingCalculator mGroupingCalculator;
    private final LayerClient mLayerClient;
    private final Conversation mConversation;
    private final LayerChangeEventListener.BackgroundThread.Weak listener;
    private final BinderRegistry mBinderRegistry;

    private int mMyNewestMessagePosition = Integer.MAX_VALUE;

    /**
     * Create a DataSource and registers a listener with the {@link LayerClient} to listen for
     * relevant change notifications to invalidate if necessary.
     *
     * @param layerClient client to use for the query
     * @param conversation conversation to fetch the messages for
     * @param binderRegistry registry that handles model creation
     * @param groupingCalculator calculator to use for message grouping
     */
    @SuppressWarnings("WeakerAccess")
    public MessagesDataSource(LayerClient layerClient, final Conversation conversation,
            BinderRegistry binderRegistry, GroupingCalculator groupingCalculator) {
        mLayerClient = layerClient;
        mConversation = conversation;
        mBinderRegistry = binderRegistry;
        mGroupingCalculator = groupingCalculator;

        listener = new LayerChangeEventListener.BackgroundThread.Weak() {
            @Override
            public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
                List<LayerChange> changes = layerChangeEvent.getChanges();
                boolean needsInvalidation = false;
                for (LayerChange change : changes) {
                    switch (change.getObjectType()) {
                        case MESSAGE:
                            Message message = (Message) change.getObject();
                            if (message.getConversation().equals(conversation)) {
                                needsInvalidation = true;
                            }
                            break;
                        case IDENTITY:
                            Identity changed = (Identity) change.getObject();
                            if (conversation.getParticipants().contains(changed)) {
                                needsInvalidation = true;
                            }
                            break;
                        case MESSAGE_PART:
                            MessagePart part = (MessagePart) change.getObject();
                            if (part.getMessage().getConversation().equals(conversation)) {
                                needsInvalidation = true;
                            }
                            break;
                    }

                    if (needsInvalidation) {
                        // Unregister this listener, invalidate the data source and return so no
                        // more changes are processed
                        mLayerClient.unregisterEventListener(listener);
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.d("Invalidating DataSource due to change");
                        }
                        invalidate();
                        return;
                    }
                }

            }
        };

        mLayerClient.registerEventListener(listener);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params, @NonNull LoadInitialCallback<MessageModel> callback) {
        int count = (int) computeCount();
        if (count == 0) {
            callback.onResult(Collections.<MessageModel>emptyList(), 0, 0);
        } else {
            int position = computeInitialLoadPosition(params, count);
            int size = computeInitialLoadSize(params, position, count);

            LoadRangeResults results = loadRangeInternal(position, size);
            if (results.mRealSize == size) {
                callback.onResult(convertMessagesToModels(results), position, count);
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
            @NonNull LoadRangeCallback<MessageModel> callback) {
        LoadRangeResults results = loadRangeInternal(params.startPosition, params.loadSize);
        callback.onResult(convertMessagesToModels(results));
    }

    private long computeCount() {
        Long count = mLayerClient.executeQueryForCount(Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, mConversation))
                .build());
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private LoadRangeResults loadRangeInternal(int position, int requestedLoadSize) {
        LoadRangeResults results = new LoadRangeResults();
        // Load an additional after for cluster calculation
        int loadSizeForGrouping = requestedLoadSize + 1;
        // Load an additional before for cluster calculation

        if (position != 0) {
            results.mExtraAtBeginning = true;
            position--;
            loadSizeForGrouping++;
        }
        List<? extends Queryable> messages = mLayerClient.executeQueryForObjects(Query.builder(
                Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO,
                        mConversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION,
                        SortDescriptor.Order.DESCENDING))
                .offset(position)
                .limit(loadSizeForGrouping) // Additional for clustering calculation
                .build());

        results.mMessages = (List<Message>) messages;
        results.mOffset = position;

        // Determine if there is an extra at the end or not
        int resultSize = messages.size();
        if (results.mExtraAtBeginning) {
            resultSize--;
        }
        if (resultSize == requestedLoadSize + 1) {
            resultSize--;
            results.mExtraAtEnd = true;
        }
        results.mRealSize = resultSize;

        return results;
    }

    @NonNull
    private List<MessageModel> convertMessagesToModels(LoadRangeResults loadResults) {
        List<MessageModel> models = new ArrayList<>();
        for (Message message : loadResults.mMessages) {
            MessageModel model = mBinderRegistry.getMessageModelManager().getNewModel(message);
            model.processPartsFromTreeRoot();
            models.add(model);
        }

        updateMyNewestMessagePosition(models, loadResults.mOffset);

        mGroupingCalculator.calculateGrouping(models);
        // Trim extras that were loaded for the grouping calc
        if (loadResults.mExtraAtBeginning) {
            models.remove(0);
        }
        if (loadResults.mExtraAtEnd) {
            models.remove(models.size() - 1);
        }

        return models;
    }

    private void updateMyNewestMessagePosition(List<MessageModel> models, int offset) {
        if (mMyNewestMessagePosition < offset) {
            // No use in calculating as we already have a newer message
            return;
        }
        for (int i = 0; i < models.size(); i++) {
            MessageModel model = models.get(i);
            if (model.isMessageFromMe() && (offset + i < mMyNewestMessagePosition)) {
                mMyNewestMessagePosition = offset + i;
                model.setMyNewestMessage(true);
                break;
            }
        }
    }

    private static class LoadRangeResults {
        List<Message> mMessages;
        int mRealSize;
        boolean mExtraAtBeginning;
        boolean mExtraAtEnd;
        int mOffset;
    }
}
