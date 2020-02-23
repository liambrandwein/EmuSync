package com.example.finalemucloud;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = Emulator.class, version = 1)
public abstract class EmulatorDatabase extends RoomDatabase {

    private static EmulatorDatabase instance;

    public abstract EmulatorDao emulatorDao();

    public static synchronized EmulatorDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    EmulatorDatabase.class, "emulator_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {

        private EmulatorDao emulatorDao;

        private PopulateDbAsyncTask(EmulatorDatabase db) {
            emulatorDao = db.emulatorDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }


    }
}
