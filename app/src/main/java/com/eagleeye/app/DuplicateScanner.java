package com.eagleeye.app;

import android.os.Environment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DuplicateScanner {

    public static int scanDuplicates() {
        File downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File dcim = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);

        Map<String, Integer> seen = new HashMap<>();
        scanDir(downloads, seen);
        scanDir(dcim, seen);

        int duplicates = 0;
        for (int count : seen.values()) {
            if (count > 1) duplicates += (count - 1);
        }
        return duplicates;
    }

    private static void scanDir(File dir, Map<String, Integer> seen) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) continue;
            String key = f.getName() + "_" + f.length();
            seen.put(key, seen.getOrDefault(key, 0) + 1);
        }
    }
}
