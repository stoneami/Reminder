package com.stone.reminder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.stone.utils.Util;

public class MainActivity extends Activity {
    private MainPreferencFragment mPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        startService(new Intent(MainActivity.this, FloatViewManager.class));

        mPreferenceFragment = new MainPreferencFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPreferenceFragment).commit();
    }
}
