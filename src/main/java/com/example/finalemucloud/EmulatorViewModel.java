package com.example.finalemucloud;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class EmulatorViewModel extends AndroidViewModel {
    private EmulatorRepository repository;
    private LiveData<List<Emulator>> allEmulators;


    public EmulatorViewModel(@NonNull Application application) {
        super(application);
        repository = new EmulatorRepository(application);
        allEmulators = repository.getAllEmulators();
    }

    public void insert(Emulator emulator) {
        repository.insert(emulator);
    }

    public void update(Emulator emulator) {
        repository.update(emulator);
    }

    public void delete(Emulator emulator) {
        repository.delete(emulator);
    }

    public void deleteAllEmulators() {
        repository.deleteAllEmulators();
    }

    public LiveData<List<Emulator>> getAllEmulators() {
        return allEmulators;
    }

    public List<Emulator> getAllEmulatorsNotLive(){
        return repository.getAllEmulatorsNotLive();
    }
}
