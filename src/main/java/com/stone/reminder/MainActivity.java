package com.stone.reminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends Activity {
    private CheckBox mFloatViewCheckBox;

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();

            Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);

            /*if (R.id.bt_start == id){
                MainActivity.this.startService(intent);
            }else if (R.id.bt_stop == id){
                MainActivity.this.stopService(intent);
            }else */if(R.id.bt_listener == id){
                startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        setContentView(R.layout.activity_main);

        mFloatViewCheckBox = (CheckBox)findViewById(R.id.cb_floatview);
        mFloatViewCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(MainActivity.this, NotificationReceiver.class);
                if(isChecked){
                    MainActivity.this.startService(intent);
                }else{
                    MainActivity.this.stopService(intent);
                }
            }
        });

//        findViewById(R.id.bt_start).setOnClickListener(mListener);
//        findViewById(R.id.bt_stop).setOnClickListener(mListener);
        findViewById(R.id.bt_listener).setOnClickListener(mListener);

        startService(new Intent(MainActivity.this, NotificationReceiver.class));
    }
}
