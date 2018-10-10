package com.petar.pptxplugins;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public class PPTXReceiver extends BroadcastReceiver {

    private static PPTXReceiver instance;

    // bytes that will be read by Unity
    public static byte[] bytes;

    // Triggered when an Intent is catched
    @Override
    public void onReceive(Context context, Intent intent) {
        // We get the data the Intent has
        //        if (sentIntent != null) {
            // We assigned it to our static variable
            bytes = intent.getByteArrayExtra(Intent.EXTRA_TEXT);
//        }
    }

    // static method to create our receiver object, it'll be Unity that will create ou receiver object (singleton)
    public static void createInstance(Activity activity)
    {
        if(instance ==  null)
        {
            instance = new PPTXReceiver();
            activity.registerReceiver(instance,new IntentFilter("com.petar.pptx.IntentToUnity"));
        }

    }
}
