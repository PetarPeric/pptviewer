package com.petar.pptxplugins;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class PPTX {

    @SuppressLint("StaticFieldLeak")
    private static PPTX singleton;
    public static String msg;

    private Activity activity;

    public static void load(Activity activity, String path) {
        if (singleton == null) {
            singleton = new PPTX();
        }
        singleton.create(activity,path);
    }

    private void sendMessage(String msg){
        PPTX.msg = msg;
        Log.d("PPTX","Send msg: " + msg);
        // sendIntent is the object that will be broadcast outside our app
        Intent sendIntent = new Intent();

        // We add flags for example to work from background
//        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_FROM_BACKGROUND|Intent.FLAG_INCLUDE_STOPPED_PACKAGES  );

        // SetAction uses a string which is an important name as it identifies the sender of the itent and that we will give to the receiver to know what to listen.
        // By convention, it's suggested to use the current package name
        sendIntent.setAction("com.petar.pptxplugin.UnityToIntent");

        // Here we fill the Intent with our data, here just a string with an incremented number in it.
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        // And here it goes ! our message is send to any other app that want to listen to it.
        activity.sendBroadcast(sendIntent);
    }

    private void create(Activity activity, String path) {
        this.activity = activity;
        if (singleton != null) {
            singleton.sendMessage(path);
        }

    }

    public static void prepareNextSlide() {
        if (singleton != null) {
            singleton.sendMessage("NEXT");
        }
    }

    //Zahtev za pripremu predhodnog slajda
    public static void preparePreviousSlide() {
        if (singleton != null) {
            singleton.sendMessage("PREVIOUS");
        }
    }

    //Zahtev za putanju do slike slajda
    //Vraca "" prazan string ako nije spremna putanja
    public static void getPrepareSlide() {
        if (singleton != null) {
            singleton.sendMessage("GET_SLIDE");
        }

    }
}
