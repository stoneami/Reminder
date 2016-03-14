package com.stone.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;

/**
 * Created by 80048914 on 2015/4/17.
 */
public class MainPreferencFragment extends PreferenceFragment  implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{
    public final static String TAG = "MainPreferencFragment";
    public static String KEY_ACTIVE_LISTENER;
    public static String KEY_FLOAT_VIEW;
    public static String KEY_SHOW_DEFAULT_FLOAT_VIEW;
    public static String KEY_AUTO_OPEN_MSG_ONLY_AT_HOME;
    public static String KEY_AUTO_OPEN_EVERYWHERE;
    public static String KEY_RECORD_ONGOING_MSG;
    public static String KEY_SMART_OPEN_APP;
    public static String KEY_DISPLAY_OFTEN_OPEN_ICON;

    private Preference mPreferenceActiveListener;
    private Preference mPreferenceFloatView;
    private Preference mPreferenceAlwaysShowFloatView;
    private Preference mPreferenceAutoOpenMsg;
    private Preference mPreferenceOpenEverywhere;
    private Preference mpPreferenceRecordOngoingMsg;
    private Preference mPreferenceSmartOpenApp;
    private Preference mPreferenceDisplayOftenOpenIcon;

    private void init(){
        KEY_ACTIVE_LISTENER = getString(R.string.key_active_listener);
        KEY_FLOAT_VIEW = getString(R.string.key_float_view);
        KEY_SHOW_DEFAULT_FLOAT_VIEW = getString(R.string.key_show_default_float_view);
        KEY_AUTO_OPEN_MSG_ONLY_AT_HOME = getString(R.string.key_auto_open_msg_at_home);
        KEY_AUTO_OPEN_EVERYWHERE = getString(R.string.key_open_everywhere);
        KEY_RECORD_ONGOING_MSG = getString(R.string.key_record_ongoing_msg);
        KEY_SMART_OPEN_APP = getString(R.string.key_smart_open_app);
        KEY_DISPLAY_OFTEN_OPEN_ICON = getString(R.string.key_display_often_open_icon);

        mPreferenceActiveListener = findPreference(KEY_ACTIVE_LISTENER);
        mPreferenceFloatView = findPreference(KEY_FLOAT_VIEW);
        mPreferenceAlwaysShowFloatView = findPreference(KEY_SHOW_DEFAULT_FLOAT_VIEW);
        mPreferenceAutoOpenMsg = findPreference(KEY_AUTO_OPEN_MSG_ONLY_AT_HOME);
        mPreferenceOpenEverywhere = findPreference(KEY_AUTO_OPEN_EVERYWHERE);
        mpPreferenceRecordOngoingMsg = findPreference(KEY_RECORD_ONGOING_MSG);
        mPreferenceSmartOpenApp = findPreference(KEY_SMART_OPEN_APP);
        mPreferenceDisplayOftenOpenIcon = findPreference(KEY_DISPLAY_OFTEN_OPEN_ICON);

        mPreferenceActiveListener.setOnPreferenceClickListener(this);
        mPreferenceFloatView.setOnPreferenceClickListener(this);
        mPreferenceAlwaysShowFloatView.setOnPreferenceClickListener(this);
        mPreferenceAutoOpenMsg.setOnPreferenceClickListener(this);
        mPreferenceOpenEverywhere.setOnPreferenceClickListener(this);
        mpPreferenceRecordOngoingMsg.setOnPreferenceClickListener(this);
        mPreferenceSmartOpenApp.setOnPreferenceClickListener(this);
        mPreferenceDisplayOftenOpenIcon.setOnPreferenceClickListener(this);

        mPreferenceActiveListener.setOnPreferenceChangeListener(this);
        mPreferenceFloatView.setOnPreferenceChangeListener(this);
        mPreferenceAlwaysShowFloatView.setOnPreferenceChangeListener(this);
        mPreferenceAutoOpenMsg.setOnPreferenceChangeListener(this);
        mPreferenceOpenEverywhere.setOnPreferenceChangeListener(this);
        mpPreferenceRecordOngoingMsg.setOnPreferenceChangeListener(this);
        mPreferenceSmartOpenApp.setOnPreferenceChangeListener(this);
        mPreferenceDisplayOftenOpenIcon.setOnPreferenceChangeListener(this);
        //remove this preference temporarily
        ((PreferenceScreen)findPreference("root_screen")).removePreference(mPreferenceAutoOpenMsg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.main);

        init();
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
        Log.i(TAG,"onPreferenceChange() -> " + preference.getKey());

        if(KEY_FLOAT_VIEW.equals(preference.getKey())){
            Intent intent = new Intent(getActivity(), FloatViewManager.class);
            Log.i(TAG,"KEY_FLOAT_VIEW -> " + (Boolean)newValue);
            if((Boolean)newValue){
                mPreferenceAlwaysShowFloatView.setEnabled(true);
                getActivity().startService(intent);
                getActivity().sendBroadcast(new Intent(NotificationListener.MSG_LOAD_NOTIFICATIONS));
            }else {
                mPreferenceAlwaysShowFloatView.setEnabled(false);
                getActivity().stopService(intent);
            }
        }else if(KEY_SHOW_DEFAULT_FLOAT_VIEW.equals(preference.getKey())){
            Log.i(TAG,"KEY_SHOW_DEFAULT_FLOAT_VIEW -> " + (Boolean)newValue);
            Intent intent = new Intent(NotificationListener.MSG_SHOW_DEFAULT_FLOAT_VIEW);
            getActivity().sendBroadcast(intent);
        }else if(KEY_RECORD_ONGOING_MSG.equals(preference.getKey())){
            Intent intent = new Intent(NotificationListener.MSG_RELOAD_NOTIFICATIONS);
            getActivity().sendBroadcast(intent);
        }else if(KEY_DISPLAY_OFTEN_OPEN_ICON.equals(preference.getKey())){
            Intent intent = new Intent(NotificationListener.MSG_DISPLAY_OFTEN_OPEN_ICON);
            getActivity().sendBroadcast(intent);
        }else if(KEY_SMART_OPEN_APP.equals(preference.getKey())){
            if((Boolean)newValue){
                //mPreferenceDisplayOftenOpenIcon.setEnabled(true);
            }else {
                //mPreferenceDisplayOftenOpenIcon.setEnabled(false);
            }
        }

        return true;
    }
}
