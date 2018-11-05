package com.layer.xdk.ui.composebar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.layer.xdk.test.common.stub.LayerClientStub;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.sender.AttachmentSender;
import com.layer.xdk.ui.testactivity.ComposeBarTestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ComposeBarTest {

    private ComposeBar mComposeBar;

    @Rule
    public ActivityTestRule<ComposeBarTestActivity> mActivityTestRule = new ActivityTestRule<>(
            ComposeBarTestActivity.class);

    @Before
    public void setUp() {
        mComposeBar = mActivityTestRule.getActivity().getComposeBar();
    }

    @Test
    public void testAttachmentMenuClosedAfterSelection() {
        final StubAttachmentSender sender = new StubAttachmentSender(mActivityTestRule.getActivity());
        mActivityTestRule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mComposeBar.addAttachmentSendersToDefaultAttachmentButton(sender);
            }
        });

        onView(withId(R.id.xdk_ui_compose_bar_button_left_4)).perform(click());
        onView(withId(R.id.xdk_ui_attachment_menu)).check(matches(isDisplayed()));

        onView(withText(sender.getTitle())).perform(click());

        onView(withId(R.id.xdk_ui_attachment_menu)).check(doesNotExist());
    }

    private static class StubAttachmentSender extends AttachmentSender {

        public StubAttachmentSender(Context context) {
            super(context, new LayerClientStub(), "Stub sender", null);
        }

        @Override
        public boolean requestSend() {
            return true;
        }
    }
}
