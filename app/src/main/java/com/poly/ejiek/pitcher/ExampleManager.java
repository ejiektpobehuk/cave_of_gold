package com.poly.ejiek.pitcher;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by ejiek on 6/17/16.
 */
public class ExampleManager {
    ArrayList<Example> examples;

    public ExampleManager() {
        examples = new ArrayList<>();
        Example example;

        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            try {
                String name = fields[i].getName();
                int resourceID = 0;
                resourceID = fields[i].getInt(fields[i]);
                example = new Example(name, resourceID);
                examples.add(example);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<Example> getExamples(){
        return examples;
    }
}
