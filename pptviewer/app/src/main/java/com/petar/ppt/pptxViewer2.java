package com.petar.ppt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.pbdavey.awt.Graphics2D;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import and.awt.Dimension;

public class pptxViewer2 {


    private static String TAG = "pptxViewer2";

    private static pptxViewer2 singleton;

    //    private Activity activity;
    private String path;

    private int slideCount = 0;
    private XSLFSlide[] slides;
    private XMLSlideShow ppt;
    private Dimension pgsize;

    private Bitmap slide;
    private boolean isSlideReady = false;

    //Ucitavanje prezentacije
    //Paramentri: Aktiviti i putanja do prezentacije
    //Automatski pokrene citanje prvog slajda
    public static void load(Activity activity, String path) {

        pptxViewer2.singleton = new pptxViewer2();

        System.setProperty("javax.xml.stream.XMLInputFactory",
                "com.sun.xml.stream.ZephyrParserFactory");
        System.setProperty("javax.xml.stream.XMLOutputFactory",
                "com.sun.xml.stream.ZephyrWriterFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory",
                "com.sun.xml.stream.events.ZephyrEventFactory");

        Thread.currentThread().setContextClassLoader(
                activity.getClass().getClassLoader());

        // some test
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = inputFactory
                    .createXMLEventReader(new StringReader(
                            "<doc att=\"value\">some text</doc>"));
            while (reader.hasNext()) {
                XMLEvent e = reader.nextEvent();
                Log.e("HelloStax", "Event:[" + e + "]");
            }
        } catch (XMLStreamException e) {
            Log.e("HelloStax", "Error parsing XML", e);
        }


        File demoFile = new File(path);
        if (!demoFile.exists()) {
            Toast.makeText(activity, "File not exist, loading demo file!", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(activity, "File exist!", Toast.LENGTH_SHORT).show();
        }


        singleton.create(activity, path);
    }

    //Zahtev za pripremu seledeg slajda
    public static void prepareNextSlide() {
        if (singleton != null) {
            singleton.nativePrepareNextSlide();
        }
    }

    //Zahtev za pripremu seledeg slajda
    public static void preparePreviosSlide() {
        if (singleton != null) {
            singleton.nativePreviousNextSlide();
        }
    }

    //Zahtev za pripremu predhodnog slajda
    public static byte[] getPrepareSlide() {
        if (singleton != null) {
            return singleton.nativeGetPrepareSlide();
        } else {
            return null;
        }

    }

    //Ucitavanje slajda
    //Vraca niz bajtova bitmape
    private byte[] nativeGetPrepareSlide() {
        if (isSlideReady) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            slide.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            slide.recycle();
            slide = null;
            isSlideReady = false;
            return byteArray;
        } else {
            return null;
        }
    }

    private void create(Activity activity, String path) {
//        this.activity = activity;
        this.path = path;

        slideCount = 0;

        new LoadPowerPointPresentation().execute();
    }

    private void nativePrepareNextSlide() {


        slideCount++;
        if (slideCount >= slides.length) {
            slideCount--;
        } else {
            new LoadSlide().execute();
        }

    }

    private void nativePreviousNextSlide() {

        slideCount--;
        if (slideCount < 0) {
            slideCount++;
        } else {
            new LoadSlide().execute();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class LoadPowerPointPresentation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            long time = System.currentTimeMillis();
            try {
                Log.d(TAG, "Starting to load PowerPointPresentation.");
                ppt = new XMLSlideShow(OPCPackage.open(path,
                        PackageAccess.READ));
                pgsize = ppt.getPageSize();
                System.out.println("pgsize.width: " + pgsize.getWidth()
                        + ", pgsize.height: " + pgsize.getHeight());
                slides = ppt.getSlides();

                Log.d(TAG, "PowerPointPresentation load with " + slides.length + "slide(s) in " + (-time) + "ms.");
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }

            time = System.currentTimeMillis();
            slide = Bitmap.createBitmap((int) pgsize.getWidth(),
                    (int) pgsize.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(slide);
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.WHITE);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            canvas.drawPaint(paint);

            final Graphics2D graphics2d = new Graphics2D(canvas);

            slides[slideCount].draw(graphics2d);

            time -= System.currentTimeMillis();
            Log.d(TAG, "Slide 1. is load in bitmap for " + (-time) + "ms ");
            isSlideReady = true;

            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadSlide extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            long time = System.currentTimeMillis();
            slide = Bitmap.createBitmap((int) pgsize.getWidth(),
                    (int) pgsize.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(slide);
            Paint paint = new Paint();
            paint.setColor(android.graphics.Color.WHITE);
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            canvas.drawPaint(paint);

            final Graphics2D graphics2d = new Graphics2D(canvas);

            slides[slideCount].draw(graphics2d);

            time -= System.currentTimeMillis();
            Log.d(TAG, "Slide " + (slideCount + 1) + " is load in bitmap for " + (-time) + "ms ");
            isSlideReady = true;
            return null;
        }
    }

}
