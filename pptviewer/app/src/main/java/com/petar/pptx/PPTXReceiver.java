package com.petar.pptx;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PPTXReceiver extends BroadcastReceiver {

    private static long time=0;
    // Triggered when an Intent is catched
    @Override
    public void onReceive(Context context, Intent intent) {
        // We get the data the Intent has
        if(System.currentTimeMillis()-time>1000) {
            String receiveMsg = intent.getStringExtra(Intent.EXTRA_TEXT);

            if (receiveMsg != null) {
                Log.d("PPTXReceiver", "Receive: " + receiveMsg);
                // We assigned it to our static variable
                if (receiveMsg.equals("NEXT")) {
                    pptxViewer2.prepareNextSlide();
                } else if (receiveMsg.equals("PREVIOUS")) {
                    pptxViewer2.preparePreviosSlide();
                } else if (receiveMsg.equals("GET_SLIDE")) {
                    pptxViewer2.getPrepareSlide();
                } else {
                    pptxViewer2.load(context, receiveMsg);
                }

            }
            time=System.currentTimeMillis();
        }
    }


}
