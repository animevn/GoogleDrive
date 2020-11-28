package com.haanhgs.googledrivedemo.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.MyFile;
import com.haanhgs.googledrivedemo.repo.DriveHelper;
import com.haanhgs.googledrivedemo.viewmodel.FileViewModel;
import java.util.Collections;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {

    @BindView(R.id.bnQuery)
    Button bnQuery;
    @BindView(R.id.bnLogout)
    Button bnLogout;

    private static final String TAG = "D.FragmentHome";
    private static final int SIGN_IN = 1;

    private GoogleSignInClient client;
    private DriveHelper helper;
    private FileViewModel viewModel;
    private FragmentActivity activity;
    private FragmentManager manager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
        if (activity != null){
            manager = activity.getSupportFragmentManager();
        }
    }

    private void initGoogleSignIn(){
        GoogleSignInOptions options = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        client = GoogleSignIn.getClient(activity, options);
    }

    private Drive getDrive(GoogleSignInAccount account){
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                activity, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(account.getAccount());

        //Can use HttpTransport
        HttpTransport transport = AndroidHttp.newCompatibleTransport();

        //or NethttpTransport
        NetHttpTransport transport1 = new NetHttpTransport();

        return new Drive.Builder(transport1, new GsonFactory(), credential)
                .setApplicationName("Drive Demo").build();
    }

    private void createDriveHelper(){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account != null){
            helper = new DriveHelper(getDrive(account));
        }else {
            startActivityForResult(client.getSignInIntent(), SIGN_IN);
        }
    }

    private void handleSignInResult(Intent intent){
        GoogleSignIn.getSignedInAccountFromIntent(intent)
                .addOnSuccessListener(account -> helper = new DriveHelper(getDrive(account)))
                .addOnFailureListener(e -> Log.e(TAG, "login failed"));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN && resultCode == RESULT_OK && data != null){
            handleSignInResult(data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        viewModel = new ViewModelProvider(activity).get(FileViewModel.class);
        initGoogleSignIn();
        createDriveHelper();
        return view;
    }

    private void queryAllFiles(){
        if (helper != null){
            helper.queryAllFiles().addOnSuccessListener(fileList -> {
                Files files = new Files();
                for (File file:fileList.getFiles()){
                    files.getFileList().add(new MyFile(file.getId(), file.getName()));
                }
                viewModel.setFilesData(files);
            }).addOnFailureListener(e -> Log.e(TAG, "cannot extract filelist"));
        }
    }

    private void openFragmentList(){
        if (manager != null){
            FragmentTransaction ft = manager.beginTransaction();
            Fragment fragment = manager.findFragmentByTag("list");
            if (fragment == null){
                FragmentList fragmentList = new FragmentList();
                fragmentList.setHelper(helper);
                ft.replace(R.id.flMain, fragmentList, "list");
                ft.addToBackStack(null);
                ft.commit();
            }else {
                ft.attach(fragment);
            }
        }
    }

    private void logOut(){
        client.signOut();
    }

    @OnClick({R.id.bnQuery, R.id.bnLogout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bnQuery:
                queryAllFiles();
                openFragmentList();
                break;
            case R.id.bnLogout:
                logOut();
                break;
        }
    }
}
