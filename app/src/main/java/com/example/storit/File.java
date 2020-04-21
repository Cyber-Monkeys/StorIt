package com.example.storit;

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
