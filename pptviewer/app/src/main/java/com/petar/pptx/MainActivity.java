package com.petar.pptx;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Bundle;

import java.io.File;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(new PPTXReceiver(),new IntentFilter("com.petar.pptxplugin.UnityToIntent"));

//        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.pptx";
//        pptxViewer.load(this, path);
    }
}
