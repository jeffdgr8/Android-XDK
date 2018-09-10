package com.layer.xdk.ui.message.feedback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

import com.layer.xdk.ui.R;

/**
 * Special edit text used for feedback comments in the large message view. This uses a null
 * background when disabled so it appears as a normal text view.
 *
 * @see LargeFeedbackMessageFragment
 */
public class CommentEditText extends TextInputEditText {

    private Drawable mBackground;

    public CommentEditText(Context context) {
        this(context, null);
    }

    public CommentEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public CommentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBackground = getBackground();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ViewCompat.setBackground(this, enabled ? mBackground : null);
    }
}
