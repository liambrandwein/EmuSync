package com.example.finalemucloud;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EmulatorDao {

    @Insert
    void insert(Emulator emulator);

    @Update
    void update(Emulator emulator);

    @Delete
    void delete(Emulator emulator);

    @Query("DELETE FROM emu_table")
    void deleteAllEmulators();

    @Query("SELECT * FROM emu_table")
    LiveData<List<Emulator>> getAllEmulators();

    @Query("SELECT * FROM emu_table")
    List<Emulator> getAllEmulatorsNotLive();

}
