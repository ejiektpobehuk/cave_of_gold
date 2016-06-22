/**
 *  ____  _ _       _
 * |  _ \(_) |_ ___| |__   ___ _ __
 * | |_) | | __/ __| '_ \ / _ \ '__|
 * |  __/| | || (__| | | |  __/ |
 * |_|   |_|\__\___|_| |_|\___|_|
 *
 * Pitcher is a guide to a better intonation in English
 *
 * @author  ejiek
 * @version 0.1
 */
package com.poly.ejiek.pitcher;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * This is the analyzer class
 * main purpose is to extract pitch from audio
 */
public class Analyzer {
    private AudioDispatcher dispatcher;

    private int currentNullBlock;
    private float previosPitch;
    private boolean firstPitch = true;
    private int timeCorrection = 0;
    private Sample sample;
    private Thread analizer;

    private PitchProcessor.PitchEstimationAlgorithm algorithm = PitchProcessor.PitchEstimationAlgorithm.YIN;

    /**
     * Starts recording and processing audio stream from default microphone
     */
    public void startMicSample(){
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
        startSample(22050, 1024);
    }

    /**
     * Stops recording and processing audio stream from default microphone
     */
    public void micStop(){
        dispatcher.stop();
    }

    /**
     * Used to get micSample after {@link #micStop() micStop} method
     * @return {@link Sample Sample}
     */
    public Sample getSample(){
        return sample;
    }

    /**
     * Initializes sample processing by opening file described in Example class object
     * @param example
     * @param sampleRate in Hz
     * @param bufferSize in bytes
     * @return {@link Sample Sample}
     */
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

    /**
     * Initializes sample processing by opening file from the given path
     * @param path absolute path to the audio file
     * @param sampleRate in Hz
     * @param bufferSize in bytes
     * @return {@link Sample Sample}
     */
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

    /**
     * Core part of processing the sample
     * Declares a handler for pitch detection
     * and start a thread jf audio processing
     * @param sampleRate in Hz
     * @param bufferSize in bytes
     */
    private void startSample(final float sampleRate, int bufferSize){
        sample = new Sample();
        timeCorrection = 0;
        currentNullBlock = 1;
        previosPitch = 0;
        firstPitch = true;

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, final AudioEvent audioEvent) {
                final float pitchInHz = result.getPitch();
                double timeStamp = audioEvent.getTimeStamp();
                if (pitchInHz != -1) {
                    if(firstPitch){
                        timeCorrection = (int)(audioEvent.getTimeStamp()*10 +0.5d);
                        firstPitch = false;
                    }
                    sample.addPoint((int) (timeStamp * 1000 + 0.5d) - timeCorrection, (int) (pitchInHz + 0.5f));
                } else {

                    sample.addX((int) (timeStamp * 1000 + 0.5d) - timeCorrection);
                    sample.addYnull();
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

    /**
     * Sets one of PichProcessor algorithms
     * @param algorithm from PitchProcessor
     */
    public void setAlgorithm(PitchProcessor.PitchEstimationAlgorithm algorithm){
        this.algorithm = algorithm;
    }
}
