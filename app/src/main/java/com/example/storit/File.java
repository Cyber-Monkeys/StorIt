package com.example.storit;

import java.util.ArrayList;

public class File {
    int fileId;
    int fileSize;
    String fileName;
    String fileType; // can replace with enum
    ArrayList<File> children;

    int fileImage = R.drawable.background_2;
    public File(int fileId, int fileSize, String fileName, String fileType, boolean isFolder) {
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.fileType = fileType;
        if(!isFolder) {
            fileImage = R.drawable.background_2;
        } else {
            fileImage = R.drawable.ic_folder_black_24dp;
        }
    }

    public int getFileImage() {
        return fileImage;
    }

    public void setFileImage(int fileImage) {
        this.fileImage = fileImage;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
