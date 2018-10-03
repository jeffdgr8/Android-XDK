package com.layer.xdk.ui.testactivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.test.common.stub.IdentityStub;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.avatar.AvatarView;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.cache.PicassoImageCacheWrapper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws several avatars with varying participant numbers and data so drawing can be visually
 * verified.
 */
public class AvatarDrawingTestActivity extends Activity {

    ImageCacheWrapper mImageCacheWrapper;
    DefaultIdentityFormatter mDefaultIdentityFormatter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageCacheWrapper = new PicassoImageCacheWrapper(Picasso.get());
        mDefaultIdentityFormatter = new DefaultIdentityFormatter(getApplicationContext());

        LinearLayout root = new LinearLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        root.addView(addAvatarGroup("Single", createSingleAvatars()));
        root.addView(addAvatarGroup("Two", createTwoAvatars()));
        root.addView(addAvatarGroup("Group", createGroupAvatars()));
    }

    private List<Pair<String, AvatarView>> createSingleAvatars() {
        List<Pair<String, AvatarView>> avatars = new ArrayList<>();

        avatars.add(new Pair<>("Placeholder", createAvatarWithParticipants(
                createIdentityWithNoData()
        )));
        avatars.add(new Pair<>("Image", createAvatarWithParticipants(
                createIdentityWithImage("Alice")
        )));
        avatars.add(new Pair<>("Display name 1", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice")
        )));
        avatars.add(new Pair<>("Display name 2", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice Smith")
        )));
        avatars.add(new Pair<>("First name only", createAvatarWithParticipants(
                createIdentityWithFirstAndLastName("Alice", null)
        )));
        avatars.add(new Pair<>("Last name only", createAvatarWithParticipants(
                createIdentityWithFirstAndLastName(null, "Smith")
        )));
        avatars.add(new Pair<>("First/last name", createAvatarWithParticipants(
                createIdentityWithFirstAndLastName("Alice", "Smith")
        )));
        return avatars;
    }

    private List<Pair<String, AvatarView>> createTwoAvatars() {
        List<Pair<String, AvatarView>> avatars = new ArrayList<>();

        avatars.add(new Pair<>("Placeholders", createAvatarWithParticipants(
                createIdentityWithNoData(), createIdentityWithNoData()
        )));
        avatars.add(new Pair<>("Images", createAvatarWithParticipants(
                createIdentityWithImage("Alice"), createIdentityWithImage("Bob")
        )));
        avatars.add(new Pair<>("Placeholder & image", createAvatarWithParticipants(
                createIdentityWithNoData(), createIdentityWithImage("Alice")
        )));
        avatars.add(new Pair<>("Image & placeholder", createAvatarWithParticipants(
                createIdentityWithImage("Alice"), createIdentityWithNoData()
        )));

        avatars.add(new Pair<>("Placeholder & display name", createAvatarWithParticipants(
                createIdentityWithNoData(), createIdentityWithDisplayName("Alice S")
        )));
        avatars.add(new Pair<>("Display name & placeholder", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice S"), createIdentityWithNoData()
        )));
        avatars.add(new Pair<>("Display name & display name", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice S"), createIdentityWithDisplayName("Bob J")
        )));

        return avatars;
    }

    private List<Pair<String, AvatarView>> createGroupAvatars() {
        List<Pair<String, AvatarView>> avatars = new ArrayList<>();

        avatars.add(new Pair<>("Three placeholders", createAvatarWithParticipants(
                createIdentityWithNoData(),
                createIdentityWithNoData(),
                createIdentityWithNoData()
        )));
        avatars.add(new Pair<>("Three display names", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice S"),
                createIdentityWithDisplayName("Bob W"),
                createIdentityWithDisplayName("Carl A")
        )));
        avatars.add(new Pair<>("Four placeholders", createAvatarWithParticipants(
                createIdentityWithNoData(),
                createIdentityWithNoData(),
                createIdentityWithNoData()
        )));
        avatars.add(new Pair<>("Four display names", createAvatarWithParticipants(
                createIdentityWithDisplayName("Alice S"),
                createIdentityWithDisplayName("Bob W"),
                createIdentityWithDisplayName("Carl A"),
                createIdentityWithDisplayName("Diane C")
        )));

        return avatars;
    }

    private View addAvatarGroup(String labelText, List<Pair<String, AvatarView>> avatars) {
        TextView label = new TextView(this);
        label.setText(labelText);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(label);
        for (Pair<String, AvatarView> avatar : avatars) {
            TextView textView = new TextView(this);
            textView.setText(avatar.first);
            layout.addView(textView);
            layout.addView(avatar.second);
        }

        return layout;
    }

    private AvatarView createAvatarWithParticipants(Identity... identities) {
        AvatarView avatarView = new AvatarView(this);
        avatarView.setLayoutParams(new FrameLayout.LayoutParams(
                getResources().getDimensionPixelSize(R.dimen.xdk_ui_avatar_width_large),
                getResources().getDimensionPixelSize(R.dimen.xdk_ui_avatar_height_large))
        );
        avatarView.setImageCacheWrapper(mImageCacheWrapper);
        avatarView.setIdentityFormatter(mDefaultIdentityFormatter);

        avatarView.setParticipants(identities);
        return avatarView;
    }

    private Identity createIdentityWithNoData() {
        IdentityStub identity = new IdentityStub();
        identity.mAvatarImageUrl = null;
        identity.mDisplayName = null;
        identity.mFirstName = null;
        identity.mLastName = null;
        return identity;
    }

    private Identity createIdentityWithImage(String text) {
        IdentityStub identity = new IdentityStub();
        identity.mAvatarImageUrl = "https://via.placeholder.com/400x400?text=" + text;
        return identity;
    }

    private Identity createIdentityWithDisplayName(String displayName) {
        IdentityStub identity = new IdentityStub();
        identity.mDisplayName = displayName;
        identity.mFirstName = null;
        identity.mLastName = null;
        return identity;
    }

    private Identity createIdentityWithFirstAndLastName(String firstName, String lastName) {
        IdentityStub identity = new IdentityStub();
        identity.mDisplayName = null;
        identity.mFirstName = firstName;
        identity.mLastName = lastName;
        return identity;
    }
}
