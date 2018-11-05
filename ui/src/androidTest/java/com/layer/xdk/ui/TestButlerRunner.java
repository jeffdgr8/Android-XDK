package com.layer.xdk.ui;

import android.os.Bundle;
import androidx.multidex.MultiDex;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnitRunner;

import com.linkedin.android.testbutler.TestButler;

public class TestButlerRunner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        MultiDex.install(getTargetContext());
        super.onCreate(arguments);
    }

    @Override
    public void onStart() {
        TestButler.setup(InstrumentationRegistry.getTargetContext());

        super.onStart();
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        TestButler.teardown(InstrumentationRegistry.getTargetContext());

        super.finish(resultCode, results);
    }
}
