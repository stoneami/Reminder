package com.stone.reminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
    private MainPreferencFragment mPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        startService(new Intent(MainActivity.this, FloatViewManager.class));

        mPreferenceFragment = new MainPreferencFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPreferenceFragment).commit();

        //Intent killIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        //startActivity(killIntent);
    }
}
