package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.drive.model.File;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.helper.DriveServiceHelper;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.Item;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Files files;
    private DriveServiceHelper helper;
    private FragmentManager manager;

    public void setManager(FragmentManager manager) {
        this.manager = manager;
    }

    public void setHelper(DriveServiceHelper helper) {
        this.helper = helper;
    }

    public void setFiles(Files files) {
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_items, parent, false);
        return new ViewHolder(view);
    }

    private void openDetail(int position){
        FragmentTransaction ft = manager.beginTransaction();
        FragmentDetail fragmentDetail = new FragmentDetail();
        fragmentDetail.setHelper(helper);
        fragmentDetail.setPosition(position);
        ft.replace(R.id.flMain, fragmentDetail, "detail");
        ft.addToBackStack(null);
        ft.commit();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item file = files.getFileList().get(position);
        holder.tvFile.setText(file.getFilename());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDetail(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files == null ? 0 : files.getFileList().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvFile)
        TextView tvFile;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
