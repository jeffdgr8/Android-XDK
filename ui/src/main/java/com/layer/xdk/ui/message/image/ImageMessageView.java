package com.layer.xdk.ui.message.image;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.layer.xdk.ui.message.view.MessageViewHelper;

public class ImageMessageView extends AppCompatImageView {

    private MessageViewHelper mMessageViewHelper;

    public ImageMessageView(Context context) {
        this(context, null, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMessageViewHelper = new MessageViewHelper(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageViewHelper.performAction();
            }
        });
    }

    public void setMessageModel(@Nullable ImageMessageModel model) {
        mMessageViewHelper.setMessageModel(model);
    }
}
