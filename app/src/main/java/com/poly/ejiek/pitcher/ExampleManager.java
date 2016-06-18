package com.poly.ejiek.pitcher;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by ejiek on 6/17/16.
 */
public class ExampleManager {
    ArrayList<Example> examples;
    public ExampleManager(){
        examples = new ArrayList<Example>();
        Example thisIsMine = new Example("/thisismine.wav");
        examples.add(thisIsMine);
    }

    public ArrayList<Example> getExamples(){
        return examples;
    }
}
