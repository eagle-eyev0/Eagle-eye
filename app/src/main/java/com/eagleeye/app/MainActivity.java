package com.eagleeye.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView txtRiskScore;
    private TextView txtMicStatus;
    private TextView txtDupCount;
    private TextView txtRiskListHint;

    private MaterialButton btnScanPerms;
    private MaterialButton btnScanDup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI
        txtRiskScore = findViewById(R.id.txtRiskScore);
        txtMicStatus = findViewById(R.id.txtMicStatus);
        txtDupCount = findViewById(R.id.txtDupCount);
        txtRiskListHint = findViewById(R.id.txtRiskListHint);

        btnScanPerms = findViewById(R.id.btnScanPerms);
        btnScanDup = findViewById(R.id.btnScanDup);

        // Initial state
        txtRiskScore.setText("--");
        txtMicStatus.setText("IDLE");
        txtDupCount.setText("--");
        txtRiskListHint.setText("No scan yet");

        btnScanPerms.setOnClickListener(v -> runPermissionScan());

        btnScanDup.setOnClickListener(v ->
                txtRiskListHint.setText("Duplicate scan coming next step")
        );
    }

    private void runPermissionScan() {
        txtMicStatus.setText("SCANNING...");
        txtRiskListHint.setText("Scanning apps…");

        List<AppScanner.AppPermReport> reports =
                AppScanner.scanLaunchableApps(this);

        int totalRiskScore = 0;
        int riskyApps = 0;

        for (AppScanner.AppPermReport r : reports) {
            if (!r.dangerousPerms.isEmpty()) {
                RiskEngine.RiskResult rr =
                        RiskEngine.scoreFromDangerousPerms(r.dangerousPerms);
                totalRiskScore += rr.score;
                riskyApps++;
            }
        }

        txtRiskScore.setText(String.valueOf(totalRiskScore));
        txtMicStatus.setText("DONE");
        txtRiskListHint.setText(
                riskyApps + " apps with dangerous permissions"
        );
    }
}
