package com.haanhgs.googledrivedemo.view;

import android.os.Bundle;
import com.haanhgs.googledrivedemo.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        FragmentHome fragmentHome = new FragmentHome();
        ft.replace(R.id.flMain, fragmentHome, "home");
        ft.commit();
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
