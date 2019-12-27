package com.haanhgs.googledrivedemo.model;

import java.util.ArrayList;
import java.util.List;

public class Files {

    private List<Item> fileList = new ArrayList<>();

    public List<Item> getFileList() {
        return fileList;
    }

    public void setFileList(List<Item> fileList) {
        this.fileList = fileList;
    }
}
