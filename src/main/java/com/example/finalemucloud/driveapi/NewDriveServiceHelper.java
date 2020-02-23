package com.example.finalemucloud.driveapi;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NewDriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public NewDriveServiceHelper(Drive mDriveService) {

        this.mDriveService = mDriveService;
    }


    public Task<String> createFile(String filePath, String parentFolderId) {
        return Tasks.call(mExecutor, () -> {
            File grandFile = new File(filePath);
            if (grandFile.exists()) {
                if (grandFile.isDirectory()) {
                    com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
                    fileMetaData.setName(grandFile.getName());
                    fileMetaData.setMimeType("application/vnd.google-apps.folder");
                    List<String> theList = new ArrayList<>();
                    theList.add(parentFolderId);
                    fileMetaData.setParents(theList);

                    com.google.api.services.drive.model.File upload = null;
                    try {
                        upload = mDriveService.files().create(fileMetaData).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    File[] nextFiles = grandFile.listFiles();
                    for (File file : nextFiles) {
                        createFile(file.getPath(), upload.getId());
                    }
                } else {
                    com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
                    fileMetaData.setName(grandFile.getName());
                    List<String> theList = new ArrayList<>();
                    theList.add(parentFolderId);
                    fileMetaData.setParents(theList);

                    FileContent mediaContent = new FileContent("application/octet-stream", grandFile);

                    com.google.api.services.drive.model.File upload = null;

                    try {
                        upload = mDriveService.files().create(fileMetaData, mediaContent).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        });
    }

    public Task<String> downloadFile2(String id, String parentDirectory, String name, int first) {
        return Tasks.call(mExecutor, () -> {
            if (first == 1) {
                File dir = new File(parentDirectory);
                dir.mkdir();
                downloadFile2(id, parentDirectory, name, 2);
            }

            String q = "trashed=false and '" + id + "' in parents";
            FileList result = mDriveService.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .execute();
            for (com.google.api.services.drive.model.File f : result.getFiles()) {
                String newDirString = parentDirectory + "/" + f.getName();
                File newPath = new File(newDirString);
                if (f.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
                    newPath.mkdir();
                    downloadFile2(f.getId(), newDirString, null, 2);
                }
                else {
                    OutputStream outputStream = new FileOutputStream(newPath);
                    mDriveService.files().get(f.getId()).executeMediaAndDownloadTo(outputStream);
                    outputStream.close();
                }
            }
            return null;

        });
    }
    public Task<String> downloadFile3(String id, String parentDirectory, String name, int first) {
        return Tasks.call(mExecutor, () -> {
            if (first == 1) {
                File dir = new File(parentDirectory);
                if (dir.exists()) {
                    downloadFile3(id, parentDirectory, name, 2);
                }
                else {
                    dir.mkdir();
                    downloadFile3(id, parentDirectory, name, 2);
                }
            }

            String q = "trashed=false and '" + id + "' in parents";
            FileList result = mDriveService.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .execute();
            for (com.google.api.services.drive.model.File f : result.getFiles()) {
                String newDirString = parentDirectory + "/" + f.getName();
                File newPath = new File(newDirString);
                if (f.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
                    if (newPath.exists()) {
                        downloadFile3(f.getId(), newDirString, null, 2);
                    }
                    else {
                        newPath.mkdir();
                        downloadFile3(f.getId(), newDirString, null, 2);
                    }
                }
                else {
                    OutputStream outputStream = new FileOutputStream(newPath);
                    mDriveService.files().get(f.getId()).executeMediaAndDownloadTo(outputStream);
                    outputStream.close();
                }
            }
            return null;

        });
    }
    public Task<String> createFileV3(String filePath, String parentFolderId) {
        return Tasks.call(mExecutor, () -> {
            File grandFile = new File(filePath);
            boolean exists = false;
            String tempId = null;
            if (grandFile.exists()) {
                if (grandFile.isDirectory()) {
                    String q = "mimeType='application/vnd.google-apps.folder' and trashed=false and '" + parentFolderId + "' in parents";
                    FileList result = mDriveService.files().list()
                            .setQ(q)
                            .setSpaces("drive")
                            .execute();
                    for (com.google.api.services.drive.model.File dFile : result.getFiles()) {
                        if (dFile.getName().equals(grandFile.getName())) {
                            exists = true;
                            tempId = dFile.getId();
                            break;
                        }
                    }
                    if (!exists) {
                        com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
                        fileMetaData.setName(grandFile.getName());
                        fileMetaData.setMimeType("application/vnd.google-apps.folder");
                        List<String> theList = new ArrayList<>();
                        theList.add(parentFolderId);
                        fileMetaData.setParents(theList);

                        com.google.api.services.drive.model.File upload = null;
                        try {
                            upload = mDriveService.files().create(fileMetaData).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        File[] nextFiles = grandFile.listFiles();
                        for (File file : nextFiles) {
                            createFileV3(file.getPath(), upload.getId());
                        }

                    }
                    else {
                        File[] nextFiles = grandFile.listFiles();
                        for (File file : nextFiles) {
                            createFileV3(file.getPath(), tempId);
                        }
                    }

                } else {
                    String q = "mimeType!='application/vnd.google-apps.folder' and trashed=false and '" + parentFolderId + "' in parents";
                    FileList result = mDriveService.files().list()
                            .setQ(q)
                            .setSpaces("drive")
                            .execute();
                    for (com.google.api.services.drive.model.File driveFile : result.getFiles()) {
                        if (driveFile.getName().equals(grandFile.getName())) {
                            exists = true;
                            tempId = driveFile.getId();
                            break;
                        }
                    }
                    if (!exists) {
                        com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
                        fileMetaData.setName(grandFile.getName());
                        List<String> theList = new ArrayList<>();
                        theList.add(parentFolderId);
                        fileMetaData.setParents(theList);

                        FileContent mediaContent = new FileContent("application/octet-stream", grandFile);

                        com.google.api.services.drive.model.File upload = null;

                        try {
                            mDriveService.files().create(fileMetaData, mediaContent).execute();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            com.google.api.services.drive.model.File pendingUpdate = new com.google.api.services.drive.model.File();

                            pendingUpdate.setName(grandFile.getName());
                            pendingUpdate.setMimeType("application/octet-stream");

                            FileContent mediaContent = new FileContent("application/octet-stream", grandFile);

                            mDriveService.files().update(tempId, pendingUpdate, mediaContent).execute();

                        }
                        catch (IOException e) {
                            Log.d("he",e.getMessage());
                        }
                    }

                }
            }
            return null;
        });
    }
    public Task<HashMap<String, String>> getAllFolders() {
        return Tasks.call(mExecutor, () -> {
            HashMap<String, String> info = new HashMap<>();
            String pageToken = null;
            try {
                do {
                    FileList result = mDriveService.files().list()
                            .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                    for (com.google.api.services.drive.model.File file : result.getFiles()) {
                        info.put(file.getName(), file.getId());
                    }
                    pageToken = result.getNextPageToken();
                } while (pageToken != null);
            } catch (IOException e) {
                return null;
            }
        return info;
        });
    }


}