package com.poly.ejiek.pitcher;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class Analyzer {
    private AudioDispatcher dispatcher;

    private int currentNullBlock;
    private float previosPitch;
    private boolean firstPitch = true;
    private int timeCorrection = 0;
    private Sample sample;
    private Thread analizer;

    private PitchProcessor.PitchEstimationAlgorithm algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN;

    public void startMicSample(){
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        startSample(22050, 1024);
    }

    public void micStop(){
        dispatcher.stop();
    }

    public Sample getSample(){
        return sample;
    }
    
    public Sample startFileSample(Example example, float sampleRate, int bufferSize){
        dispatcher = AudioDispatcherFactory.fromPipe(example.getPath(),(int)sampleRate,bufferSize,0);
        startSample(sampleRate,bufferSize);
        try {
            analizer.join();
            return sample;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Sample startFileSample(String path, float sampleRate, int bufferSize){
        dispatcher = AudioDispatcherFactory.fromPipe(path,(int)sampleRate,bufferSize,0);
        startSample(sampleRate,bufferSize);
        try {
            analizer.join();
            return sample;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startSample(float sampleRate, int bufferSize){
        sample = new Sample();
        timeCorrection = 0;
        currentNullBlock = 1;
        previosPitch = 0;
        firstPitch = true;

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                if (pitchInHz != -1) {
                    if(firstPitch){
                        timeCorrection = (int)(audioEvent.getTimeStamp()*10 +0.5d);
                        firstPitch = false;
                    }
                    double timeStamp = audioEvent.getTimeStamp();
                    sample.addX((int) (timeStamp * 10 + 0.5d) - timeCorrection);
                    sample.addY((int) (pitchInHz + 0.5f));
                } else {
                    sample.setNulls(sample.getNulls()+1);
                    if (previosPitch == -1) {
                        currentNullBlock++;
                        if (currentNullBlock > sample.getMaxNullBlock())
                            sample.setMaxNullBlock(currentNullBlock);
                    } else {
                        currentNullBlock = 1;
                    }
                }
                previosPitch = pitchInHz;
            }
        };
        AudioProcessor mp = new PitchProcessor(algorithm, sampleRate, bufferSize, pdh);
        dispatcher.addAudioProcessor(mp);
        analizer = new Thread(dispatcher,"Audio Dispatcher");
        analizer.start();
    }

    public void setAlgorithm(PitchProcessor.PitchEstimationAlgorithm algorithm){
        this.algorithm = algorithm;
    }
}
