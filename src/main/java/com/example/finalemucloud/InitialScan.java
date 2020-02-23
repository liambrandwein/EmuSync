package com.example.finalemucloud;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class InitialScan extends Application {

    final PackageManager pm;
    Scanner scan;

    public InitialScan(PackageManager pm) {
        this.pm = pm;

    }

    public ArrayList<String[]> findEmu(InputStream is) {
        scan = new Scanner(is);
        ArrayList<String[]> tempList = new ArrayList<String[]>();

        while(scan.hasNextLine()) {
            String[] s = scan.nextLine().split(",");
            tempList.add(s);
        }

        ArrayList<String[]> emus = new ArrayList<String[]>();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            for (String[] arr : tempList) {
                if (packageInfo.packageName.equalsIgnoreCase(arr[0])) {
                    CharSequence cS = pm.getApplicationLabel(packageInfo);
                    String name = cS.toString();
                    String[] tempArr = {name, arr[1], arr[2], arr[0]};
                    emus.add(tempArr);
                }
            }
        }
    return emus;
    }

    public Drawable findIcon(Emulator emu, Context c) {
        Drawable dr;
        try {
            dr = pm.getApplicationIcon(emu.getPackagename());
        }
        catch (PackageManager.NameNotFoundException e) {
            dr = c.getDrawable(R.drawable.ic_missing);
        }
        return dr;
    }

}
