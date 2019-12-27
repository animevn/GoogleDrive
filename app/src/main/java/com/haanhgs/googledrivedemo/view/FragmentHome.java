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
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.haanhgs.googledrivedemo.R;
import com.haanhgs.googledrivedemo.helper.DriveServiceHelper;
import com.haanhgs.googledrivedemo.model.Files;
import com.haanhgs.googledrivedemo.model.Item;
import com.haanhgs.googledrivedemo.viewmodel.FileViewModel;
import java.util.Collections;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
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
    private static final int OPEN_DOCUMENT = 2;

    private GoogleSignInClient client;
    private DriveServiceHelper helper;
    private FileViewModel viewModel;
    private FragmentActivity activity;
    private FragmentManager manager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = getActivity();
        manager = getFragmentManager();
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        client = GoogleSignIn.getClient(activity, signInOptions);
        startActivityForResult(client.getSignInIntent(), SIGN_IN);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        requestSignIn();
        viewModel = ViewModelProviders.of(activity).get(FileViewModel.class);
        return view;
    }

    private void query() {
        if (helper != null) {
            Log.d(TAG, "Querying for files.");
            helper.queryFiles().addOnSuccessListener(fileList -> {
                Files files = new Files();
                for (File file : fileList.getFiles()) {
                    files.getFileList().add(new Item(file.getId(), file.getName()));
                }
                viewModel.setFilesData(files);
            }).addOnFailureListener(exception ->
                    Log.e(TAG, "Unable to query files.", exception));
        }
    }

    private void logout() {
        client.signOut();
    }

    @OnClick({R.id.bnQuery, R.id.bnLogout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bnQuery:
                query();
                FragmentTransaction ft = manager.beginTransaction();
                FragmentOpen fragmentOpen = new FragmentOpen();
                fragmentOpen.setHelper(helper);
                ft.replace(R.id.flMain, fragmentOpen, "list");
                ft.addToBackStack("list");
                ft.commit();
                break;
            case R.id.bnLogout:
                logout();
                break;
        }
    }

    private Drive getDrive(GoogleSignInAccount googleAccount) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                activity, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(googleAccount.getAccount());
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        return new Drive.Builder(httpTransport, new GsonFactory(), credential)
                .setApplicationName("Drive Demo").build();
    }

    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result).addOnSuccessListener(googleAccount -> {
            Log.d(TAG, "Signed in as " + googleAccount.getEmail());
            helper = new DriveServiceHelper(getDrive(googleAccount));
        }).addOnFailureListener(exception -> Log.e(TAG, "Unable to sign in.", exception));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == SIGN_IN && resultCode == RESULT_OK && resultData != null) {
            handleSignInResult(resultData);
        }
        super.onActivityResult(requestCode, resultCode, resultData);
    }
}
