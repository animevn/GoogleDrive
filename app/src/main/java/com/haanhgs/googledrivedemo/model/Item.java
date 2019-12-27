package com.haanhgs.googledrivedemo.model;

public class Item {

    private String fileId;
    private String filename;

    public Item(){}

    public Item(String fileId, String fileName) {
        this.fileId = fileId;
        this.filename = fileName;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
