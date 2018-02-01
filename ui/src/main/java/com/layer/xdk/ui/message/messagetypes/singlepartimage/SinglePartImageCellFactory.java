package com.layer.xdk.ui.message.messagetypes.singlepartimage;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiMessageItemCellImageBinding;
import com.layer.xdk.ui.message.messagetypes.CellFactory;
import com.layer.xdk.ui.util.imagecache.ImageCacheWrapper;
import com.layer.xdk.ui.util.imagecache.ImageRequestParameters;
import com.layer.xdk.ui.util.imagepopup.ImagePopupActivity;

import java.util.Set;

/**
 * BasicImage handles non-ThreePartImage images.  It relies on the ThreePartImage RequestHandler and does not handle image rotation.
 */
public class SinglePartImageCellFactory extends
        CellFactory<SinglePartImageCellFactory.CellHolder, SinglePartImageCellFactory.PartId> implements View.OnClickListener {
    private static final String IMAGE_CACHING_TAG = SinglePartImageCellFactory.class.getSimpleName();
    private static final int PLACEHOLDER = com.layer.xdk.ui.R.drawable.xdk_ui_message_item_cell_placeholder;
    private static final int CACHE_SIZE_BYTES = 256 * 1024;

    private final LayerClient mLayerClient;
    private final ImageCacheWrapper mImageCacheWrapper;

    public SinglePartImageCellFactory(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        super(CACHE_SIZE_BYTES);
        mLayerClient = layerClient;
        mImageCacheWrapper = imageCacheWrapper;
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {
        return new CellHolder(XdkUiMessageItemCellImageBinding.inflate(layoutInflater, cellView, true));
    }

    @Override
    public void bindCellHolder(final CellHolder cellHolder, PartId index, Message message, CellHolderSpecs specs) {
        cellHolder.mImageView.setTag(index);
        cellHolder.mImageView.setOnClickListener(this);
        cellHolder.mProgressBar.show();

        ImageCacheWrapper.Callback callback = new ImageCacheWrapper.Callback() {
            @Override
            public void onSuccess() {
                cellHolder.mProgressBar.hide();
            }

            @Override
            public void onFailure() {
                cellHolder.mProgressBar.hide();
            }
        };

        ImageRequestParameters imageRequestParameters = new ImageRequestParameters
                .Builder(index.mId)
                .placeHolder(PLACEHOLDER)
                .resize(specs.maxWidth, specs.maxHeight)
                .rotate(0)
                .tag(IMAGE_CACHING_TAG)
                .centerCrop(true)
                .onlyScaleDown(true)
                .defaultCircularTransform(true)
                .rotate(0)
                .callback(callback)
                .build();
        mImageCacheWrapper.loadImage(imageRequestParameters, cellHolder.mImageView);
    }

    @Override
    public void onClick(View v) {
        ImagePopupActivity.init(mLayerClient);
        Context context = v.getContext();
        if (context == null) return;
        Intent intent = new Intent(context, ImagePopupActivity.class);
        intent.putExtra("fullId", ((PartId) v.getTag()).mId);

        if (Build.VERSION.SDK_INT >= 21 && context instanceof Activity) {
            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context, v, "image").toBundle());
        } else {
            context.startActivity(intent);
        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mImageCacheWrapper.pauseTag(IMAGE_CACHING_TAG);
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
            case RecyclerView.SCROLL_STATE_SETTLING:
                mImageCacheWrapper.resumeTag(IMAGE_CACHING_TAG);
                break;
        }
    }

    @Override
    public PartId parseContent(LayerClient layerClient, Message message) {
        for (MessagePart part : message.getMessageParts()) {
            if (part.getMimeType().startsWith("image/")) return new PartId(part.getId());
        }
        return null;
    }

    @Override
    public boolean isType(Message message) {
        Set<MessagePart> parts = message.getMessageParts();
        return parts.size() == 1
                && parts.iterator().next().getMimeType().startsWith("image/");
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        if (isType(message)) {
            return context.getString(R.string.xdk_ui_message_preview_image);
        }
        else {
            throw new IllegalArgumentException("Message is not of the correct type - SinglePartImage");
        }
    }


    //==============================================================================================
    // Inner classes
    //==============================================================================================

    public static class CellHolder extends CellFactory.CellHolder {
        ImageView mImageView;
        ContentLoadingProgressBar mProgressBar;
        XdkUiMessageItemCellImageBinding mXdkUiMessageItemCellImageBinding;

        public CellHolder(XdkUiMessageItemCellImageBinding uiMessageItemCellImageBinding) {
            mXdkUiMessageItemCellImageBinding = uiMessageItemCellImageBinding;
            mImageView = uiMessageItemCellImageBinding.cellImage;
            mProgressBar = uiMessageItemCellImageBinding.cellProgress;
        }
    }

    public static class PartId implements CellFactory.ParsedContent {
        public final Uri mId;

        public PartId(Uri id) {
            mId = id;
        }

        @Override
        public int sizeOf() {
            return mId.toString().getBytes().length;
        }
    }
}
