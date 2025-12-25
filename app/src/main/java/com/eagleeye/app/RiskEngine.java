package com.eagleeye.app;

import java.util.List;

public class RiskEngine {

    public static class RiskResult {
        public final int score;         // 0–100
        public final String level;      // LOW / MED / HIGH
        public final String explanation;

        public RiskResult(int score, String level, String explanation) {
            this.score = score;
            this.level = level;
            this.explanation = explanation;
        }
    }

    public static RiskResult scoreFromDangerousPerms(List<String> dangerousPerms) {
        if (dangerousPerms == null || dangerousPerms.isEmpty()) {
            return new RiskResult(0, "LOW", "No dangerous permissions detected.");
        }

        int score = 0;

        // Weight by category (simple but effective)
        for (String p : dangerousPerms) {
            if (p.equals("READ_SMS") || p.equals("SEND_SMS")) score += 30;
            else if (p.equals("RECORD_AUDIO")) score += 22;
            else if (p.equals("CAMERA")) score += 20;
            else if (p.equals("ACCESS_FINE_LOCATION")) score += 18;
            else if (p.equals("ACCESS_COARSE_LOCATION")) score += 12;
            else if (p.equals("READ_CONTACTS") || p.equals("WRITE_CONTACTS")) score += 14;
            else if (p.equals("READ_CALL_LOG") || p.equals("WRITE_CALL_LOG")) score += 18;
            else if (p.startsWith("READ_MEDIA_")) score += 8;
            else if (p.equals("READ_EXTERNAL_STORAGE") || p.equals("WRITE_EXTERNAL_STORAGE")) score += 10;
            else score += 6;
        }

        // Cap at 100
        if (score > 100) score = 100;

        String level;
        if (score >= 70) level = "HIGH";
        else if (score >= 35) level = "MED";
        else level = "LOW";

        String explanation = buildExplanation(dangerousPerms, score, level);

        return new RiskResult(score, level, explanation);
    }

    private static String buildExplanation(List<String> perms, int score, String level) {
        StringBuilder sb = new StringBuilder();
        sb.append("Risk ").append(level).append(" (").append(score).append("/100). ");
        sb.append("Signals: ");

        // Mention key ones
        if (perms.contains("RECORD_AUDIO")) sb.append("MIC ");
        if (perms.contains("CAMERA")) sb.append("CAMERA ");
        if (perms.contains("ACCESS_FINE_LOCATION") || perms.contains("ACCESS_COARSE_LOCATION")) sb.append("LOCATION ");
        if (perms.contains("READ_CONTACTS") || perms.contains("WRITE_CONTACTS")) sb.append("CONTACTS ");
        if (perms.contains("READ_SMS") || perms.contains("SEND_SMS")) sb.append("SMS ");
        if (perms.contains("READ_CALL_LOG") || perms.contains("WRITE_CALL_LOG")) sb.append("CALL_LOG ");
        if (perms.contains("READ_EXTERNAL_STORAGE") || perms.contains("WRITE_EXTERNAL_STORAGE")
                || containsMedia(perms)) sb.append("FILES ");

        return sb.toString().trim();
    }

    private static boolean containsMedia(List<String> perms) {
        for (String p : perms) {
            if (p.startsWith("READ_MEDIA_")) return true;
        }
        return false;
    }
}
