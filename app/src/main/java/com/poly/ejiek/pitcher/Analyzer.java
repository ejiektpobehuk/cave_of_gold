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



    private Thread rec;
    private int micCurrentNullBlock = 1;
    private float micPreviosPitch = 0;
    private boolean firstPitch = true;
    private int timeCorrection = 0;
    private Sample micSample;
    private int micNulls = 0;
    private int micMaxNullBlock = 1;

    private PitchProcessor.PitchEstimationAlgorithm algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN;;
    private boolean micFirstPitch = true;
    private int micTimeCorrection = 0;

    public void startMicSample(){
        micSample = new Sample();

        micDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        PitchDetectionHandler mpdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                if (pitchInHz != -1) {
                    if(micFirstPitch){
                        micTimeCorrection = (int)(audioEvent.getTimeStamp()*10 +0.5d);
                        micFirstPitch = false;
                    }
                    double timeStamp = audioEvent.getTimeStamp();
                    micSample.addX((int) (timeStamp * 10 + 0.5d) - micTimeCorrection);
                    micSample.addY((int) (pitchInHz + 0.5f));
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
        AudioProcessor mp = new PitchProcessor(algorithm, 22050, 1024, mpdh);
        micDispatcher.addAudioProcessor(mp);
        new Thread(micDispatcher,"Audio Dispatcher").start();
    }

    public void micStop(){
        micDispatcher.stop();
        micSample.setNulls(micNulls);
        micSample.setMaxNullBlock(micMaxNullBlock);
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
