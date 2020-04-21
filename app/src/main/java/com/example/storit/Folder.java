package com.example.storit;

import android.os.Parcel;

public class Folder extends Node {

    public Folder(String nodeName) {
        this.nodeName = nodeName;
        this.isFolder = true;
    }

    private Folder(Parcel in) {
        nodeName = in.readString();
        byte tmpIsFolder = in.readByte();
        isFolder = tmpIsFolder == 0 ? null : tmpIsFolder == 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nodeName);
        dest.writeByte((byte) (isFolder == null ? 0 : isFolder ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Folder> CREATOR = new Creator<Folder>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
}
