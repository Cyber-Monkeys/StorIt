package com.example.storit;

import java.util.ArrayList;
import java.util.List;

public class File {

    private String name;
    private String type;

    public File() {}

    public File(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

class FileDocument {
    public List<File> files;

    public FileDocument() {}
}