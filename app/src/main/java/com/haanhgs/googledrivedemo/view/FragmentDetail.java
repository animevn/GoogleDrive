package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.repo.DriveHelper;
import com.haanhgs.googledrivedemo.viewmodel.FileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentDetail extends Fragment {

    @BindView(R.id.etTitle)
    EditText etTitle;
    @BindView(R.id.etContent)
    EditText etContent;
    @BindView(R.id.bnSave)
    Button bnSave;

    private static final String TAG = "D.FragmentDetail";
    private DriveHelper helper;
    private FileViewModel viewModel;
    private Files files;
    private int position;
    private FragmentManager manager;
    private FragmentActivity activity;

    public void setHelper(DriveHelper helper) {
        this.helper = helper;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        manager = getFragmentManager();
        activity = getActivity();
    }

    private void readFile(String fileID) {
        if (helper != null) {
            Log.d(TAG, "Reading file " + fileID);
            helper.readFile(fileID).addOnSuccessListener(nameAndContent -> {
                String name = nameAndContent.first;
                String content = nameAndContent.second;
                etTitle.setText(name);
                etContent.setText(content);
            }).addOnFailureListener(exception ->
                    Log.e(TAG, "Couldn't read file.", exception));
        }
    }

    private void saveFile(String fileID) {
        if (helper != null && fileID != null) {
            String fileName = etTitle.getText().toString();
            String fileContent = etContent.getText().toString();
            files.getFileList().get(position).setFilename(fileName);
            helper.saveFile(fileID, fileName, fileContent).addOnSuccessListener(aVoid -> {
                Fragment fragment = manager.findFragmentByTag("detail");
                manager.popBackStack();
                viewModel.setFilesData(files);
            }).addOnFailureListener(exception ->
                    Log.e(TAG, "Unable to save file via REST.", exception));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(activity).get(FileViewModel.class);
        viewModel.getFilesData().observe(this, files -> {
            FragmentDetail.this.files = files;
            readFile(files.getFileList().get(position).getFileId());
        });
        return view;
    }

    @OnClick(R.id.bnSave)
    public void onViewClicked(){
        saveFile(files.getFileList().get(position).getFileId());
    }
}
