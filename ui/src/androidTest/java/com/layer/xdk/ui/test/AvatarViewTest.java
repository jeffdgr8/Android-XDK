package com.layer.xdk.ui.test;


import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.graphics.Color;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.filters.MediumTest;
import androidx.test.internal.util.Checks;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.WindowManager;

import com.layer.xdk.ui.presence.PresenceView;
import com.layer.xdk.ui.testactivity.AvatarActivityTestView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class AvatarViewTest {

    private int mAwayColor = Color.rgb(0xF7, 0xCA, 0x40);

    @Rule
    public ActivityTestRule<AvatarActivityTestView> mAvatarActivityTestRule =
            new ActivityTestRule<>(AvatarActivityTestView.class);

    @Before
    public void setUp() {
        final AvatarActivityTestView avatarActivityTestView = mAvatarActivityTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                avatarActivityTestView.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        avatarActivityTestView.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void testThatAvatarColorChangeWhenSpinnerIsChanged() {
        String selectionText = "AWAY";

        onView(withId(AvatarActivityTestView.VIEW_ID_SPINNER)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(selectionText))).perform(click());
        onView(withId(AvatarActivityTestView.VIEW_ID_SPINNER)).check(matches(withSpinnerText(containsString(selectionText))));
        onView(withText(selectionText)).perform(click());
    }


    public static Matcher<View> withBgColor(final int color) {
        Checks.checkNotNull(color);
        return new BoundedMatcher<View, PresenceView>(PresenceView.class) {
            @Override
            public boolean matchesSafely(PresenceView presenceView) {
                return color ==  presenceView.getSolidColor();
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("");
            }
        };
    }

}