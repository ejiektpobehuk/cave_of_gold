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

import java.util.ArrayList;

/**
 * This is a container for audio sample.
 * Main purpose is to contain audio data and metadata(Pitch)
 */
public class Sample {
    private ArrayList<Integer> X;
    private ArrayList<Integer> Y;
    private int nulls;
    private int maxNullBlock;

    /**
     * Only initializes key values
     */
    public Sample(){
        X = new ArrayList<>();
        Y = new ArrayList<>();
        nulls = 0;
        maxNullBlock = 1;
    }

    /**
     * Returns all pitch points time stamps
     * @return list of pitch points time stamps
     */
    public ArrayList<Integer> getX() {
        return X;
    }

    /**
     * Returns all pitch points frequencies
     * @return list of pitch points frequencies
     */
    public ArrayList<Integer> getY() {
        return Y;
    }

    /**
     * Adds timestamp for pitch
     * @param X timestamp for pisth point
     */
    public void addX(int X) {
        this.X.add(X);
    }

    /**
     * Adds frequency for pitch
     * @param Y frequency for pitch point
     */
    public void addY(int Y) {
        this.Y.add(Y);
    }

    /**
     * Returnt total amount of "nulls" returned by pitch detector.
     * Actual return value of pitch detector is -1.
     * @return total amount of "nulls" returned by pitch detector
     */
    public int getNulls() {
        return nulls;
    }

    /**
     * Returns total amount of "nulls" returned by pitch detector.
     * Actual return value of pitch detector is -1
     * @param nulls total amount of "nulls" returned by pitch detector
     */
    public void setNulls(int nulls) {
        this.nulls = nulls;
    }

    /**
     * Returns maximum amount of nulls returned by pitch detector in a raw
     * @return max block of nulls returned by pitch detector
     */
    public int getMaxNullBlock() {
        return maxNullBlock;
    }

    /**
     * Sets maximum amount of nulls returned by pitch detector in a raw
     * @param maxNullBlock max block of nulls returned by pitch detector
     */
    public void setMaxNullBlock(int maxNullBlock) {
        this.maxNullBlock = maxNullBlock;
    }

    /**
     * Return the size of pith timestamps array
     * @return size of pith timestamps array
     */
    public int getSizeX(){
        return X.size();
    }

    /**
     * Returns the size of pitch frequencies array
     * @return size of pitch frequencies array
     */
    public int getSizeY(){
        return Y.size();
    }

    /**
     * Interleaves X(pith timestamps) and Y(pitch frequencies) 1 by 1
     * @return interleaved 1 by 1 X and Y arrays
     */
    public Integer[] interleave(){
        Integer[] intObj = new Integer[Y.size()+X.size()];
        for (int i=0; i < Y.size(); i++) {
            intObj[i*2] = Integer.valueOf(X.get(i));
            intObj[i*2+1] = Integer.valueOf(Y.get(i));
        }
        return  intObj;
    }

}
