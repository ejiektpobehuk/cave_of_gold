package com.poly.ejiek.pitcher;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by ejiek on 6/18/16.
 */
public class Analyzer {
    private AudioDispatcher micDispatcher;

    private int micCurrentNullBlock;
    private float micPreviosPitch;
    private boolean firstPitch = true;
    private int timeCorrection = 0;
    private Sample micSample;

    private PitchProcessor.PitchEstimationAlgorithm algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN;

    public void startMicSample(){
        micSample = new Sample();
        timeCorrection = 0;
        micCurrentNullBlock = 1;
        micPreviosPitch = 0;

        micDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        PitchDetectionHandler mpdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                if (pitchInHz != -1) {
                    if(firstPitch){
                        timeCorrection = (int)(audioEvent.getTimeStamp()*10 +0.5d);
                        firstPitch = false;
                    }
                    double timeStamp = audioEvent.getTimeStamp();
                    micSample.addX((int) (timeStamp * 10 + 0.5d) - timeCorrection);
                    micSample.addY((int) (pitchInHz + 0.5f));
                } else {
                    micSample.setNulls(micSample.getNulls()+1);
                    if (micPreviosPitch == -1) {
                        micCurrentNullBlock++;
                        if (micCurrentNullBlock > micSample.getMaxNullBlock())
                            micSample.setMaxNullBlock(micCurrentNullBlock);
                    } else {
                        micCurrentNullBlock = 1;
                    }
                }
                micPreviosPitch = pitchInHz;
            }
        };
        AudioProcessor mp = new PitchProcessor(algorithm, 22050, 1024, mpdh);
        micDispatcher.addAudioProcessor(mp);
        new Thread(micDispatcher,"Audio Dispatcher").start();
    }

    public void micStop(){
        micDispatcher.stop();
    }

    public Sample getMicSample(){
        return micSample;
    }

    public PitchProcessor.PitchEstimationAlgorithm getAlgorithm(){
        return algorithm;
    }

    public void setAlgorithm(PitchProcessor.PitchEstimationAlgorithm algorithm){
        this.algorithm = algorithm;
    }
}
