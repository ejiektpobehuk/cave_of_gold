package com.poly.ejiek.pitcher;

import android.Manifest;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ejiek on 6/17/16.
 */
public class Example implements Parcelable {
    private String name;
    private int resourceID;

    public Example(String name, int resourceID){
        this.name = name;
        this.resourceID = resourceID;
    }

    public String getName() {
        return name.replace('_', ' ');
    }

    public int getResourceID() {
        return resourceID;
    }

    public String getPath() {
        return String.format("android.resource://%s/%s", MainActivity.PACKAGE_NAME, name);
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
}
