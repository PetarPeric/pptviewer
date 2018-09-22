package com.petar.pptviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.pbdavey.awt.Graphics2D;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import and.awt.Dimension;

public class Viewer {

    private static Viewer singleton;

    private Activity activity;
    private String path;

    private int numOfFinishedSlide = -1;

    private int slideCount = 0;
    private XSLFSlide[] slides;
    private XMLSlideShow ppt;
    private Dimension pgsize;

    private String tempFolderPath;


    //Ucitavanje prezentacije
    //Paramentri: Aktiviti i putanja do prezentacije
    //Automatski pokrene citanje prvog slajda
    public static void load(Activity activity, String path) {

        Viewer.singleton = new Viewer();

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

//        try {
//            setTitle(path);
//            pptx2png(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }
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
    public static String getPrepareSlide() {
        if (singleton != null) {
            return singleton.nativeGetPrepareSlide();
        } else {
            return null;
        }

    }

    //Ucitavanje slajda
    //Vraca niz bajtova bitmape
    private String nativeGetPrepareSlide() {
        if (slideCount <= numOfFinishedSlide) {
            return tempFolderPath + File.separator + "slide_" + slideCount + ".png";
        } else {
            return "";
        }
    }

    private void create(Activity activity, String path) {
        this.activity = activity;
        this.path = path;

        slideCount = 0;
        numOfFinishedSlide = -1;

        new LoadPowerPointPresentation().execute();
    }

    private void nativePrepareNextSlide() {
        slideCount++;
        if (slideCount >= slides.length) {
            slideCount--;
        }

    }

    private void nativePreviousNextSlide() {

        slideCount--;
        if (slideCount < 0) {
            slideCount++;
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class LoadPowerPointPresentation extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            File tempFolder = new File(Environment.getExternalStorageDirectory().getPath() +
                    File.separator +
                    getAppName(activity) +
                    File.separator +
                    "presentationTemp");

            boolean success = true;
            if (!tempFolder.exists()) {
                success = tempFolder.mkdirs();
            }
            if (success) {
                tempFolderPath = tempFolder.getPath();
                for(File tempFile : tempFolder.listFiles()) {
                    tempFile.delete();
                }
                try {
                    ppt = new XMLSlideShow(OPCPackage.open(path,
                            PackageAccess.READ));
                    pgsize = ppt.getPageSize();
                    System.out.println("pgsize.width: " + pgsize.getWidth()
                            + ", pgsize.height: " + pgsize.getHeight());
                    slides = ppt.getSlides();
                } catch (InvalidFormatException e) {
                    e.printStackTrace();
                }

                Bitmap slide = Bitmap.createBitmap((int) pgsize.getWidth(),
                        (int) pgsize.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(slide);
                Paint paint = new Paint();
                paint.setColor(android.graphics.Color.WHITE);
                paint.setFlags(Paint.ANTI_ALIAS_FLAG);
                canvas.drawPaint(paint);


                for (int i = 0; i < slides.length; i++) {
                    FileOutputStream tempSlideOS = null;
                    try {
                        tempSlideOS = new FileOutputStream(tempFolder.getPath() +
                                File.separator +
                                "slide_" + i + ".png");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    final Graphics2D graphics2d = new Graphics2D(canvas);
                    slides[i].draw(graphics2d);

                    if (tempSlideOS != null) {
                        slide.compress(Bitmap.CompressFormat.PNG, 100, tempSlideOS);
                    }
                    publishProgress(i);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            numOfFinishedSlide = values[0];
        }
    }


    private String getAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException ignored) {
        }
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

}
