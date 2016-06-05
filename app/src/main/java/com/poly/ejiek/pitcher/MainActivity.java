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
import android.widget.Button;
import android.widget.EditText;
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

public class MainActivity extends AppCompatActivity {

    private ArrayList<Integer> X;
    private ArrayList<Integer> Y;

    private XYPlot plot;
    MediaPlayer mp;
    AudioDispatcher dispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        X = new ArrayList<>();
        Y = new ArrayList<>();

        TextView text = (TextView) findViewById(R.id.result);
        text.setText("da "+Environment.getDataDirectory());

        mp = MediaPlayer.create(this, R.raw.thisismine);

        File externalStorage = Environment.getExternalStorageDirectory();
        File wavFile = new File(externalStorage.getAbsolutePath() , "/thisismine.wav");
        dispatcher = AudioDispatcherFactory.fromPipe(wavFile.getAbsolutePath(),44100,1024,0);

        //dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                        //setContentView(R.layout.activity_main);
                        if(pitchInHz != -1) {
                            double timeStamp = audioEvent.getTimeStamp();
                            X.add((int) (timeStamp*10 + 0.5d));
                            Y.add((int) (pitchInHz + 0.5f));
                        }
            }
        };
        AudioProcessor p = new PitchProcessor(PitchEstimationAlgorithm.YIN, 44100, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
  /*      new Thread(new Runnable() {
            @Override
            public void run() {
                File externalStorage = Environment.getExternalStorageDirectory();
                File mp3 = new File(externalStorage.getAbsolutePath() , "/thisismine.wav");
                AudioDispatcher adp;
                adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(),44100,5000,2500);
                adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(),5000, AudioManager.STREAM_MUSIC));
                adp.run();
            }
        }).start();*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void buttonOnCLick(View v){
        Button button=(Button) v;

        setContentView(R.layout.activity_main);
        TextView text = (TextView) findViewById(R.id.result);
        text.setText("X: " + X.size()+ "; Y: "+ Y.size());
        mp.start();
        plot();

//        TextView result = (TextView) findViewById(R.id.result);
//        EditText x = (EditText) findViewById(R.id.val_x);
//        EditText y = (EditText) findViewById(R.id.val_y);
//        int xi = Integer.parseInt(x.getText().toString());
//        int yi = Integer.parseInt(y.getText().toString());
        //result.setVisibility(0);
        //result.setText(Integer.toString(xi+yi));;

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
