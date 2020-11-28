package com.haanhgs.googledrivedemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.MyFile;
import com.haanhgs.googledrivedemo.repo.DriveHelper;
import com.haanhgs.googledrivedemo.view.FragmentDetail;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Files files;
    private DriveHelper helper;
    private FragmentManager manager;

    public void setManager(FragmentManager manager) {
        this.manager = manager;
    }

    public void setHelper(DriveHelper helper) {
        this.helper = helper;
    }

    public void setFiles(Files files) {
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recycler_items, parent, false);
        return new ViewHolder(view);
    }

    private void openDetail(int position){
        FragmentTransaction ft = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag("detail");
        if (fragment == null){
            FragmentDetail fragmentDetail = new FragmentDetail();
            fragmentDetail.setHelper(helper);
            fragmentDetail.setPosition(position);
            ft.replace(R.id.flMain, fragmentDetail, "detail");
            ft.addToBackStack(null);
            ft.commit();
        }else {
            ft.attach(fragment);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyFile file = files.getFileList().get(position);
        holder.tvFile.setText(file.getFilename());
        holder.itemView.setOnClickListener(v -> openDetail(position));
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
