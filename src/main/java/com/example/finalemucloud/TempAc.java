package com.example.finalemucloud;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalemucloud.dialogs.BackupDialog;
import com.example.finalemucloud.driveapi.DriveServiceHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TempAc extends AppCompatActivity {
    public static final int ADD_EMULATOR_REQUEST = 1;
    public static final int EDIT_EMULATOR_REQUEST = 2;

    public static final String HAS_BEEN_INITIALIZED = "noithasnt";
    public static final String H_B_I_V_2 = "v2 of has been";
    public static final String MULTI_FOLDER_ID = "anything";
    public static final String SHARED_PREFS = "sharedPrefs";

    private EmulatorViewModel emulatorViewModel;

    private BackupDialog backupDialog = new BackupDialog();

    DriveServiceHelper driveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tempac);

        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        requestSignIn();

        RecyclerView recyclerView = findViewById(R.id.emulator_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        EmulatorAdapter adapter = new EmulatorAdapter();
        recyclerView.setAdapter(adapter);

        emulatorViewModel = ViewModelProviders.of(this).get(EmulatorViewModel.class);

        // Initial emu scan & refresh - yes I know I should've used an int for HAS_BEEN_INITIALIZED
        if (!sp.getBoolean(HAS_BEEN_INITIALIZED, false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(HAS_BEEN_INITIALIZED, true);
            editor.apply();


            try {

                PackageManager pm = getPackageManager();
                InputStream is = getAssets().open("InfoList.txt");

                InitialScan initialScan = new InitialScan(pm);
                ArrayList<String[]> emus = initialScan.findEmu(is);

                ExecutorService service = Executors.newFixedThreadPool(2);
                Future<List<Emulator>> finalList = service.submit(new DeadList());

                List<Emulator> emulators = new ArrayList<>();

                try {
                    emulators = finalList.get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                service.shutdown();

                Toast.makeText(this, "Test", Toast.LENGTH_SHORT).show();

                /* TODO: FIX THIS> if (emulators.size() == 0)
                    throw new IOException("No emulators detected");
                */
                ArrayList<String> pNames = new ArrayList<>();
                for (Emulator emulator : emulators) {
                    pNames.add(emulator.getPackagename());
                }

                for (String[] emu : emus) {
                    if (!(pNames.contains(emu[3])))
                        emulatorViewModel.insert(new Emulator(emu[0], emu[1], emu[2], emu[3], 2, true, false, 1));
                }

            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
            }
        }

        emulatorViewModel.getAllEmulators().observe(this, new Observer<List<Emulator>>() {
            @Override
            public void onChanged(List<Emulator> emulators) {
                adapter.setEmulators(emulators);
            }
        });


        adapter.setOnItemClickListener(new EmulatorAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Emulator emulator) {
                Intent intent = new Intent(TempAc.this, AddEditEmulatorActivity.class);
                intent.putExtra("Emulator Item", emulator);
                intent.putExtra(AddEditEmulatorActivity.EXTRA_ID, emulator.getId());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_TITLE, emulator.getTitle());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY1, emulator.getDirectory1());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY2, emulator.getDirectory2());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_PACKAGENAME, emulator.getPackagename());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_SYNCTYPE, emulator.getSyncType());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_MAINUPLOAD, emulator.isMainUpload());
                intent.putExtra(AddEditEmulatorActivity.EXTRA_SECONDARYUPLOAD, emulator.isSecondaryUpload());
                startActivityForResult(intent, EDIT_EMULATOR_REQUEST);
            }

            @Override
            public void onBackupClick(Emulator emulator) {
                driveServiceHelper.getAllFolders().addOnSuccessListener(new OnSuccessListener<HashMap<String, String>>() {
                    @Override
                    public void onSuccess(HashMap<String, String> stringStringHashMap) {
                        if (!stringStringHashMap.containsKey(emulator.getTitle())) {
                            driveServiceHelper.insertFolderFolder(emulator.getTitle(), stringStringHashMap.get("EmuSync")).addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    driveServiceHelper.createFileV3(emulator.getDirectory1(), s).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            Toast.makeText(getApplicationContext(), "Success",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                        }
                        else {
                            driveServiceHelper.createFileV3(emulator.getDirectory1(),stringStringHashMap.get(emulator.getTitle())).addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    Toast.makeText(getApplicationContext(), "Success",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onRestoreClick(Emulator emulator) {
                driveServiceHelper.getAllFolders().addOnSuccessListener(new OnSuccessListener<HashMap<String, String>>() {
                    @Override
                    public void onSuccess(HashMap<String, String> stringStringHashMap) {
                        if (!stringStringHashMap.containsKey(emulator.getTitle())) {
                            Toast.makeText(getApplicationContext(), "No save data to restore", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String[] tmp = emulator.getDirectory1().split("/");
                            String tmp2 = tmp[tmp.length-1];
                            driveServiceHelper.downloadFile3(stringStringHashMap.get(tmp2), emulator.getDirectory1(), 1).addOnSuccessListener(new OnSuccessListener<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 400:
                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data);
                }
                break;
        }

        if (requestCode == ADD_EMULATOR_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(AddEditEmulatorActivity.EXTRA_TITLE);
            String directory1 = data.getStringExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY1);
            String directory2 = data.getStringExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY2);
            String packageName = data.getStringExtra(AddEditEmulatorActivity.EXTRA_PACKAGENAME);
            int syncType = data.getIntExtra(AddEditEmulatorActivity.EXTRA_SYNCTYPE, 2);
            boolean mainUpload = data.getBooleanExtra(AddEditEmulatorActivity.EXTRA_MAINUPLOAD, true);
            boolean secondaryUpload = data.getBooleanExtra(AddEditEmulatorActivity.EXTRA_SECONDARYUPLOAD, false);

            int priority = 2;

            Emulator emulator = new Emulator(title, directory1, directory2, packageName, syncType, mainUpload, secondaryUpload, priority);
            emulatorViewModel.insert(emulator);
            Toast.makeText(this, "Emulator created", Toast.LENGTH_SHORT).show();
        } else if (requestCode == EDIT_EMULATOR_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(AddEditEmulatorActivity.EXTRA_ID, -1);

            if (id == -1) {
                Toast.makeText(this, "Emulator can't be updated", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = data.getStringExtra(AddEditEmulatorActivity.EXTRA_TITLE);
            String directory1 = data.getStringExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY1);
            String directory2 = data.getStringExtra(AddEditEmulatorActivity.EXTRA_DIRECTORY2);
            String packageName = data.getStringExtra(AddEditEmulatorActivity.EXTRA_PACKAGENAME);
            int syncType = data.getIntExtra(AddEditEmulatorActivity.EXTRA_SYNCTYPE, 2);
            boolean mainUpload = data.getBooleanExtra(AddEditEmulatorActivity.EXTRA_MAINUPLOAD, true);
            boolean secondaryUpload = data.getBooleanExtra(AddEditEmulatorActivity.EXTRA_SECONDARYUPLOAD, false);
            int priority = data.getIntExtra(AddEditEmulatorActivity.EXTRA_PRIORITY, 1);

            Emulator emulator = new Emulator(title, directory1, directory2, packageName, syncType, mainUpload, secondaryUpload, priority);
            emulator.setId(id);
            emulatorViewModel.update(emulator);

            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();

        }
        try {
            backupDialog.dismiss();
        } catch (IllegalStateException e) {

        }

    }

    class DeadList implements Callable<List<Emulator>> {
        DeadList() {
        }

        @Override
        public List<Emulator> call() {
            return emulatorViewModel.getAllEmulatorsNotLive();
        }
    }

    private void refresh() {
        try {
            backupDialog.show(getSupportFragmentManager(), "refreshDialog");
        } catch (IllegalStateException fe) {

        }
        SharedPreferences sp = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(HAS_BEEN_INITIALIZED, false);
        editor.apply();
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    private void handleSignInIntent(Intent data) {
        try {
            backupDialog.show(getSupportFragmentManager(), "loadDialog");
        }catch (IllegalStateException fef) {

        }
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {

                        GoogleAccountCredential credential = GoogleAccountCredential.
                                usingOAuth2(TempAc.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("Final Emu Cloud")
                                .build();

                        driveServiceHelper = new DriveServiceHelper(googleDriveService);
                        //TODO: WHEN USING FOLDER EXIST CREATE, MAKE PARENT FOLDER SOMETHING THAT CORRESPONDS TO IGNORE IN METHOD
                        folderExistCreate("EmuSync", "ignore");
                        backupDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Sign in failed", Toast.LENGTH_LONG);
                backupDialog.dismiss();
            }
        });

    }

    private void folderExistCreate(String targetName, String parentFolderName) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            backupDialog.show(getSupportFragmentManager(), "fExist");
        } catch (IllegalStateException ef) {

        }
        driveServiceHelper.doesFolderExist(targetName).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s == null) {
                    if (parentFolderName.equalsIgnoreCase("ignore")) {
                        driveServiceHelper.createFolder(targetName).addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                try {
                                    backupDialog.dismiss();
                                    editor.putString(MULTI_FOLDER_ID, s);
                                    editor.apply();
                                } catch (IllegalStateException f) {

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                                try {
                                    backupDialog.dismiss();
                                } catch (IllegalStateException f) {

                                }
                            }
                        });
                    } else {
                        driveServiceHelper.doesFolderExist(parentFolderName).addOnSuccessListener(new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String s) {
                                if (s == null) {
                                    driveServiceHelper.createFolder(parentFolderName).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            driveServiceHelper.insertFolderFolder(targetName, s).addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(String s) {
                                                    try {
                                                        backupDialog.dismiss();
                                                        editor.putString(MULTI_FOLDER_ID, s);
                                                        editor.apply();
                                                    } catch (IllegalStateException f) {

                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                                                    try {
                                                        backupDialog.dismiss();
                                                    } catch (IllegalStateException f) {

                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                                            try {
                                                backupDialog.dismiss();
                                            } catch (IllegalStateException f) {

                                            }
                                        }
                                    });
                                } else {
                                    driveServiceHelper.insertFolderFolder(targetName, s).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            try {
                                                backupDialog.dismiss();
                                                editor.putString(MULTI_FOLDER_ID, s);
                                                editor.apply();
                                            } catch (IllegalStateException f) {

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                                            try {
                                                backupDialog.dismiss();
                                            } catch (IllegalStateException f) {

                                            }
                                        }
                                    });
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                                try {
                                    backupDialog.dismiss();
                                } catch (IllegalStateException f) {

                                }
                            }
                        });
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG);
                try {
                    backupDialog.dismiss();
                } catch (IllegalStateException f) {

                }
            }
        });
    }

    //TODO: DELETE LATER
    public void uploadListPopulate(File file, HashMap<String, String> filesAndParents) {
        if (file.isDirectory()) {
            File[] tempList = file.listFiles();
            for (File f : tempList) {
                uploadListPopulate(f, filesAndParents);
            }
        }
        else {
            filesAndParents.put(file.getPath(), file.getParent());
        }
    }

}