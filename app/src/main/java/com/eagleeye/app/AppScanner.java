package com.eagleeye.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import java.util.*;

public class AppScanner {

    public static class AppPermReport {
        public final String label;
        public final String packageName;
        public final List<String> dangerousPerms;

        public AppPermReport(String label, String packageName, List<String> dangerousPerms) {
            this.label = label;
            this.packageName = packageName;
            this.dangerousPerms = dangerousPerms;
        }
    }

    // Practical “dangerous-ish” list (you can expand later)
    private static final Set<String> DANGEROUS = new HashSet<>(Arrays.asList(
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.WRITE_CONTACTS",
            "android.permission.READ_SMS",
            "android.permission.SEND_SMS",
            "android.permission.READ_CALL_LOG",
            "android.permission.WRITE_CALL_LOG",
            "android.permission.READ_CALENDAR",
            "android.permission.WRITE_CALENDAR",
            // Android 13+ media
            "android.permission.READ_MEDIA_IMAGES",
            "android.permission.READ_MEDIA_VIDEO",
            "android.permission.READ_MEDIA_AUDIO",
            // Legacy storage
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    ));

    /** Scans LAUNCHABLE apps only (no QUERY_ALL_PACKAGES needed) */
    public static List<AppPermReport> scanLaunchableApps(Context ctx) {
        PackageManager pm = ctx.getPackageManager();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(i, 0);
        List<AppPermReport> out = new ArrayList<>();

        for (ResolveInfo ri : apps) {
            String pkg = ri.activityInfo.packageName;
            String label = ri.loadLabel(pm).toString();

            try {
                PackageInfo pi;
                if (Build.VERSION.SDK_INT >= 33) {
                    pi = pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
                } else {
                    pi = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
                }

                List<String> found = new ArrayList<>();
                if (pi.requestedPermissions != null) {
                    for (String p : pi.requestedPermissions) {
                        if (DANGEROUS.contains(p)) found.add(shortPerm(p));
                    }
                }

                out.add(new AppPermReport(label, pkg, found));

            } catch (Exception ignored) {
                // skip broken entries
            }
        }

        // Sort: apps with most dangerous perms first
        Collections.sort(out, (a, b) -> Integer.compare(b.dangerousPerms.size(), a.dangerousPerms.size()));
        return out;
    }

    private static String shortPerm(String full) {
        int idx = full.lastIndexOf('.');
        return idx >= 0 ? full.substring(idx + 1) : full;
    }
}
