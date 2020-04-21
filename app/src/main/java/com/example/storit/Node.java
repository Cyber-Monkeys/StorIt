package com.example.storit;

public abstract class Node {
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
