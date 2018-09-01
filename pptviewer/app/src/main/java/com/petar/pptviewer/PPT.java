package com.petar.pptviewer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.olivephone.office.TempFileManager;
import com.olivephone.office.powerpoint.DocumentSession;
import com.olivephone.office.powerpoint.DocumentSessionBuilder;
import com.olivephone.office.powerpoint.DocumentSessionStatusListener;
import com.olivephone.office.powerpoint.IMessageProvider;
import com.olivephone.office.powerpoint.ISystemColorProvider;
import com.olivephone.office.powerpoint.android.AndroidMessageProvider;
import com.olivephone.office.powerpoint.android.AndroidSystemColorProvider;
import com.olivephone.office.powerpoint.android.AndroidTempFileStorageProvider;
import com.olivephone.office.powerpoint.view.PersentationView;
import com.olivephone.office.powerpoint.view.SlideShowNavigator;
import com.olivephone.office.powerpoint.view.SlideView;

import java.io.File;


public class PPT extends RelativeLayout implements DocumentSessionStatusListener {

    RelativeLayout.LayoutParams params;
    private DocumentSession session;
    PersentationView slide;
    String path;
    Context ctx;
    Activity act;
    private SlideShowNavigator navitator;
    private int currentSlideNumber;
    private float zoomlevel = 20.0F;
    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (zoomlevel > 25.0F) {
                return false;
            } else {
                float sensitvity = (float) dpToPx(100);
                if (e1.getX() - e2.getX() > sensitvity) {
                    next();
                } else if (e2.getX() - e1.getX() > sensitvity) {
                    prev();
                }

                return true;
            }
        }
    };
    GestureDetector gestureDetector;


    public int getTotalSlides() {
        return this.session != null && this.session.getPPTContext() != null ? this.navitator.getSlideCount() : -1;
    }

    void toast(Object msg) {
        Toast.makeText(this.act, msg.toString(), Toast.LENGTH_SHORT).show();
    }



    public void setPath(String path) {
        this.path = path;
    }

    public void loadPPT(Activity act, String path) {
        this.setPath(path);
        this.loadPPT(act);
    }

    public void loadPPT(Activity act) {
        this.act = act;
        try {
            IMessageProvider msgProvider = new AndroidMessageProvider(this.ctx);
            TempFileManager tmpFileManager = new TempFileManager(new AndroidTempFileStorageProvider(this.ctx));
            ISystemColorProvider sysColorProvider = new AndroidSystemColorProvider();
            this.session = (new DocumentSessionBuilder(new File(this.path))).setMessageProvider(msgProvider).setTempFileManager(tmpFileManager).setSystemColorProvider(sysColorProvider).setSessionStatusListener(this).build();
            this.session.startSession();
        } catch (Exception var5) {
            var5.printStackTrace();
        }

    }

    int dpToPx(int dp) {
        return (int) ((float) dp * Resources.getSystem().getDisplayMetrics().density);
    }

    int pxToDp(int px) {
        return (int) ((float) px / Resources.getSystem().getDisplayMetrics().density);
    }

    public PPT(Context ctx, AttributeSet attr) {
        super(ctx, attr);
        this.gestureDetector = new GestureDetector(this.ctx, this.simpleOnGestureListener);
        this.ctx = ctx;
        this.slide = new PersentationView(ctx, attr);
        this.slide.setVisibility(INVISIBLE);
        this.slide.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        this.params = new RelativeLayout.LayoutParams(-2, -2);
        this.params.addRule(13);
        this.addView(this.slide, this.params);
        this.params = new RelativeLayout.LayoutParams(-2, -2);
        this.params.addRule(13);
        this.params = new RelativeLayout.LayoutParams(this.dpToPx(60), this.dpToPx(60));
        int m = this.dpToPx(10);
        this.params.setMargins(m, m, m, m);
        LinearLayout ll = new LinearLayout(ctx);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        this.params = new RelativeLayout.LayoutParams(-2, -2);
        this.params.addRule(14);
        this.params.addRule(12);
        this.params.bottomMargin = this.dpToPx(30);
        this.addView(ll, this.params);
        this.params = new RelativeLayout.LayoutParams(-2, -2);
        this.params.addRule(0, 1234);
        this.params.topMargin = this.dpToPx(20);
    }

    public void onDocumentException(Exception arg0) {
    }

    public void onDocumentReady() {
        this.act.runOnUiThread(new Runnable() {
            public void run() {
                navitator = new SlideShowNavigator(session.getPPTContext());
                currentSlideNumber = navitator.getFirstSlideNumber() - 1;
                next();
                slide.setVisibility(VISIBLE);
            }
        });
    }

    public void onSessionEnded() {
    }

    public void onSessionStarted() {
    }

    private void navigateTo(int slideNumber) {
        SlideView slideShow = this.navitator.navigateToSlide(this.slide.getGraphicsContext(), slideNumber);
        this.slide.setContentView(slideShow);
    }

    public void next() {
        if (this.navitator != null) {
            if (this.navitator.getFirstSlideNumber() + this.navitator.getSlideCount() - 1 > this.currentSlideNumber) {
                this.navigateTo(++this.currentSlideNumber);
            } else {
                this.toast("IT'S THE LAST SLIDE");
            }
        }

    }

    public void prev() {
        if (this.navitator != null) {
            if (this.navitator.getFirstSlideNumber() < this.currentSlideNumber) {
                this.navigateTo(--this.currentSlideNumber);
            } else {
                this.toast("IT'S THE FIRST SLIDE");
            }
        }

    }
    public void zoomIn() {
        this.zoomlevel += 5.0F;
        this.slide.notifyScale((double) this.zoomlevel / 100.0D);
    }

    public void zoomOut() {
        this.zoomlevel -= 5.0F;
        this.slide.notifyScale((double) this.zoomlevel / 100.0D);
    }
}