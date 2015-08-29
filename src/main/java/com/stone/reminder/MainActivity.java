package com.stone.reminder;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import java.util.Iterator;

public class MainActivity extends Activity {
    private MainPreferencFragment mPreferenceFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.i("jerry",
                "Country: " + getResources().getConfiguration().locale.getCountry() +
                " Language: " + getResources().getConfiguration().locale.getLanguage());

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        startService(new Intent(MainActivity.this, NotificationReceiver.class));

        mPreferenceFragment = new MainPreferencFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPreferenceFragment).commit();
    }
}
