package com.solana.scanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class SolanaWalletScanner {
    private static final String BIP39_FILE = "bip39.txt";
    private static final String HITS_FILE = "hits.txt";
    private static final int MNEMONIC_WORDS = 12;
    private static final String RPC_URL = "https://solana-rpc.publicnode.com";
    
    private static List<String> wordList;
    private static AtomicLong tryCount = new AtomicLong(0);
    private static AtomicLong hitCount = new AtomicLong(0);
    private static ExecutorService executor;
    private static ScheduledExecutorService dashboardExecutor;
    
    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Solana Wallet Scanner Starting...");
        
        // Load BIP39 word list
        loadWordList();
        System.out.println("✓ Loaded " + wordList.size() + " words from " + BIP39_FILE);
        
        // Initialize file
        initHitsFile();
        
        // Setup thread pool (cores - 1)
        int coreCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        executor = Executors.newFixedThreadPool(coreCount);
        dashboardExecutor = Executors.newScheduledThreadPool(1);
        
        System.out.println("✓ Using " + coreCount + " worker threads");
        System.out.println("✓ Starting scan...\n");
        
        // Start dashboard updater
        startDashboard();
        
        // Submit scanning tasks
        for (int i = 0; i < coreCount * 10; i++) {
            executor.submit(SolanaWalletScanner::scanTask);
        }
        
        // Keep running
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
    
    private static void loadWordList() throws IOException {
        wordList = new ArrayList<>();
        Path path = Paths.get(BIP39_FILE);
        
        if (!Files.exists(path)) {
            throw new FileNotFoundException("bip39.txt not found!");
        }
        
        wordList = Files.readAllLines(path, StandardCharsets.UTF_8);
        
        if (wordList.size() < MNEMONIC_WORDS) {
            throw new IllegalArgumentException("Not enough words in bip39.txt");
        }
    }
    
    private static void initHitsFile() throws IOException {
        Path path = Paths.get(HITS_FILE);
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }
    
    private static void startDashboard() {
        dashboardExecutor.scheduleAtFixedRate(() -> {
            System.out.print("\rTries: " + tryCount.get() + " | Hits: " + hitCount.get());
            System.out.flush();
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
    
    private static void scanTask() {
        Random random = new Random();
        
        while (true) {
            try {
                // Generate random 12-word mnemonic
                String mnemonic = generateRandomMnemonic(random);
                tryCount.incrementAndGet();
                
                // Validate mnemonic
                if (!BIP39Validator.isValid(mnemonic)) {
                    continue;
                }
                
                // Derive first address
                String address = SolanaAddressDeriver.deriveAddress(mnemonic, 0);
                
                // Check balance
                double balance = BalanceChecker.getBalance(address, RPC_URL);
                
                if (balance >= 0.0001) {
                    recordHit(mnemonic, address, balance);
                    hitCount.incrementAndGet();
                }
                
            } catch (Exception e) {
                // Continue on error
            }
        }
    }
    
    private static String generateRandomMnemonic(Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MNEMONIC_WORDS; i++) {
            if (i > 0) sb.append(" ");
            sb.append(wordList.get(random.nextInt(wordList.size())));
        }
        return sb.toString();
    }
    
    private static synchronized void recordHit(String mnemonic, String address, double balance) {
        try {
            Path path = Paths.get(HITS_FILE);
            String entry = mnemonic + "\n" + address + "\n" + balance + "\n\n";
            Files.write(path, entry.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
