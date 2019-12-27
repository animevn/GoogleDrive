package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.helper.DriveServiceHelper;
import com.haanhgs.googledrivedemo.view.FileAdapter;
import com.haanhgs.googledrivedemo.viewmodel.FileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FragmentOpen extends Fragment {

    @BindView(R.id.rvOpenFiles)
    RecyclerView rvOpenFiles;

    private DriveServiceHelper helper;
    private FragmentActivity activity;
    private Context context;
    private FileAdapter adapter;
    private FragmentManager manager;

    public void setHelper(DriveServiceHelper helper) {
        this.helper = helper;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = getActivity();
        manager = getFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open, container, false);
        ButterKnife.bind(this, view);
        FileViewModel viewModel = ViewModelProviders.of(activity).get(FileViewModel.class);
        adapter = new FileAdapter();
        adapter.setHelper(helper);
        adapter.setManager(manager);
        viewModel.getFilesData().observe(this, files -> {
            adapter.setFiles(files);
            adapter.notifyDataSetChanged();
        });
        rvOpenFiles.setLayoutManager(new LinearLayoutManager(context));
        rvOpenFiles.setAdapter(adapter);
        return view;
    }
}
