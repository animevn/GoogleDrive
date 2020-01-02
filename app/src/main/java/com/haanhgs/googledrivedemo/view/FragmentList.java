package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.adapter.FileAdapter;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.MyFile;
import com.haanhgs.googledrivedemo.repo.DriveHelper;
import com.haanhgs.googledrivedemo.viewmodel.FileViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentList extends Fragment {

    @BindView(R.id.rvOpenFiles)
    RecyclerView rvOpenFiles;
    @BindView(R.id.bnCreate)
    Button bnCreate;

    private DriveHelper helper;
    private FragmentActivity activity;
    private Context context;
    private FileAdapter adapter;
    private FragmentManager manager;
    private FileViewModel viewModel;
    private Files files;

    public void setHelper(DriveHelper helper) {
        this.helper = helper;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = getActivity();
        manager = getFragmentManager();
    }

    private void initRecyclerView(){
        adapter = new FileAdapter();
        adapter.setHelper(helper);
        adapter.setManager(manager);
        viewModel.getFilesData().observe(this, files -> {
            this.files = files;
            adapter.setFiles(files);
            adapter.notifyDataSetChanged();
        });
        rvOpenFiles.setLayoutManager(new LinearLayoutManager(context));
        rvOpenFiles.setAdapter(adapter);
    }

    private void deleteFile(String fileId){
        if (helper != null){
            helper.deleteFile(fileId);
        }
    }

    private void setSwipe(){
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                MyFile file = files.getFileList().get(viewHolder.getAdapterPosition());
                deleteFile(file.getFileId());
                files.getFileList().remove(viewHolder.getAdapterPosition());
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        helper.attachToRecyclerView(rvOpenFiles);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(activity).get(FileViewModel.class);
        initRecyclerView();
        setSwipe();
        return view;
    }

    private void openCreate(){
        FragmentTransaction ft = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag("create");
        if (fragment == null){
            FragmentCreate fragmentCreate = new FragmentCreate();
            fragmentCreate.setHelper(helper);
            ft.replace(R.id.flMain, fragmentCreate, "create");
            ft.addToBackStack(null);
            ft.commit();
        }else {
            ft.attach(fragment);
        }

    }

    @OnClick(R.id.bnCreate)
    public void onViewClicked() {
        openCreate();
    }
}
