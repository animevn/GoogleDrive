package com.haanhgs.googledrivedemo.helper;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import androidx.core.util.Pair;

public class DriveServiceHelper {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Drive driveService;

    public DriveServiceHelper(Drive driveService) {
        this.driveService = driveService;
    }

    public Task<String> createFolder() {
        return Tasks.call(executor, () -> {
            File metadata = new File()
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName("Drive Demo");

            File googleFile = driveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<String> createFile(final String filename) {
        return Tasks.call(executor, () -> {
            File metadata = new File()
//                        .setParents(Collections.singletonList("root"))
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setMimeType("text/plain")
                    .setName(filename);
            File googleFile = driveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    public Task<Void> saveFile(final String fileId, final String name, final String content) {
        return Tasks.call(executor, () -> {
            // Create a Item containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            driveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    public Task<Pair<String, String>> readFile(final String fileId) {
        return Tasks.call(executor, () -> {
            File metadata = driveService.files().get(fileId).execute();
            String name = metadata.getName();
            // Stream the file contents to a String.
            try (InputStream is = driveService.files().get(fileId).executeMediaAsInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();
                return Pair.create(name, contents);
            }
        });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(executor, () ->
                driveService.files().list().setSpaces("appDataFolder").execute());
    }



    //check later
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        return intent;
    }

    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
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
            return Pair.create(name, content);
        });
    }
}
