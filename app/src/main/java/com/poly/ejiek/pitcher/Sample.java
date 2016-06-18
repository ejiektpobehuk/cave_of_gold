package com.poly.ejiek.pitcher;

import java.util.ArrayList;

/**
 * Created by ejiek on 6/18/16.
 */
public class Sample {
    private ArrayList<Integer> X;
    private ArrayList<Integer> Y;
    private int nulls;
    private int maxNullBlock;

    public Sample(){
        X = new ArrayList<>();
        Y = new ArrayList<>();
        nulls = 0;
        maxNullBlock = 1;
    }

    public ArrayList<Integer> getX() {
        return X;
    }

    public ArrayList<Integer> getY() {
        return Y;
    }

    public void addX(int X) {
        this.X.add(X);
    }

    public void addY(int Y) {
        this.Y.add(Y);
    }

    public int getNulls() {
        return nulls;
    }

    public void setNulls(int nulls) {
        this.nulls = nulls;
    }

    public int getMaxNullBlock() {
        return maxNullBlock;
    }

    public void setMaxNullBlock(int maxNullBlock) {
        this.maxNullBlock = maxNullBlock;
    }

    public int getSizeX(){
        return X.size();
    }

    public int getSizeY(){
        return Y.size();
    }

    public Integer[] interleave(){
        Integer[] intObj = new Integer[Y.size()+X.size()];
        for (int i=0; i < Y.size(); i++) {
            intObj[i*2] = Integer.valueOf(X.get(i));
            intObj[i*2+1] = Integer.valueOf(Y.get(i));
        }
        return  intObj;
    }

    public boolean isEmpty(){
        return X.isEmpty();
    }

}
