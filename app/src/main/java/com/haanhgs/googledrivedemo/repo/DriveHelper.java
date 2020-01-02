package com.haanhgs.googledrivedemo.repo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DriveHelper {

//    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 8, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>());
    private Drive drive;

    public DriveHelper(Drive drive){
        this.drive = drive;
    }

    public Task<String> createFolder(String folderName){
        return Tasks.call(executor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName);
            File folder = drive.files().create(metadata).execute();
            if (folder == null){
                throw new IOException("Error requesting create new folder");
            }
            return folder.getId();
        });
    }

    public Task<String> createFile(String fileName){
        return Tasks.call(executor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setMimeType("text/plain")
                    .setName(fileName);
            File file = drive.files().create(metadata).execute();
            if (file == null){
                throw new IOException("Error requesting create new folder");
            }
            return file.getId();
        });
    }

    public Task<FileList> queryAllFiles(){
        return Tasks.call(executor, ()->drive.files().list().setSpaces("appDataFolder").execute());
    }

    public Task<Void> saveFile(String fileId, String filename, String content){
        return Tasks.call(executor, ()->{
            File metadata = new File().setName(filename);
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);
            drive.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    public Task<Pair<String, String>> readFile(String fileId){
        return Tasks.call(executor, ()->{
            File metadata = drive.files().get(fileId).execute();
            String name = metadata.getName();
            try(InputStream inputStream = drive.files().get(fileId).executeMediaAsInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))){
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) builder.append(line);
                String content = builder.toString();
                return Pair.create(name, content);
            }
        });
    }

    public void deleteFile(String fileId){
        Tasks.call(executor, () -> {
            drive.files().delete(fileId).execute();
            return null;
        });
    }

    ///////////////////
    //open filepicker//
    ///////////////////
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        return intent;
    }

    public Task<androidx.core.util.Pair<String, String>> openFileUsingStorageAccessFramework(
            final ContentResolver contentResolver, final Uri uri) {
        return Tasks.call(executor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }
            // Read the document's contents as a String.
            String content = "";
            InputStream is = contentResolver.openInputStream(uri);
            if (is != null){
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) stringBuilder.append(line);
                    content = stringBuilder.toString();
                }
            }
            return androidx.core.util.Pair.create(name, content);
        });
    }
}
