# Solana Wallet Scanner - Configuration Guide

## Main Configuration

Edit `SolanaWalletScanner.java` to customize:

### RPC Endpoint
```java
private static final String RPC_URL = "https://solana-rpc.publicnode.com";
```

**Alternative RPC Endpoints:**
- Mainnet: `https://api.mainnet-beta.solana.com`
- Devnet: `https://api.devnet.solana.com`
- Local: `http://localhost:8899`

**Public RPC Options:**
- `https://solana-api.projectserum.com`
- `https://rpc.ankr.com/solana`
- `https://api.blockos.io`

### Mnemonic Settings
```java
private static final String BIP39_FILE = "bip39.txt";
private static final int MNEMONIC_WORDS = 12;
```

### Output File
```java
private static final String HITS_FILE = "hits.txt";
```

### Minimum Balance Threshold
Edit `SolanaWalletScanner.java` scanTask method:
```java
if (balance >= 0.0001) {  // Change 0.0001 to your threshold
    recordHit(mnemonic, address, balance);
    hitCount.incrementAndGet();
}
```

### Thread Configuration
Automatic (cores - 1):
```java
int coreCount = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
```

Manual override:
```java
int coreCount = 8;  // Fixed number of threads
```

### Task Queue Depth
Edit executor submission loop:
```java
for (int i = 0; i < coreCount * 10; i++) {  // 10x multiplier
    executor.submit(SolanaWalletScanner::scanTask);
}
```

## Derivation Path Configuration

Edit `SolanaAddressDeriver.java`:

```java
private static final String DERIVATION_PATH = "m/44'/501'/0'/0";
```

### Common Paths:
- Solana: `m/44'/501'/0'/0`
- Bitcoin: `m/44'/0'/0'/0`
- Ethereum: `m/44'/60'/0'/0`
- Cardano: `m/44'/1852'/0'/0`

## Performance Tuning

### JVM Memory Settings
```bash
# Small (2GB heap)
java -Xms512m -Xmx2g -jar wallet-scanner.jar

# Medium (4GB heap)
java -Xms1g -Xmx4g -jar wallet-scanner.jar

# Large (8GB heap)
java -Xms2g -Xmx8g -jar wallet-scanner.jar
```

### Garbage Collection
```bash
# G1GC (recommended for most systems)
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xmx4g -jar wallet-scanner.jar

# Parallel GC (good for throughput)
java -XX:+UseParallelGC -Xmx4g -jar wallet-scanner.jar

# ZGC (low latency, experimental)
java -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -Xmx4g -jar wallet-scanner.jar
```

### Thread Pool Settings
Edit `SolanaWalletScanner.java`:

```java
// Increase task submission multiplier
for (int i = 0; i < coreCount * 20; i++) {  // 20x instead of 10x
    executor.submit(SolanaWalletScanner::scanTask);
}
```

## Network Configuration

### RPC Timeout Settings
Edit `BalanceChecker.java`:

```java
private static final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)  // Connection timeout
        .readTimeout(10, TimeUnit.SECONDS)     // Read timeout
        .writeTimeout(10, TimeUnit.SECONDS)    // Write timeout
        .retryOnConnectionFailure(true)        // Auto-retry
        .build();
```

### Rate Limiting
Add delay between RPC calls:
```java
// In BalanceChecker.getBalance()
Thread.sleep(50);  // 50ms delay between requests
```

## Logging Configuration

### Enable Debug Logging
Create `simplelogger.properties` in `src/main/resources/`:

```properties
# Log level: TRACE, DEBUG, INFO, WARN, ERROR
org.slf4j.simpleLogger.defaultLogLevel=info

# Package-specific logging
org.slf4j.simpleLogger.log.com.solana.scanner=debug

# Console output
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss

# Log file (optional)
# org.slf4j.simpleLogger.logFile=scan.log
```

## Dashboard Configuration

Edit `DashboardUpdater.java`:

```java
// Update frequency (milliseconds)
if (timeDiff >= 100) {  // Update every 100ms
    updateDashboard();
}

// Format numbers
private String formatNumber(long num) {
    // Customize number formatting here
}
```

## Security Considerations

### File Permissions
```bash
# Protect sensitive files
chmod 600 hits.txt      # Owner read/write only
chmod 600 bip39.txt     # Owner read/write only
```

### RPC Endpoint Security
- Use HTTPS endpoints only
- Consider local node for sensitive operations
- Avoid exposing real mnemonics in logs

## Environment Variables

You can extend the code to use environment variables:

```java
// Add to SolanaWalletScanner.java
private static final String RPC_URL = 
    System.getenv("SOL_RPC_URL") != null 
        ? System.getenv("SOL_RPC_URL") 
        : "https://solana-rpc.publicnode.com";
```

Usage:
```bash
export SOL_RPC_URL="https://api.mainnet-beta.solana.com"
java -jar wallet-scanner.jar
```

## Configuration Profiles

### Quick Scan (Low Resource)
```java
private static final int MIN_BALANCE = 1.0;  // Only 1+ SOL
int coreCount = 2;  // 2 threads
```

### Balanced (Medium Resource)
```java
private static final double MIN_BALANCE = 0.0001;
int coreCount = Runtime.getRuntime().availableProcessors() - 1;
```

### Aggressive (High Resource)
```java
private static final double MIN_BALANCE = 0.00001;  // Any dust
int coreCount = Runtime.getRuntime().availableProcessors();
for (int i = 0; i < coreCount * 50; i++)  // 50x queue depth
```

## Troubleshooting Configuration

### Too Slow
1. Increase thread count
2. Use faster RPC endpoint
3. Increase JVM heap: `-Xmx8g`
4. Switch to faster GC: `-XX:+UseG1GC`

### High Memory Usage
1. Reduce thread count
2. Lower heap size: `-Xmx2g`
3. Increase GC frequency: `-XX:MaxGCPauseMillis=50`

### RPC Errors
1. Check internet connection
2. Try different RPC endpoint
3. Reduce request rate (increase sleep)
4. Run local Solana node

## Advanced Configuration

See BUILD.md for JVM optimization flags.
