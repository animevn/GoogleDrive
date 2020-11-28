package com.haanhgs.googledrivedemo.viewmodel;

import com.haanhgs.googledrivedemo.model.Files;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FileViewModel extends ViewModel {

    private final MutableLiveData<Files> filesData = new MutableLiveData<>();

    public MutableLiveData<Files> getFilesData() {
        return filesData;
    }

    public void setFilesData(Files files) {
        this.filesData.setValue(files);
    }
}
