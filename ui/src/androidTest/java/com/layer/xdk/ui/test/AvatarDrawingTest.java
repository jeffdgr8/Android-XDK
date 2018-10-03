package com.layer.xdk.ui.test;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.layer.xdk.ui.testactivity.AvatarDrawingTestActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class AvatarDrawingTest {

    @Rule
    public ActivityTestRule<AvatarDrawingTestActivity> mActivityRule =
            new ActivityTestRule<>(AvatarDrawingTestActivity.class);

    @Test
    public void testDrawing() throws InterruptedException {
        // Sleep enough so a screenshot can be captured or it can be viewed on the recorded video
        Thread.sleep(2000);
    }
}
