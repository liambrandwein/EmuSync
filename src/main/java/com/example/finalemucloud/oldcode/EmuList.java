package com.example.finalemucloud.oldcode;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.finalemucloud.R;
import com.example.finalemucloud.configure.EmulatorConfigure;

import java.util.ArrayList;
import java.io.File;
import java.util.List;
import java.util.Scanner;

public class EmuList extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private EmuListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<String> txtAnalyze = new ArrayList<String>();


    private int manInt = 0;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final boolean HAS_BEEN_INITIALIZED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.zold_activity_emu_list);

        ArrayList<ModelRecyclerItem> exampleList = new ArrayList<ModelRecyclerItem>();

        Drawable plusSign = getDrawable(R.drawable.ic_add_black_24dp);

        exampleList.add(new ModelRecyclerItem(plusSign));
        populateList(exampleList);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new EmuListAdapter(exampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new EmuListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(new Intent(EmuList.this, EmulatorConfigure.class));
                mAdapter.notifyItemChanged(position);
            }
        });
    }
    public void populateList(ArrayList<ModelRecyclerItem> tempList) {
        final Drawable blackMenu = getDrawable(R.drawable.ic_menu_black_24dp);
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);


        File mFile = new File("/storage/emulated/0/Download/InfoList.txt");
        try {
            Scanner txtScan = new Scanner(mFile);
            while (txtScan.hasNextLine()) {
                String s;
                s = txtScan.nextLine().split(",")[0];
                txtAnalyze.add(s);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //vvScanningvv
        for (ApplicationInfo packageInfo : packages) {
            for (String str : txtAnalyze) {
                if (packageInfo.packageName.equals(str)) {
                    Drawable icon = pm.getApplicationIcon(packageInfo);
                    CharSequence cS = pm.getApplicationLabel(packageInfo);
                    String name = cS.toString();
                    tempList.add(new ModelRecyclerItem(icon, name, blackMenu));
                }
            }
        }
        //^^^^^^^^^^^
    }

    public void makeNewEmuItem(String dir1, String dir2) {

        /*USE SHARED PREFS HERE TO MAKE A
        NEW FINAL VARIBLE WHEN THIS IS FIRST
        RUN FOR EACH EMULATOR. THEN IN POPULATE
        LIST (OR WHEREVER), USE "HASBEENINITIALIZED"
        THEN, IF IT HAS BEEN INITIALIZED, CHECK IF
        FINAL VARIABLE  SHARED PREF NULL OR NOT TO
        INITIALIZE NEW EMUS
         */


    }
}