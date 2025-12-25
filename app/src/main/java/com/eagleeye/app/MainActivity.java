package com.eagleeye.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {

    private TextView txtRiskScore, txtMicStatus, txtDupCount, txtRiskListHint;
    private Button btnScanPerms, btnScanDup;

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

        // default UI
        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");
        txtRiskListHint.setText("Ready.");

        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        btnScanDup.setOnClickListener(v ->
                txtRiskListHint.setText("Duplicate scan coming next step…")
        );
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");

        List<AppScanner.AppPermReport> reports = AppScanner.scanLaunchableApps(this);

        int totalApps = reports.size();
        int riskyApps = 0;
        int totalRiskScore = 0;

        StringBuilder top = new StringBuilder();

        int shown = 0;
        for (AppScanner.AppPermReport r : reports) {
            if (r.dangerousPerms == null || r.dangerousPerms.isEmpty()) continue;

            riskyApps++;

            RiskEngine.RiskResult rr = RiskEngine.scoreFromDangerousPerms(r.dangerousPerms);
            totalRiskScore += rr.score;

            if (shown < 6) {
                top.append("• ")
                        .append(r.label)
                        .append("  [")
                        .append(rr.level)
                        .append("]  ")
                        .append(r.dangerousPerms.toString())
                        .append("\n");
                shown++;
            }
        }

        int avg = (riskyApps == 0) ? 0 : (totalRiskScore / riskyApps);

        txtRiskScore.setText(String.valueOf(avg));
        txtDupCount.setText(String.valueOf(totalApps));
        txtMicStatus.setText("DONE");

        if (top.length() == 0) {
            txtRiskListHint.setText("No risky permissions found in launchable apps.");
        } else {
            txtRiskListHint.setText(top.toString().trim());
        }
    }
}
