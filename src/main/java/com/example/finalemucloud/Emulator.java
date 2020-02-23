package com.example.finalemucloud;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "emu_table")
public class Emulator implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private String directory1;

    private String directory2;

    private String packagename;

    private int syncType;

    private boolean mainUpload;

    private boolean secondaryUpload;

    private int priority;

    public Emulator(String title, String directory1, String directory2, String packagename, int syncType, boolean mainUpload, boolean secondaryUpload, int priority) {
        this.title = title;
        this.directory1 = directory1;
        this.directory2 = directory2;
        this.packagename = packagename;
        this.syncType = syncType;
        this.mainUpload = mainUpload;
        this.secondaryUpload = secondaryUpload;
        this.priority = priority;
    }

    protected Emulator(Parcel in) {
        id = in.readInt();
        title = in.readString();
        directory1 = in.readString();
        directory2 = in.readString();
        packagename = in.readString();
        syncType = in.readInt();
        mainUpload = in.readByte() != 0;
        secondaryUpload = in.readByte() != 0;
        priority = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(directory1);
        dest.writeString(directory2);
        dest.writeString(packagename);
        dest.writeInt(syncType);
        dest.writeByte((byte) (mainUpload ? 1 : 0));
        dest.writeByte((byte) (secondaryUpload ? 1 : 0));
        dest.writeInt(priority);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Emulator> CREATOR = new Creator<Emulator>() {
        @Override
        public Emulator createFromParcel(Parcel in) {
            return new Emulator(in);
        }

        @Override
        public Emulator[] newArray(int size) {
            return new Emulator[size];
        }
    };

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDirectory1() {
        return directory1;
    }

    public String getDirectory2() {
        return directory2;
    }

    public String getPackagename() { return packagename; }

    public int getPriority() { return priority; }

    public int getSyncType() {
        return syncType;
    }

    public boolean isMainUpload() {
        return mainUpload;
    }

    public boolean isSecondaryUpload() {
        return secondaryUpload;
    }

}
