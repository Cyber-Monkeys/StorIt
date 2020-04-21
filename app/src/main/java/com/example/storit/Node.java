package com.example.storit;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class Node implements Parcelable {
    String nodeName;
    Boolean isFolder;

    public Boolean getIsFolder() { return isFolder; }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}
