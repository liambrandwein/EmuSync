package com.example.finalemucloud;

import java.io.File;
import java.util.Stack;

public class Tests {
    private static long dirSize(File dir) {
        long result = 0;

        if (!dir.exists())
            return result;

        Stack<File> dirlist = new Stack<File>();
        dirlist.clear();

        dirlist.push(dir);

        while (!dirlist.isEmpty()) {
            File dirCurrent = dirlist.pop();

            File[] fileList = dirCurrent.listFiles();
            for (File f : fileList) {
                if (f.isDirectory())
                    dirlist.push(f);
                else
                    result += f.length();
            }
        }

        return result;
    }

}
