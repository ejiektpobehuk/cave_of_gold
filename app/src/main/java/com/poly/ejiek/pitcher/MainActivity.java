package com.poly.ejiek.pitcher;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.pitch.PitchDetector;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import android.graphics.DashPathEffect;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{

    private ArrayList<Integer> X;
    private ArrayList<Integer> Y;
    private int nulls = 0;
    private int currentNullBlock = 1;
    private int maxNullBlock = 1;
    private float previosPitch = 0;


    private Thread rec;
    private ArrayList<Integer> micX;
    private ArrayList<Integer> micY;
    private int micNulls = 0;
    private int micCurrentNullBlock = 1;
    private int micMaxNullBlock = 1;
    private float micPreviosPitch = 0;
    private boolean isListening = false;

    private XYPlot plot;
    private MediaPlayer mp;
    private AudioDispatcher dispatcher;
    private AudioDispatcher micDispatcher;
    private Spinner algSpinner;
    private PitchEstimationAlgorithm algorithm;

    private float sampleRate = 44100;
    private int bufferSize = 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        algorithm = PitchEstimationAlgorithm.YIN;
        setAlgthToSpinner();

        EditText etSampleRate = (EditText) findViewById(R.id.editSampRate);
        EditText etBufSize = (EditText) findViewById(R.id.editBufSize);

        sampleRate = Float.parseFloat(etSampleRate.getText().toString());
        bufferSize = Integer.parseInt(etBufSize.getText().toString());

        X = new ArrayList<>();
        Y = new ArrayList<>();

        mp = MediaPlayer.create(this, R.raw.thisismine);

        File externalStorage = Environment.getExternalStorageDirectory();
        File wavFile = new File(externalStorage.getAbsolutePath() , "/thisismine.wav");
        dispatcher = AudioDispatcherFactory.fromPipe(wavFile.getAbsolutePath(),(int)sampleRate,bufferSize,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                        //setContentView(R.layout.activity_main);
                        if(pitchInHz != -1) {
                            double timeStamp = audioEvent.getTimeStamp();
                            X.add((int) (timeStamp*10 + 0.5d));
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
        AudioProcessor p = new PitchProcessor(algorithm, sampleRate, bufferSize, pdh);

        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();

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

    private void setAlgthToSpinner() {
        Spinner algSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayList<String> spinnerArray = new ArrayList<String>();

        for (PitchEstimationAlgorithm alg : PitchEstimationAlgorithm.values())
                spinnerArray.add(alg.toString());


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray); //selected item will look like a spinner set from XML
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algSpinner.setAdapter(adapter);
        algSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                        algorithm = PitchEstimationAlgorithm.valueOf(parent.getItemAtPosition(pos).toString());
                }

                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

    }

    public void buttonOnCLick(View v){
        Button button=(Button) v;

        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.result);
        text.setText("Dots: " + X.size()+ "; Nulls: "+ nulls + "; max Null Block: " + maxNullBlock);
        mp.start();
        plot();
    }

    public void button2OnCLick(View v){
        Button button=(Button) v;

        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.result);
        text.setText(algorithm.toString());
    }

    public void listenButtonOnCLick(View v){
        Button button=(Button) v;

        if(isListening == false) {
            isListening = true;
            button.setText("Stop");
            micDispatcher = AudioDispatcherFactory.fromDefaultMicrophone((int) sampleRate, bufferSize, 0);

            PitchDetectionHandler mpdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                    final float pitchInHz = result.getPitch();
                    //setContentView(R.layout.activity_main);
                    if (pitchInHz != -1) {
                        double timeStamp = audioEvent.getTimeStamp();
                        micX.add((int) (timeStamp * 10 + 0.5d));
                        micY.add((int) (pitchInHz + 0.5f));
                    } else {
                        micNulls++;
                        if (micPreviosPitch == -1) {
                            micCurrentNullBlock++;
                            if (micCurrentNullBlock > micMaxNullBlock)
                                micMaxNullBlock = micCurrentNullBlock;
                        } else {
                            micCurrentNullBlock = 1;
                        }
                    }
                    micPreviosPitch = pitchInHz;
                }
            };
            AudioProcessor mp = new PitchProcessor(algorithm, sampleRate, bufferSize, mpdh);
            micDispatcher.addAudioProcessor(mp);
            new Thread(micDispatcher, "Audio Dispatcher").start();
        }else {
            isListening = false;
            micDispatcher.stop();
            button.setText("Listen");
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

    public void plot(){
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.plot);

        // create a couple arrays of y-values to plot:

        double myTest[] = new double[5];
        myTest[0] = 0.1;
        myTest[1] = 0.4;
        myTest[2] = 0.2;
        myTest[3] = 0.8;
        myTest[4] = 0.6;
        int roundNum = (int) (myTest[0] + 0.5f);

        Integer[] intObj = new Integer[Y.size()+X.size()];
        for (int i=0; i < Y.size(); i++) {
            intObj[i*2] = Integer.valueOf(X.get(i));
            intObj[i*2+1] = Integer.valueOf(Y.get(i));
        }

        Number[] series1Numbers = (Number[])intObj;

        Arrays.asList(myTest);
        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "Series1");


        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels);

        // just for fun, add some smoothing to the lines:
        // see: http://androidplot.com/smooth-curves-and-androidplot/
        //series1Format.setInterpolationParams(
        //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        //series2Format.setInterpolationParams(
        //        new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);

        // rotate domain labels 45 degrees to make them more compact horizontally:
        plot.getGraphWidget().setDomainLabelOrientation(-45);

    }
}
