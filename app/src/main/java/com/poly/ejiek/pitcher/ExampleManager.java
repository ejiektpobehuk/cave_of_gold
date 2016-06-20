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

import android.content.Context;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Manager extracts all available {@link Example Examples} from all known sources
 * for 0.1 the only known source is res/raw
 */
public class ExampleManager {
    ArrayList<Example> examples;

    /**
     * Extracts {@link Example Examples} from res/raw
     * @param mainContext context of Activity. Contains info
     *                    about files locations
     */
    public ExampleManager(Context mainContext) {
        examples = new ArrayList<>();
        Example example;

        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            try {
                String name = fields[i].getName();
                int resourceID = 0;
                resourceID = fields[i].getInt(fields[i]);
                example = new Example(name, resourceID, mainContext);
                examples.add(example);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns list of extracted {@link Example Examples}
     * @return list of extracted {@link Example Examples}
     */
    public ArrayList<Example> getExamples(){
        return examples;
    }
}
