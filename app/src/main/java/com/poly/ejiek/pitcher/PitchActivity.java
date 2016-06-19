package com.poly.ejiek.pitcher;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class PitchActivity extends AppCompatActivity {

    private XYPlot plot;
    private MediaPlayer mp;
    private Spinner algSpinner;
    private Example example;

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
        setContentView(R.layout.activity_pitch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        example = (Example) intent.getParcelableExtra("Example");

        setAlgthToSpinner();

        analyzer = new Analyzer();

        EditText etSampleRate = (EditText) findViewById(R.id.editSampRate);
        EditText etBufSize = (EditText) findViewById(R.id.editBufSize);

        sampleRate = Float.parseFloat(etSampleRate.getText().toString());
        bufferSize = Integer.parseInt(etBufSize.getText().toString());

        X = new ArrayList<>();
        Y = new ArrayList<>();

        mp = MediaPlayer.create(this, example.getResourceID());

        setContentView(R.layout.activity_pitch);
        TextView text = (TextView) findViewById(R.id.result);
        text.setText("example: "+ example.getName() +" "+ example.getPath() +" "+example.getResourceID());

        File externalStorage = Environment.getExternalStorageDirectory();
        wavFile = new File(externalStorage.getAbsolutePath() , "/thisismine.wav");

        eManager = new ExampleManager();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setAlgthToSpinner() {
        Spinner algSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayList<String> spinnerArray = new ArrayList<String>();

        for (PitchProcessor.PitchEstimationAlgorithm alg : PitchProcessor.PitchEstimationAlgorithm.values())
            spinnerArray.add(alg.toString());


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray); //selected item will look like a spinner set from XML
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algSpinner.setAdapter(adapter);
        algSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                analyzer.setAlgorithm(PitchProcessor.PitchEstimationAlgorithm.valueOf(parent.getItemAtPosition(pos).toString()));
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

    }

    public void buttonOnCLick(View v){
        dispatcher = AudioDispatcherFactory.fromPipe(example.getPath(),(int)sampleRate,bufferSize,0);
        nativeWipe();

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                //setContentView(R.layout.activity_main);
                if(pitchInHz != -1) {
                    if(firstPitch){
                        timeCorrection = (int)(audioEvent.getTimeStamp()*10 +0.5d);
                        firstPitch = false;
                    }                    double timeStamp = audioEvent.getTimeStamp();
                    X.add((int) (timeStamp*10 + 0.5d) - timeCorrection);
                    Y.add((int) (pitchInHz + 0.5f));
                }else {
                    nulls++;
                    if (previosPitch == -1){
                        currentNullBlock++;
                        if (currentNullBlock > maxNullBlock) maxNullBlock = currentNullBlock;
                    }else{
                        currentNullBlock = 1;
                    }
                }
                previosPitch = pitchInHz;
            }
        };
        AudioProcessor p = new PitchProcessor(analyzer.getAlgorithm(), sampleRate, bufferSize, pdh);

        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
        mp.start();

    }

    private void nativeWipe() {
        nulls = 0;
        currentNullBlock = 1;
        maxNullBlock = 1;
        previosPitch = 0;
        firstPitch = true;
        X.clear();
        Y.clear();
        X = new ArrayList<>();
        Y = new ArrayList<>();
    }

    public void button2OnCLick(View v){
        Button button=(Button) v;

        setContentView(R.layout.activity_pitch);
        TextView text = (TextView) findViewById(R.id.result);
        text.setText("Dots: " + X.size()+ "; Nulls: "+ nulls + "; max Null Block: " + maxNullBlock);
        TextView micText = (TextView) findViewById(R.id.micResult);
        if(micSample!=null){
            micText.setText("Dots: " + micSample.getSizeX()+ "; Nulls: "+ micSample.getNulls() + "; max Null Block: " + micSample.getMaxNullBlock());
        }
        plot();
    }

    public void listenButtonOnCLick(View v){
        Button button=(Button) v;

        if(isListening == false) {
            analyzer.startMicSample();
            isListening = true;
            button.setText("Stop");
        }else {
            isListening = false;
            analyzer.micStop();
            micSample = analyzer.getMicSample();
            button.setText("Listen");
            //plot();
        }

    }

    public void plot(){
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:
        if(Y.size()>0){
            Integer[] intObj = new Integer[Y.size()+X.size()];
            for (int i=0; i < Y.size(); i++) {
                intObj[i*2] = Integer.valueOf(X.get(i));
                intObj[i*2+1] = Integer.valueOf(Y.get(i));
            }

            Number[] series1Numbers = (Number[])intObj;


            XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                    SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Series1");

            LineAndPointFormatter series1Format = new LineAndPointFormatter();
            series1Format.setPointLabelFormatter(new PointLabelFormatter());
            series1Format.configure(getApplicationContext(),
                    R.xml.line_point_formatter_with_labels);


            plot.addSeries(series1, series1Format);
        }

        if(micSample!=null){
            Number[] series2Numbers = (Number[])micSample.interleave();

            XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                    SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Series2");

            LineAndPointFormatter series2Format = new LineAndPointFormatter();
            series2Format.setPointLabelFormatter(new PointLabelFormatter());
            series2Format.configure(getApplicationContext(),
                    R.xml.line_point_formatter_with_labels_2);


            plot.addSeries(series2, series2Format);
        }


        // add a new series' to the xyplot:

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);

    }

}
