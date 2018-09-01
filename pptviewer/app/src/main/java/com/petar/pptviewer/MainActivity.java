package com.petar.pptviewer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File pptFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "test.ppt");

        if(pptFile.exists()){
            Toast.makeText(this, "File exist!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "File not exist!", Toast.LENGTH_SHORT).show();
        }

        PPT pptViewer = findViewById(R.id.pptviewer);
        pptViewer.loadPPT(this, pptFile.getPath());
    }
}
