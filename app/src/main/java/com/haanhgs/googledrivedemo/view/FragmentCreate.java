package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.helper.DriveServiceHelper;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.Item;
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

public class FragmentCreate extends Fragment {

    private static final String TAG = "D.FragmentDetail";
    @BindView(R.id.etTitle)
    EditText etTitle;
    @BindView(R.id.etContent)
    EditText etContent;
    @BindView(R.id.bnSave)
    Button bnSave;

    private DriveServiceHelper helper;
    private FileViewModel viewModel;
    private Files files;
    private FragmentManager manager;
    private FragmentActivity activity;
    private Context context;

    public void setHelper(DriveServiceHelper helper) {
        this.helper = helper;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        manager = getFragmentManager();
        activity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(activity).get(FileViewModel.class);
        viewModel.getFilesData().observe(this, files -> FragmentCreate.this.files = files);
        return view;
    }

    private void saveFileWhenCreated(String fileId) {
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        helper.saveFile(fileId, title, content)
                .addOnFailureListener(e -> Log.e(TAG, "Couldn't read file.", e))
                .addOnSuccessListener(aVoid -> {
                    Fragment fragment = manager.findFragmentByTag("create");
                    manager.popBackStack();
                    files.getFileList().add(new Item(fileId, title));
                    viewModel.setFilesData(files);
                });
    }

    private void createFile() {
        if (helper != null) {
            Log.d(TAG, "Creating a file.");
            if (!TextUtils.isEmpty(etTitle.getText())) {
                String filename = etTitle.getText().toString();
                helper.createFile(filename)
                        .addOnSuccessListener(this::saveFileWhenCreated)
                        .addOnFailureListener(exception ->
                                Log.e(TAG, "Couldn't create file.", exception));
            } else {
                Toast.makeText(context, "empty filename", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.bnSave)
    public void onViewClicked() {
        createFile();
    }
}
