package com.eagleeye.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtRiskScore, txtMicStatus, txtDupCount, txtRiskListHint;
    private MaterialButton btnScanPerms, btnScanDup;

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

        // placeholders
        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");
        txtRiskListHint.setText("Ready. Tap SCAN PERMISSIONS.");

        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        btnScanDup.setOnClickListener(v -> {
            // Duplicate remover comes later
            txtRiskListHint.setText("Duplicates scan not implemented yet.");
        });
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");

        List<AppScanner.AppPermReport> reports = AppScanner.scanLaunchableApps(this);

        int totalApps = reports.size();
        int appsWithDanger = 0;

        // Overall score = average risk of top 30 apps (sorted by dangerous perms count already)
        int consider = Math.min(30, reports.size());
        int sum = 0;

        for (int i = 0; i < reports.size(); i++) {
            AppScanner.AppPermReport r = reports.get(i);

            if (r.dangerousPerms != null && !r.dangerousPerms.isEmpty()) {
                appsWithDanger++;
            }

            if (i < consider) {
                RiskEngine.RiskResult rr = RiskEngine.scoreFromDangerousPerms(r.dangerousPerms);
                sum += rr.score;
            }
        }

        int overallScore = (consider == 0) ? 0 : (sum / consider);
        String overallLevel = (overallScore >= 70) ? "HIGH" : (overallScore >= 35 ? "MED" : "LOW");

        txtRiskScore.setText(overallLevel + " " + overallScore);
        txtMicStatus.setText("DONE");

        StringBuilder sb = new StringBuilder();
        sb.append("Scanned ").append(totalApps).append(" launchable apps.\n");
        sb.append("Apps with dangerous permissions: ").append(appsWithDanger).append("\n\n");
        sb.append("TOP RISKY APPS:\n");

        int n = Math.min(7, reports.size());
        for (int i = 0; i < n; i++) {
            AppScanner.AppPermReport r = reports.get(i);
            RiskEngine.RiskResult rr = RiskEngine.scoreFromDangerousPerms(r.dangerousPerms);

            sb.append("• ").append(rr.level).append(" ").append(rr.score)
              .append("  ").append(r.label).append("\n")
              .append("  perms: ").append(r.dangerousPerms).append("\n");
        }

        txtRiskListHint.setText(sb.toString());
    }
}
