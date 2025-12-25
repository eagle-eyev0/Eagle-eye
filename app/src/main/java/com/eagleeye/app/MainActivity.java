package com.eagleeye.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private TextView txtRiskScore;
    private TextView txtMicStatus;
    private TextView txtDupCount;
    private TextView txtRiskListHint;

    private Button btnScanPerms;
    private Button btnScanDup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRiskScore = findViewById(R.id.txtRiskScore);
        txtMicStatus = findViewById(R.id.txtMicStatus);
        txtDupCount  = findViewById(R.id.txtDupCount);
        txtRiskListHint = findViewById(R.id.txtRiskListHint);

        btnScanPerms = findViewById(R.id.btnScanPerms);
        btnScanDup   = findViewById(R.id.btnScanDup);

        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");

        // Permissions scan
        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        // ✅ Duplicate scan (wired)
        btnScanDup.setOnClickListener(v -> {
            txtRiskListHint.setText("Scanning duplicates...");
            int dups = DuplicateScanner.scanDuplicates();
            txtDupCount.setText(String.valueOf(dups));
            txtRiskListHint.setText("Duplicate scan complete");
        });
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");

        List<AppScanner.AppPermReport> reports =
                AppScanner.scanLaunchableApps(this);

        int total = reports.size();
        int risky = 0;

        for (AppScanner.AppPermReport r : reports) {
            if (!r.dangerousPerms.isEmpty()) {
                risky++;
            }
        }

        int score = total == 0 ? 0 : (risky * 100 / total);
        txtRiskScore.setText(String.valueOf(score));
        txtMicStatus.setText("DONE");
        txtRiskListHint.setText("Apps with risky permissions: " + risky);
    }
}
