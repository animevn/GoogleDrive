package com.haanhgs.googledrivedemo.model;

import java.util.ArrayList;
import java.util.List;

public class Files {

    private List<MyFile> fileList = new ArrayList<>();

    public List<MyFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<MyFile> fileList) {
        this.fileList = fileList;
    }
}
