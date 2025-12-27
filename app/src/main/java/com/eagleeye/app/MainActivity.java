package com.eagleeye.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private static final int REQ_PICK_FOLDER = 1001;

    private TextView txtRiskScore, txtMicStatus, txtDupCount, txtRiskListHint;
    private Button btnScanPerms, btnScanDup, btnPickFolder;

    // Folder selected by user (Storage Access Framework)
    private Uri pickedFolderUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

// Bind TextViews
txtRiskScore = findViewById(R.id.txtRiskScore);
txtMicStatus = findViewById(R.id.txtMicStatus);
txtDupCount = findViewById(R.id.txtDupCount);
txtRiskListHint = findViewById(R.id.txtRiskListHint);

// Bind Buttons
btnScanPerms = findViewById(R.id.btnScanPerms);
btnScanDup = findViewById(R.id.btnScanDup);
btnPickFolder = findViewById(R.id.btnPickFolder);

        // UI refs
        txtRiskScore = findViewById(R.id.txtRiskScore);
        txtMicStatus = findViewById(R.id.txtMicStatus);
        txtDupCount = findViewById(R.id.txtDupCount);
        txtRiskListHint = findViewById(R.id.txtRiskListHint);

        btnScanPerms = findViewById(R.id.btnScanPerms);
        btnScanDup = findViewById(R.id.btnScanDup);
        btnPickFolder = findViewById(R.id.btnPickFolder);

        // default HUD text
        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");
        txtRiskListHint.setText("Pick a folder, then scan duplicates.");

        // PICK FOLDER
        btnPickFolder.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            );
            startActivityForResult(intent, REQ_PICK_FOLDER);
        });

        // SCAN PERMISSIONS
        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        // SCAN DUPLICATES (SAF)
        btnScanDup.setOnClickListener(v -> runDuplicateScan());
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");
        txtRiskListHint.setText("Scanning permissions...");

        List<AppScanner.AppPermReport> reports = AppScanner.scanLaunchableApps(this);

        int totalApps = reports.size();
        int appsWithDanger = 0;
        int totalScore = 0;

        // show a small “top list” in the HUD log
        StringBuilder sb = new StringBuilder();
        int shown = 0;

        for (AppScanner.AppPermReport r : reports) {
            if (r.dangerousPerms != null && !r.dangerousPerms.isEmpty()) {
                appsWithDanger++;
            }

            RiskEngine.RiskResult rr = RiskEngine.scoreFromDangerousPerms(r.dangerousPerms);
            totalScore += rr.score;

            if (shown < 8) {
                sb.append(rr.badge)
                        .append(" ")
                        .append(r.label)
                        .append(" (")
                        .append(r.dangerousPerms == null ? 0 : r.dangerousPerms.size())
                        .append(")")
                        .append("\n");
                shown++;
            }
        }

        int overall = totalApps == 0 ? 0 : (totalScore / totalApps);
        if (overall < 0) overall = 0;
        if (overall > 100) overall = 100;

        txtRiskScore.setText(String.valueOf(overall));
        txtMicStatus.setText("DONE");
        txtRiskListHint.setText(
                "Apps scanned: " + totalApps +
                " | risky apps: " + appsWithDanger +
                "\n\n" + sb
        );
    }

    private void runDuplicateScan() {
        if (pickedFolderUri == null) {
            txtRiskListHint.setText("No folder selected.\nTap PICK FOLDER first.");
            return;
        }

        txtMicStatus.setText("WORKING...");
        txtRiskListHint.setText("Scanning duplicates...");

        int dups = DuplicateScanner.scanDuplicates(this, pickedFolderUri);

        txtDupCount.setText(String.valueOf(dups));
        txtMicStatus.setText("DONE");
        txtRiskListHint.setText("Duplicate scan complete.\nDuplicates found: " + dups);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_FOLDER && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri == null) return;

            // Persist permission so it still works after app restart
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (Exception ignored) { }

            pickedFolderUri = uri;
            txtRiskListHint.setText("Folder selected:\n" + pickedFolderUri.toString());
        }
    }
}
