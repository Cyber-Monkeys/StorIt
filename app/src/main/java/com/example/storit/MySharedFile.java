package com.example.storit;

public class MySharedFile {
    int sharedUser;
    int nodeId;
    String nodeName;
    int fileSize;
    String fileType;
    String fileKey;

    public MySharedFile(int sharedUser, int nodeId, String nodeName, int fileSize, String fileType, String fileKey) {
        this.sharedUser = sharedUser;
        this.nodeId = nodeId;
        this.nodeName = nodeName;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.fileKey = fileKey;
    }

    public int getSharedUser() {
        return sharedUser;
    }

    public void setSharedUser(int sharedUser) {
        this.sharedUser = sharedUser;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public void revokeAccess() {
    // send request to tracker remove reference to this file
        // send request to firebase, remove file from shared Files from other
        // move sharedFileFolder from sharedFileFolder to fileFolder
    }
}
