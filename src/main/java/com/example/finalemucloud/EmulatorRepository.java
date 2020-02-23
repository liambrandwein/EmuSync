package com.example.finalemucloud;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class EmulatorRepository {

    private EmulatorDao emulatorDao;
    private LiveData<List<Emulator>> allEmulators;

    public EmulatorRepository(Application application) {
        EmulatorDatabase database = EmulatorDatabase.getInstance(application);

        emulatorDao = database.emulatorDao();
        allEmulators = emulatorDao.getAllEmulators();

    }

    public void insert(Emulator emulator) {
        new InsertEmulatorAsyncTask(emulatorDao).execute(emulator);
    }
    public void update(Emulator emulator) {
        new UpdateEmulatorAsyncTask(emulatorDao).execute(emulator);
    }
    public void delete(Emulator emulator) {
        new DeleteEmulatorAsyncTask(emulatorDao).execute(emulator);
    }

    public void deleteAllEmulators() {
        new DeleteAllEmulatorsAsyncTask(emulatorDao).execute();
    }

    public LiveData<List<Emulator>> getAllEmulators(){
        return allEmulators;
    }
    public List<Emulator> getAllEmulatorsNotLive(){
        return emulatorDao.getAllEmulatorsNotLive();
    }

    private static class InsertEmulatorAsyncTask extends AsyncTask<Emulator, Void, Void> {
        private EmulatorDao emulatorDao;

        private InsertEmulatorAsyncTask(EmulatorDao emulatorDao) {
            this.emulatorDao = emulatorDao;
        }

        @Override
        protected Void doInBackground(Emulator... emulators) {
            emulatorDao.insert(emulators[0]);
            return null;
        }
    }
    private static class UpdateEmulatorAsyncTask extends AsyncTask<Emulator, Void, Void> {
        private EmulatorDao emulatorDao;

        private UpdateEmulatorAsyncTask(EmulatorDao emulatorDao) {
            this.emulatorDao = emulatorDao;
        }

        @Override
        protected Void doInBackground(Emulator... emulators) {
            emulatorDao.update(emulators[0]);
            return null;
        }
    }
    private static class DeleteEmulatorAsyncTask extends android.os.AsyncTask<Emulator, Void, Void> {
        private EmulatorDao emulatorDao;

        private DeleteEmulatorAsyncTask(EmulatorDao emulatorDao) {
            this.emulatorDao = emulatorDao;
        }

        @Override
        protected Void doInBackground(Emulator... emulators) {
            emulatorDao.delete(emulators[0]);
            return null;
        }
    }

    private static class DeleteAllEmulatorsAsyncTask extends AsyncTask<Void, Void, Void> {
        private EmulatorDao emulatorDao;

        private DeleteAllEmulatorsAsyncTask(EmulatorDao emulatorDao) {
            this.emulatorDao = emulatorDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            emulatorDao.deleteAllEmulators();
            return null;
        }
    }


}
