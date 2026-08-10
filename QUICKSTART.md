# Solana Wallet Scanner - Getting Started

## Quick Start Guide

### Prerequisites
- Java 11 or higher installed
- Maven installed
- Git

### Step 1: Clone the Repository
```bash
git clone https://github.com/rahman1337/SOL_Java.git
cd SOL_Java
```

### Step 2: Get BIP39 Word List
Download the official BIP39 word list and save as `bip39.txt`:

```bash
# Linux/Mac
curl -s https://raw.githubusercontent.com/trezor/python-mnemonic/master/mnemonic/wordlist/english.txt -o bip39.txt

# Windows - Use PowerShell
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/trezor/python-mnemonic/master/mnemonic/wordlist/english.txt" -OutFile "bip39.txt"
```

Verify you have 2048 words:
```bash
wc -l bip39.txt  # Should show 2048
```

### Step 3: Build the Project
```bash
mvn clean package
```

### Step 4: Run the Scanner
```bash
java -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

You should see:
```
🚀 Solana Wallet Scanner Starting...
✓ Loaded 2048 words from bip39.txt
✓ Using 7 worker threads
✓ Starting scan...

Tries: 1.23K | Hits: 0
```

### Step 5: Monitor Results
The scanner will continuously update the dashboard. Results are saved to `hits.txt`:

```bash
# Watch results in real-time
tail -f hits.txt
```

## Troubleshooting

### "bip39.txt not found"
- Ensure `bip39.txt` is in the project root directory
- Follow Step 2 above to download it

### "Not enough words in bip39.txt"
- File should contain exactly 2048 words
- Each word on a separate line

### Poor Performance
- Ensure you have enough CPU cores
- Close other applications to free up system resources
- Check internet connection (affects RPC queries)

### Connection Issues
- Verify internet connectivity
- Try a different RPC endpoint by modifying `RPC_URL` in code
- Some public RPC nodes may rate limit requests

## Performance Tips

1. **Maximize CPU Usage**
   - The app automatically uses all cores minus 1
   - For faster results, close other applications

2. **Optimize RPC Calls**
   - Consider running a local Solana node for faster queries
   - Or use a faster RPC endpoint

3. **Monitor Memory**
   - Default JVM memory should be sufficient
   - For very high loads: `java -Xmx4g -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar`

## Output Format (hits.txt)

Each hit is recorded as:
```
<12-word-mnemonic>
<solana-address>
<balance-in-sol>

```

Example:
```
abandon ability able about above absent absorb abstract abusive access account achieve
9B5X7sZGiZoQ3UgFz7xZjYvBnWZWGzKPdZ3WmZoYqU7K
0.0001

```

## Next Steps

- Review `hits.txt` for found wallets
- Implement additional filtering logic if needed
- Consider adding database support for large-scale operations
- Add additional derivation paths beyond `m/44'/501'/0'/0`

## Contributing

Found an issue or improvement? Feel free to contribute!

## Disclaimer

⚠️ This tool is for educational purposes. Ensure you comply with all applicable laws in your jurisdiction.