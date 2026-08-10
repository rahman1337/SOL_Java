package com.solana.scanner;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SolanaWalletScanner {
    
    private static final String RPC_URL = "https://solana-rpc.publicnode.com";
    private static final String BIP39_FILE = "bip39.txt";
    private static final String HITS_FILE = "hits.txt";
    private static final double MIN_BALANCE = 0.0001;
    
    private static AtomicLong tryCount = new AtomicLong(0);
    private static AtomicLong hitCount = new AtomicLong(0);
    private static AtomicLong errorCount = new AtomicLong(0);
    private static List<String> bip39Words = new ArrayList<>();
    private static Random random = new Random();
    private static ExecutorService executor;
    private static ScheduledExecutorService scheduler;
    private static volatile boolean running = true;
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Solana Wallet Scanner Starting...");
            System.out.println();
            
            // Load BIP39 words
            if (!loadBIP39Words()) {
                System.err.println("❌ Failed to load BIP39 words");
                System.exit(1);
            }
            System.out.println("✓ Loaded " + bip39Words.size() + " words from " + BIP39_FILE);
            
            // Create hits file if it doesn't exist
            File hitsFile = new File(HITS_FILE);
            if (!hitsFile.exists()) {
                hitsFile.createNewFile();
            }
            
            // Setup thread pool
            int coreCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
            executor = Executors.newFixedThreadPool(coreCount);
            scheduler = Executors.newScheduledThreadPool(1);
            System.out.println("✓ Using " + coreCount + " worker threads");
            
            // Start dashboard updater
            DashboardUpdater dashboard = new DashboardUpdater(tryCount.get(), hitCount.get());
            scheduler.scheduleAtFixedRate(() -> {
                dashboard.setTryCount(tryCount.get());
                dashboard.setHitCount(hitCount.get());
                dashboard.run();
            }, 0, 100, TimeUnit.MILLISECONDS);
            
            System.out.println("✓ Starting scan...");
            System.out.println();
            
            // Submit scanning tasks
            for (int i = 0; i < coreCount * 10; i++) {
                executor.submit(SolanaWalletScanner::scanTask);
            }
            
            // Wait for termination
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            
        } catch (Exception e) {
            System.err.println("❌ Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static boolean loadBIP39Words() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(BIP39_FILE));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    bip39Words.add(line);
                }
            }
            reader.close();
            return bip39Words.size() == 2048;
        } catch (FileNotFoundException e) {
            System.err.println("❌ BIP39 file not found: " + BIP39_FILE);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error loading BIP39 words: " + e.getMessage());
            return false;
        }
    }
    
    private static void scanTask() {
        while (running) {
            try {
                // Generate random 12-word mnemonic
                String mnemonic = generateMnemonic();
                tryCount.incrementAndGet();
                
                // Validate mnemonic
                if (!BIP39Validator.isValid(mnemonic)) {
                    errorCount.incrementAndGet();
                    logError("Invalid mnemonic generated: " + mnemonic);
                    continue;
                }
                
                // Derive Solana address
                String address;
                try {
                    address = SolanaAddressDeriver.deriveAddress(mnemonic, 0);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    logError("Failed to derive address for mnemonic " + mnemonic + ": " + e.getMessage());
                    continue;
                }
                
                // Check balance
                double balance;
                try {
                    balance = BalanceChecker.getBalance(address, RPC_URL);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    logError("Failed to check balance for " + address + ": " + e.getMessage());
                    continue;
                }
                
                // Record if balance meets threshold
                if (balance >= MIN_BALANCE) {
                    recordHit(mnemonic, address, balance);
                    hitCount.incrementAndGet();
                    System.out.println();
                    System.out.println("🎉 HIT! Mnemonic: " + mnemonic);
                    System.out.println("   Address: " + address);
                    System.out.println("   Balance: " + balance + " SOL");
                    System.out.println();
                }
                
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logError("Unexpected error in scan task: " + e.getMessage());
            }
        }
    }
    
    private static String generateMnemonic() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i > 0) sb.append(" ");
            sb.append(bip39Words.get(random.nextInt(bip39Words.size())));
        }
        return sb.toString();
    }
    
    private static void recordHit(String mnemonic, String address, double balance) {
        try {
            FileWriter writer = new FileWriter(HITS_FILE, true);
            writer.write(mnemonic + "\n");
            writer.write(address + "\n");
            writer.write(balance + "\n");
            writer.write("\n");
            writer.close();
            logInfo("Hit recorded: " + address + " with " + balance + " SOL");
        } catch (IOException e) {
            logError("Failed to record hit: " + e.getMessage());
        }
    }
    
    private static void logError(String message) {
        System.err.println("[ERROR] " + message);
        try {
            FileWriter logWriter = new FileWriter("errors.log", true);
            logWriter.write("[" + new java.util.Date() + "] " + message + "\n");
            logWriter.close();
        } catch (IOException e) {
            // Silently fail
        }
    }
    
    private static void logInfo(String message) {
        System.out.println("[INFO] " + message);
        try {
            FileWriter logWriter = new FileWriter("info.log", true);
            logWriter.write("[" + new java.util.Date() + "] " + message + "\n");
            logWriter.close();
        } catch (IOException e) {
            // Silently fail
        }
    }
}
