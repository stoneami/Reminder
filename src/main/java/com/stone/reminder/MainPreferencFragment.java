package com.stone.reminder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

/**
 * Created by 80048914 on 2015/4/17.
 */
public class MainPreferencFragment extends PreferenceFragment  implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{
    public static String KEY_ACTIVE_LISTENER;
    public static String KEY_FLOAT_VIEW;
    public static String KEY_AUTO_OPEN_MSG;

    private Preference mPreferenceActiveListener;
    private Preference mPreferenceFloatView;
    private Preference mPreferenceAutoOpenMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main);

        KEY_ACTIVE_LISTENER = getString(R.string.key_active_listener);
        KEY_FLOAT_VIEW = getString(R.string.key_float_view);
        KEY_AUTO_OPEN_MSG = getString(R.string.key_auto_open_msg);

        mPreferenceActiveListener = findPreference(KEY_ACTIVE_LISTENER);
        mPreferenceFloatView = findPreference(KEY_FLOAT_VIEW);
        mPreferenceAutoOpenMsg = findPreference(KEY_AUTO_OPEN_MSG);

        mPreferenceActiveListener.setOnPreferenceClickListener(this);
        mPreferenceFloatView.setOnPreferenceClickListener(this);
        mPreferenceAutoOpenMsg.setOnPreferenceClickListener(this);

        mPreferenceActiveListener.setOnPreferenceChangeListener(this);
        mPreferenceFloatView.setOnPreferenceChangeListener(this);
        mPreferenceAutoOpenMsg.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(KEY_ACTIVE_LISTENER.equals(preference.getKey())){
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.i("jerry","onPreferenceChange()");
        if(KEY_FLOAT_VIEW.equals(preference.getKey())){
            Intent intent = new Intent(getActivity(), NotificationReceiver.class);
            if((Boolean)newValue){
                Log.i("jerry","onPreferenceChange():true");
                getActivity().startService(intent);
            }else {
                Log.i("jerry","onPreferenceChange():false");
                getActivity().stopService(intent);
            }
        }

        return true;
    }
}
