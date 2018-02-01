package com.layer.xdk.ui.message;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.avatar.AvatarViewModelImpl;
import com.layer.xdk.ui.databinding.XdkUiMessageItemFooterBinding;
import com.layer.xdk.ui.identity.IdentityFormatterImpl;
import com.layer.xdk.ui.util.imagecache.ImageCacheWrapper;

import java.util.Set;

public class MessageItemFooterViewHolder extends MessageItemViewHolder<MessageItemLegacyViewModel, XdkUiMessageItemFooterBinding> {

    public MessageItemFooterViewHolder(ViewGroup parent, MessageItemLegacyViewModel messageItemViewModel,
                                       ImageCacheWrapper imageCacheWrapper) {
        super(parent, R.layout.xdk_ui_message_item_footer, messageItemViewModel);
        getBinding().avatar.init(new AvatarViewModelImpl(imageCacheWrapper), new IdentityFormatterImpl(parent.getContext()));
    }

    public void clear() {
        getBinding().root.removeAllViews();
    }

    public void bind(Set<Identity> users, View mFooterView, boolean shouldAvatarBeVisible) {
        getBinding().root.addView(mFooterView);
        getViewModel().setParticipants(users);
        getViewModel().setAvatarViewVisibilityType(shouldAvatarBeVisible);
        int numberOfUsers = users.size();

        if (numberOfUsers > 2) {
            getViewModel().setTypingIndicatorMessageVisibility(true);
            getViewModel().setMessageFooterAnimationVisibility(false);
            String firstUser = "", secondUser = "";
            int counter = 0;

            for (Identity user : users) {
                counter++;
                if (counter == 1) {
                    firstUser = user.getDisplayName();
                } else if (counter == 2) {
                    secondUser = user.getDisplayName();
                    break;
                }
            }
            Resources resources = mFooterView.getContext().getResources();
            int remainingUsers = numberOfUsers % 2;
            String typingIndicatorMessage = resources.getQuantityString(R.plurals.xdk_ui_typing_indicator_message,
                    remainingUsers, firstUser, secondUser, remainingUsers);
            getViewModel().setTypingIndicatorMessage(typingIndicatorMessage);
        } else {
            getViewModel().setTypingIndicatorMessageVisibility(false);
            getViewModel().setMessageFooterAnimationVisibility(true);
        }

        getViewModel().notifyChange();
        getBinding().setViewModel(getViewModel());
    }
}
