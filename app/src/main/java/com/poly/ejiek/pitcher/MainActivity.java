package com.poly.ejiek.pitcher;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.androidplot.xy.XYPlot;

import java.io.File;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;

public class MainActivity extends AppCompatActivity{

    public static String PACKAGE_NAME;

    private XYPlot plot;
    private MediaPlayer mp;
    private Spinner algSpinner;

    private Analyzer analyzer;
    private Sample nativeSample;
    private Sample micSample;
    private boolean firstPitch = true;
    private AudioDispatcher dispatcher;
    private int timeCorrection = 0;

    private ArrayList<Integer> X;
    private ArrayList<Integer> Y;
    private int nulls = 0;
    private int currentNullBlock = 1;
    private int maxNullBlock = 1;
    private float previosPitch = 0;

    private boolean isListening = false;

    private ExampleManager eManager;
    private float sampleRate = 44100;
    private int bufferSize = 1024;

    private File wavFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new AndroidFFMPEGLocator(this);

        PACKAGE_NAME = getPackageName();

        eManager = new ExampleManager();
        createButtons();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This will be a mic button", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void createButtons() {
        ArrayList<Example> examples = eManager.getExamples();

        for (final Example exmpl : examples){
            Button myButton = new Button(this);
            myButton.setText(exmpl.getName());
            myButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //mp = MediaPlayer.create(MainActivity.this, exmpl.getResourceID());
                    //mp.start();
                    Intent intent = new Intent(MainActivity.this, PitchActivity.class);
                    intent.putExtra("Example", exmpl);
                    startActivity(intent);
                }
            });

            LinearLayout ll = (LinearLayout)findViewById(R.id.buttonslayout);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
