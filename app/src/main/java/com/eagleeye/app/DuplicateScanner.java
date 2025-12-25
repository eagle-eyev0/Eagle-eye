package com.eagleeye.app;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DuplicateScanner {

    // Termux-safe locations (usually accessible without special permissions)
    private static final String[] ROOTS = new String[] {
            "/sdcard/Download",
            "/sdcard/Documents",
            "/sdcard/DCIM",
            "/sdcard/Pictures"
    };

    public static int scanDuplicates() {
        Map<String, Integer> seen = new HashMap<>();

        for (String path : ROOTS) {
            scanDir(new File(path), seen);
        }

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

            // Key: name + size (fast). Later we can add hash for accuracy.
            String key = f.getName() + "_" + f.length();
            Integer prev = seen.get(key);
            seen.put(key, (prev == null) ? 1 : (prev + 1));
        }
    }
}
