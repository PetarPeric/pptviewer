package com.petar.pptviewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.pbdavey.awt.Graphics2D;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.io.File;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import and.awt.Dimension;

public class Viewer {

    private static Viewer singleton;

    private Activity activity;
    private String path;

    private int slideCount = 0;
    private XSLFSlide[] slide;
    private XMLSlideShow ppt;
    private Dimension pgsize;


    private Bitmap create(Activity activity, String path){
        this.activity = activity;
        this.path = path;

        slideCount = 0;

        try {
            ppt = new XMLSlideShow(OPCPackage.open(path,
                    PackageAccess.READ));
            pgsize = ppt.getPageSize();
            System.out.println("pgsize.width: " + pgsize.getWidth()
                    + ", pgsize.height: " + pgsize.getHeight());
            slide = ppt.getSlides();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }

        Bitmap bmp = Bitmap.createBitmap((int) pgsize.getWidth(),
                (int) pgsize.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.WHITE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.drawPaint(paint);

        final Graphics2D graphics2d = new Graphics2D(canvas);

        slide[slideCount].draw(graphics2d);

        return bmp;

    }


    public static Bitmap load(Activity activity, String path){

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


        return singleton.create(activity,path);

//        try {
//            setTitle(path);
//            pptx2png(path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }
    }

    public static Bitmap nextSlide(){
        if(singleton!=null){
            return singleton.nativeNextSlide();
        } else {
            return null;
        }
    }

    private Bitmap nativeNextSlide(){
        Bitmap bmp = Bitmap.createBitmap((int) pgsize.getWidth(),
                (int) pgsize.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(android.graphics.Color.WHITE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        canvas.drawPaint(paint);

        final Graphics2D graphics2d = new Graphics2D(canvas);

        slideCount++;
        if(slideCount>=slide.length){
            slideCount--;
        }

        slide[slideCount].draw(graphics2d);

        return bmp;
    }

}
