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
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by ejiek on 6/17/16.
 */
public class Example implements Parcelable {
    private String name;
    private int resourceID;
    private Context mainContext;

    public Example(String name, int resourceID, Context mainContext){
        this.name = name;
        this.resourceID = resourceID;
        this.mainContext = mainContext;
    }

    public String getName() {
        return name.replace('_', ' ');

    }

    public int getResourceID() {
        return resourceID;
    }

    public String getPath() {
        InputStream fis = null;
        try {
            fis = mainContext.getApplicationContext().getResources().openRawResource(resourceID);
            java.io.File oFile = new java.io.File((mainContext.getFileStreamPath(name+".wav").getPath()));
            OutputStream os = new FileOutputStream(oFile);
            CopyStream(fis, os);
            fis.close();
            os.close();
            Uri uri = Uri.fromFile(oFile);
            return uri.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (;;) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    public void setContext(Context mainContext) {
        this.mainContext = mainContext;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(resourceID);
    }

    public static final Parcelable.Creator<Example> CREATOR = new Parcelable.Creator<Example>() {
        public Example createFromParcel(Parcel in) {
            return new Example(in);
        }

        public Example[] newArray(int size) {
            return new Example[size];
        }
    };

    private Example(Parcel in) {
        name = in.readString();
        resourceID = in.readInt();
    }

    @Override
    public String toString() {
        return getName();
    }
}
