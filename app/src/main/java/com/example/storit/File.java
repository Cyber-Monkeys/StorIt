package com.example.storit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class File extends Node{

    int fileId;
    int fileSize;
    String fileType; // can replace with enum
    String fileKey;
    ArrayList<Chunk> chunks;

    public File(int fileId, String nodeName, int fileSize, String fileType, String fileKey) {
        this.fileId = fileId;
        this.nodeName = nodeName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileKey = fileKey;
        this.isFolder = false;
        chunks = new ArrayList<Chunk>();
    }


    private File(Parcel in) {
        fileId = in.readInt();
        nodeName = in.readString();
        fileSize = in.readInt();
        fileType = in.readString();
        fileKey = in.readString();
        byte tmpIsFolder = in.readByte();
        isFolder = tmpIsFolder == 0 ? null : tmpIsFolder == 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fileId);
        dest.writeString(nodeName);
        dest.writeInt(fileSize);
        dest.writeString(fileType);
        dest.writeString(fileKey);
        dest.writeByte((byte) (isFolder == null ? 0 : isFolder ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<File> CREATOR = new Creator<File>() {
        @Override
        public File createFromParcel(Parcel in) {
            return new File(in);
        }

        @Override
        public File[] newArray(int size) {
            return new File[size];
        }
    };

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
