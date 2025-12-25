package com.eagleeye.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtRiskScore, txtMicStatus, txtDupCount, txtRiskListHint;
    private TextView btnScanPerms, btnScanDup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtRiskScore = findViewById(R.id.txtRiskScore);
        txtMicStatus = findViewById(R.id.txtMicStatus);
        txtDupCount = findViewById(R.id.txtDupCount);
        txtRiskListHint = findViewById(R.id.txtRiskListHint);

        btnScanPerms = findViewById(R.id.btnScanPerms);
        btnScanDup = findViewById(R.id.btnScanDup);

        // placeholders for now
        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");

        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        btnScanDup.setOnClickListener(v -> {
            // Step 4 will implement duplicates scan
            txtRiskListHint.setText("Duplicates scan not wired yet (Step 4).");
        });
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");

        // Simple scan on UI thread (fast enough for MVP). We’ll optimize later.
        List<AppScanner.AppPermReport> reports = AppScanner.scanLaunchableApps(this);

        int totalApps = reports.size();
        int appsWithDanger = 0;
        for (AppScanner.AppPermReport r : reports) {
            if (!r.dangerousPerms.isEmpty()) appsWithDanger++;
        }

        // Show top 5 apps by dangerous perms count
        StringBuilder sb = new StringBuilder();
        sb.append("Scanned ").append(totalApps).append(" launchable apps.\n");
        sb.append("Apps with dangerous permissions: ").append(appsWithDanger).append("\n\n");
        sb.append("Top risky (by dangerous permission count):\n");

        int n = Math.min(5, reports.size());
        for (int i = 0; i < n; i++) {
            AppScanner.AppPermReport r = reports.get(i);
            sb.append("• ").append(r.label)
              .append(" (").append(r.dangerousPerms.size()).append(") ")
              .append(r.dangerousPerms)
              .append("\n");
        }

        txtRiskListHint.setText(sb.toString());
        txtMicStatus.setText("DONE");
    }
}
