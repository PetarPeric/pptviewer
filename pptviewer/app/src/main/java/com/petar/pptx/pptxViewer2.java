package com.petar.pptx;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private Context context;
    private String path;

    private int slideCount = 0;
    private XSLFSlide[] slides;
    private XMLSlideShow ppt;
    private Dimension pgsize;

    private Bitmap slide;
    private boolean isSlideReady = false;

    private void sendBytes(byte[] bytes){

        if(bytes!=null) {
            // sendIntent is the object that will be broadcast outside our app
            Intent sendIntent = new Intent();

            // We add flags for example to work from background
//        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_FROM_BACKGROUND|Intent.FLAG_INCLUDE_STOPPED_PACKAGES  );

            // SetAction uses a string which is an important name as it identifies the sender of the itent and that we will give to the receiver to know what to listen.
            // By convention, it's suggested to use the current package name
            sendIntent.setAction("com.petar.pptx.IntentToUnity");

            // Here we fill the Intent with our data, here just a string with an incremented number in it.
            sendIntent.putExtra(Intent.EXTRA_TEXT, bytes);
            // And here it goes ! our message is send to any other app that want to listen to it.
            context.sendBroadcast(sendIntent);
        }
    }

    //Ucitavanje prezentacije
    //Paramentri: Aktiviti i putanja do prezentacije
    //Automatski pokrene citanje prvog slajda
    public static void load(Context context, String path) {

        pptxViewer2.singleton = new pptxViewer2();

        System.setProperty("javax.xml.stream.XMLInputFactory",
                "com.sun.xml.stream.ZephyrParserFactory");
        System.setProperty("javax.xml.stream.XMLOutputFactory",
                "com.sun.xml.stream.ZephyrWriterFactory");
        System.setProperty("javax.xml.stream.XMLEventFactory",
                "com.sun.xml.stream.events.ZephyrEventFactory");

        Thread.currentThread().setContextClassLoader(
                context.getClass().getClassLoader());

        // some test
//        try {
//            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//            XMLEventReader reader = inputFactory
//                    .createXMLEventReader(new StringReader(
//                            "<doc att=\"value\">some text</doc>"));
//            while (reader.hasNext()) {
//                XMLEvent e = reader.nextEvent();
//                Log.e("HelloStax", "Event:[" + e + "]");
//            }
//        } catch (XMLStreamException e) {
//            Log.e("HelloStax", "Error parsing XML", e);
//        }


        File demoFile = new File(path);
        if (!demoFile.exists()) {
            Toast.makeText(context, "File not exist, loading demo file!", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "File exist!", Toast.LENGTH_SHORT).show();
        }


        singleton.create(context, path);
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
    public static void getPrepareSlide() {
        if (singleton != null) {
            singleton.nativeGetPrepareSlide();
        }

    }

    //Ucitavanje slajda
    //Vraca niz bajtova bitmape
    private void nativeGetPrepareSlide() {
        if (isSlideReady) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            slide.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            slide.recycle();
            slide = null;
            isSlideReady = false;
            sendBytes(byteArray);
        } else {
            sendBytes(null);
        }
    }

    private void create(Context context, String path) {
        this.context = context;
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

                Log.d(TAG, "PowerPointPresentation load with " + slides.length + "slide(s) in " + (System.currentTimeMillis()-time) + "ms.");
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
