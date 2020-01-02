package com.haanhgs.googledrivedemo.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.haanhgs.googledrivedemo.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private void openFragmentHome(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("home");
        if (fragment == null){
            FragmentHome fragmentHome = new FragmentHome();
            ft.replace(R.id.flMain, fragmentHome, "home");
            ft.commit();
        }else {
            ft.attach(fragment);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        openFragmentHome();
    }


//    private void openFilePicker() {
//        if (helper != null) {
//            Log.d(TAG, "Opening file picker.");
//            Intent pickerIntent = helper.createFilePickerIntent();
//            startActivityForResult(pickerIntent, OPEN_DOCUMENT);
//        }
//    }

//    private void openFileFromFilePicker(final Uri uri) {
//        if (helper != null) {
//            Log.d(TAG, "Opening " + uri.getPath());
//            helper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
//                    .addOnSuccessListener(nameAndContent -> {
//                        String name = nameAndContent.first;
//                        String content = nameAndContent.second;
//                        etTitle.setText(uri.toString());
//                        etContent.setText(content);
//                    })
//                    .addOnFailureListener(exception ->
//                            Log.e(TAG, "Unable to open file from picker.", exception));
//        }
//    }
}
