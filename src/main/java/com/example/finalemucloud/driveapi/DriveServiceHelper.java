package com.example.finalemucloud.driveapi;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper implements Parcelable {

    private final Executor mExecutor = Executors.newFixedThreadPool(2);
    private Drive mDriveService;

    public DriveServiceHelper(Drive mDriveService) {

        this.mDriveService = mDriveService;
    }


    protected DriveServiceHelper(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DriveServiceHelper> CREATOR = new Creator<DriveServiceHelper>() {
        @Override
        public DriveServiceHelper createFromParcel(Parcel in) {
            return new DriveServiceHelper(in);
        }

        @Override
        public DriveServiceHelper[] newArray(int size) {
            return new DriveServiceHelper[size];
        }
    };

    public Task<String> createFile(String filePath) {
        return Tasks.call(mExecutor, () -> {

            java.io.File file = new java.io.File(filePath);

            File fileMetaData = new File();
            fileMetaData.setName(file.getName());

            FileContent mediaContent = new FileContent("application/octet-stream", file);

            File myFile = null;
            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myFile == null) {
                throw new IOException("Null result when requesting file creation");
            }

            return myFile.getId();
        });
    }

    public Task<String> createFolder(String name) {

        return Tasks.call(mExecutor, () -> {
            File fileMetadata = new File();
            fileMetadata.setName(name);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File myFile = null;

            try {
                myFile = mDriveService.files().create(fileMetadata).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(myFile.getId());

            return myFile.getId();
        });
    }

    public Task<String> insertFileFolder(String filePath, String folderId) {

        return Tasks.call(mExecutor, () -> {

            java.io.File file = new java.io.File(filePath);

            List<String> theList = new ArrayList<>();
            File fileMetaData = new File();
            fileMetaData.setName(file.getName());
            theList.add(folderId);
            fileMetaData.setParents(theList);

            FileContent mediaContent = new FileContent("application/octet-stream", file);

            File myFile = null;

            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myFile == null) {
                throw new IOException("Null result when requesting file creation");
            }
            return myFile.getId();
        });
    }

    public Task<String> listFolders() {
        return Tasks.call(mExecutor, () -> {
            String pageToken = null;
            try {
                do {
                    FileList result = mDriveService.files().list()
                            .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and name='EmuSync'")
                            .setSpaces("drive")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageToken(pageToken)
                            .execute();
                    for (File file : result.getFiles()) {
                        String s = "Found file: " + file.getName() + " " + file.getId() + "\n";
                        Log.d("yas", s);
                    }
                    pageToken = result.getNextPageToken();
                } while (pageToken != null);
            } catch (IOException e) {
                Log.d("no", "files not found");
            }
            return null;
        });
    }

    public Task<String> doesFolderExist(String target) {
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
                    for (File file : result.getFiles()) {
                        info.put(file.getName(), file.getId());
                    }
                    pageToken = result.getNextPageToken();
                } while (pageToken != null);
            } catch (IOException e) {
                return null;
            }
            if (info.containsKey(target)) {
                Log.d("helo", info.get(target));
                return info.get(target);
            }
            return null;
        });
    }

    public Task<String> insertFolderFolder(String folderName, String parentFolderId) {
        return Tasks.call(mExecutor, () -> {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            List<String> theList = new ArrayList<>();

            theList.add(parentFolderId);
            fileMetadata.setParents(theList);
            File myFile = null;

            try {
                myFile = mDriveService.files().create(fileMetadata).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return myFile.getId();

        });
    }

    public Task<String> updateFile(String fileId, String filePath) {
        return Tasks.call(mExecutor, () -> {
            try {
                File file = new File();
                java.io.File fileContent = new java.io.File(filePath);

                file.setName(fileContent.getName());
                file.setMimeType("application/octet-stream");

                FileContent mediaContent = new FileContent("application/octet-stream", fileContent);

                File updatedFile = mDriveService.files().update(fileId, file, mediaContent).execute();


                return updatedFile.getId();

            }
            catch (IOException e) {
                Log.d("he",e.getMessage());

                return null;
            }

        });
    }

    public Task<String> createFileV2(String filePath, String parentFolderId) {
        return Tasks.call(mExecutor, () -> {
            java.io.File grandFile = new java.io.File(filePath);
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

                    java.io.File[] nextFiles = grandFile.listFiles();
                    for (java.io.File file : nextFiles) {
                        createFileV2(file.getPath(), upload.getId());
                    }
                }
                else {
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

    //TODO: DELETE THIS LATER
    public Task<String> folderParentParent(String filePath) {

        return Tasks.call(mExecutor, () -> {

            java.io.File file = new java.io.File(filePath);

            List<String> theList = new ArrayList<>();
            theList.add("1vxyjCUYBT922Fum0cqvUCVg4gD5XuYE2");
            File fileMetaData = new File();
            fileMetaData.setName(file.getName());
            fileMetaData.setParents(theList);

            FileContent mediaContent = new FileContent("application/octet-stream", file);

            File myFile = null;

            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myFile == null) {
                throw new IOException("Null result when requesting file creation");
            }
            return myFile.getId();
        });
    }
    public Task<String> downloadFile(String id, String downloadDirectory) {
        return Tasks.call(mExecutor, () -> {
            try {
                java.io.File f = new java.io.File(downloadDirectory);
                f.mkdir();
                OutputStream outputStream = new FileOutputStream(f);
                mDriveService.files().get(id).executeMediaAndDownloadTo(outputStream);
                outputStream.close();
            } catch (Exception e) {
                Log.d("Error", e.getMessage());
            }
            /*
            Do the reverse of the upload method - yes it sucks :(
            Permission denied - fix this -UPDATE: SHOULD BE FIXED!
             */
            return null;

        });
    }
    //TODO: DELETE THIS LATER
    public Task<String>getFile (String id) {
        return Tasks.call(mExecutor, () -> {
            File fie = mDriveService.files().get(id).execute();
            Log.d("PAY ATTENTION!!", fie.getMimeType());
            return null;
        });
    }
    //TODO: UPDATE THIS, MAKE IT GOOD - in progress, "downloadFile3"
    public Task<String> downloadFile2(String id, String parentDirectory, int first) {
        return Tasks.call(mExecutor, () -> {
            if (first == 1) {
                java.io.File dir = new java.io.File(parentDirectory);
                dir.mkdir();
                downloadFile2(id, parentDirectory,2);
            }

            String q = "trashed=false and '" + id + "' in parents";
            FileList result = mDriveService.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .execute();
            for (com.google.api.services.drive.model.File f : result.getFiles()) {
                String newDirString = parentDirectory + "/" + f.getName();
                java.io.File newPath = new java.io.File(newDirString);
                if (f.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
                    newPath.mkdir();
                    downloadFile2(f.getId(), newDirString,2);
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
    //TODO: WRITE IN GOOGLE DOC HOW THE HELL THIS METHOD WORKS
    public Task<String> downloadFile3(String id, String parentDirectory, int first) {
        return Tasks.call(mExecutor, () -> {
            if (first == 1) {
                java.io.File dir = new java.io.File(parentDirectory);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                    downloadFile3(id, parentDirectory, 2);
            }

            String q = "trashed=false and '" + id + "' in parents";
            FileList result = mDriveService.files().list()
                    .setQ(q)
                    .setSpaces("drive")
                    .execute();
            for (com.google.api.services.drive.model.File f : result.getFiles()) {
                String newDirString = parentDirectory + "/" + f.getName();
                java.io.File newPath = new java.io.File(newDirString);
                if (f.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {
                    if (newPath.exists()) {
                        downloadFile3(f.getId(), newDirString, 2);
                    }
                    else {
                        newPath.mkdir();
                        downloadFile3(f.getId(), newDirString, 2);
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
            java.io.File grandFile = new java.io.File(filePath);
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

                        java.io.File[] nextFiles = grandFile.listFiles();
                        for (java.io.File file : nextFiles) {
                            createFileV3(file.getPath(), upload.getId());
                        }

                    }
                    else {
                        java.io.File[] nextFiles = grandFile.listFiles();
                        for (java.io.File file : nextFiles) {
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
