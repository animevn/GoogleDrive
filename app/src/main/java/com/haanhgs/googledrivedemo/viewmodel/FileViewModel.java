package com.haanhgs.googledrivedemo.viewmodel;

import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.Item;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FileViewModel extends ViewModel {

    private final MutableLiveData<Files> filesData = new MutableLiveData<>();

    public MutableLiveData<Files> getFilesData() {
        return filesData;
    }

    public void setFilesData(Files files) {
        filesData.setValue(files);
    }

    public void setFiles(String fileId, String filename) {
        Files files = filesData.getValue() == null ? new Files() : filesData.getValue();
        files.getFileList().add(new Item(fileId, filename));
        filesData.setValue(files);
    }
}
