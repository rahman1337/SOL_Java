package com.solana.scanner;

public class DashboardUpdater implements Runnable {
    
    private long tryCount;
    private long hitCount;
    private long lastUpdateTime;
    
    public DashboardUpdater(long tryCount, long hitCount) {
        this.tryCount = tryCount;
        this.hitCount = hitCount;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    @Override
    public void run() {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - lastUpdateTime;
        
        // Update every 100ms
        if (timeDiff >= 100) {
            updateDashboard();
            lastUpdateTime = currentTime;
        }
    }
    
    private void updateDashboard() {
        StringBuilder sb = new StringBuilder();
        sb.append("\rTries: ").append(formatNumber(tryCount));
        sb.append(" | Hits: ").append(formatNumber(hitCount));
        
        System.out.print(sb.toString());
        System.out.flush();
    }
    
    private String formatNumber(long num) {
        if (num >= 1_000_000) {
            return String.format("%.2fM", num / 1_000_000.0);
        } else if (num >= 1_000) {
            return String.format("%.2fK", num / 1_000.0);
        }
        return String.valueOf(num);
    }
    
    public void setTryCount(long count) {
        this.tryCount = count;
    }
    
    public void setHitCount(long count) {
        this.hitCount = count;
    }
}
