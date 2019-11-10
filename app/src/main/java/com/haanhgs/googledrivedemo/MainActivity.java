package com.haanhgs.googledrivedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 2;

    private GoogleSignInClient client;
    private DriveServiceHelper helper;
    private String fileID;
    private String openFileID;

    private EditText etTitle;
    private EditText etContent;


    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .build();
        client = GoogleSignIn.getClient(this, signInOptions);
        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    /**
     * Creates a new file via the Drive REST API.
     */
    private void createFile() {
        if (helper != null) {
            Log.d(TAG, "Creating a file.");
            String filename;
            if (!TextUtils.isEmpty(etTitle.getText())) {
                filename = etTitle.getText().toString();
                helper.createFile(filename)
                        .addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String fileId) {
                                helper.saveFile(fileId,
                                        etTitle.getText().toString(),
                                        etContent.getText().toString())
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Couldn't read file.", e);
                                            }
                                        });
                                MainActivity.this.fileID = fileId;
                                etTitle.setText("");
                                etContent.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(TAG, "Couldn't create file.", exception);
                            }
                        });
            }else {
                Toast.makeText(this, "empty filename", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void saveFile() {
        if (helper != null && fileID != null) {
            Log.d(TAG, "Saving " + fileID);

            String fileName = etTitle.getText().toString();
            String fileContent = etContent.getText().toString();

            helper.saveFile(fileID, fileName, fileContent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            etTitle.setText("");
                            etContent.setText("");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Unable to save file via REST.", exception);
                        }
                    });
        }
    }

    private void readFile() {
        if (helper != null) {
            Log.d(TAG, "Reading file " + fileID);

            helper.readFile(fileID)
                    .addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
                        @Override
                        public void onSuccess(Pair<String, String> nameAndContent) {
                            String name = nameAndContent.first;
                            String content = nameAndContent.second;
                            etTitle.setText(name);
                            etContent.setText(content);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Couldn't read file.", exception);
                        }
                    });
        }
    }

    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void query() {
        if (helper != null) {
            Log.d(TAG, "Querying for files.");

            helper.queryFiles()
                    .addOnSuccessListener(new OnSuccessListener<FileList>() {
                        @Override
                        public void onSuccess(FileList fileList) {
                            StringBuilder builder = new StringBuilder();
                            for (File file : fileList.getFiles()) {
                                builder.append(file.getName()).append("\n");
                            }
                            String fileNames = builder.toString();

                            etTitle.setText(String.format("%s", "File List"));
                            etContent.setText(fileNames);

//                            MainActivity.this.setReadOnlyMode();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Unable to query files.", exception);
                        }
                    });
        }
    }



    private void logout(){
        client.signOut();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Store the EditText boxes to be updated when files are opened/created/modified.
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);

        // Set the onClick listeners for the button bar.

        findViewById(R.id.bnCreate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createFile();
            }
        });

        findViewById(R.id.bnOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readFile();
            }
        });

        findViewById(R.id.bnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.saveFile();
            }
        });

        findViewById(R.id.bnQuery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.query();
            }
        });

        findViewById(R.id.bnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.logout();
            }
        });

        // Authenticate the user. For most apps, this should be done when the user performs an
        // action that requires Drive access rather than in onCreate.
        requestSignIn();
    }


    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleAccount) {
                        Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                        // Use the authenticated account to sign in to the Drive service.
                        GoogleAccountCredential credential =
                                GoogleAccountCredential.usingOAuth2(
                                        MainActivity.this,
                                        Collections.singleton(DriveScopes.DRIVE_APPDATA));
                        credential.setSelectedAccount(googleAccount.getAccount());
                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("Drive Demo")
                                .build();

                        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                        // Its instantiation is required before handling any onClick actions.
                        helper = new DriveServiceHelper(googleDriveService);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case REQUEST_CODE_OPEN_DOCUMENT:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    Uri uri = resultData.getData();
                    if (uri != null) {
                        openFileFromFilePicker(uri);
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }



    /**
     * Opens the Storage Access Framework file picker using {@link #REQUEST_CODE_OPEN_DOCUMENT}.
     */
    private void openFilePicker() {
        if (helper != null) {
            Log.d(TAG, "Opening file picker.");
            Intent pickerIntent = helper.createFilePickerIntent();
            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT);
        }
    }

    /**
     * Opens a file from its {@code uri} returned from the Storage Access Framework file picker
     * initiated by {@link #openFilePicker()}.
     */
    private void openFileFromFilePicker(final Uri uri) {
        if (helper != null) {
            Log.d(TAG, "Opening " + uri.getPath());
            helper.openFileUsingStorageAccessFramework(getContentResolver(), uri)
                    .addOnSuccessListener(new OnSuccessListener<Pair<String, String>>() {
                        @Override
                        public void onSuccess(Pair<String, String> nameAndContent) {
                            String name = nameAndContent.first;
                            String content = nameAndContent.second;
                            etTitle.setText(uri.toString());
                            etContent.setText(content);

                            // Files opened through SAF cannot be modified.

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "Unable to open file from picker.", exception);
                        }
                    });
        }
    }







    /**
     * Updates the UI to read-only mode.
     */
    private void setReadOnlyMode() {
        etTitle.setEnabled(false);
        etContent.setEnabled(false);
        openFileID = null;
    }

    /**
     * Updates the UI to read/write mode on the document identified by {@code fileId}.
     */
    private void setReadWriteMode(String fileId) {
        etTitle.setEnabled(true);
        etContent.setEnabled(true);
        openFileID = fileId;
    }
}
