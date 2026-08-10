# Solana Wallet Scanner

A high-performance Java application for scanning Solana wallets using BIP39 mnemonics.

## Features

- **BIP39 Mnemonic Generation**: Randomly generates 12-word mnemonics from a 2048-word list
- **Validation**: Validates each mnemonic using BIP39 standard
- **Address Derivation**: Derives Solana addresses using HD wallet path `m/44'/501'/0'/0`
- **Balance Checking**: Queries Solana RPC endpoint for wallet balances
- **Hit Recording**: Automatically records wallets with non-zero balances (≥ 0.0001 SOL) to `hits.txt`
- **Multi-threaded**: Uses all available CPU cores minus one for maximum performance
- **Live Dashboard**: Real-time terminal dashboard showing tries and hits count

## Requirements

- Java 11+
- Maven 3.6+
- Internet connection for Solana RPC queries
- `bip39.txt` file with 2048 words (one per line)

## Setup

1. **Install Dependencies**
   ```bash
   mvn clean install
   ```

2. **Prepare BIP39 Word List**
   - Create `bip39.txt` in the project root
   - Add 2048 BIP39 words, one per line
   - Download from: https://github.com/trezor/python-mnemonic/blob/master/vectors.json

3. **Build**
   ```bash
   mvn clean package
   ```

4. **Run**
   ```bash
   java -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
   ```

## Output

### Console Dashboard
```
Tries: 45.23K | Hits: 2
```

### hits.txt Format
```
word1 word2 word3 word4 word5 word6 word7 word8 word9 word10 word11 word12
SolanaAddressInBase58Format
0.0001
```

## Configuration

- **RPC Endpoint**: `https://solana-rpc.publicnode.com` (configurable in code)
- **Minimum Balance**: 0.0001 SOL (configurable in code)
- **Worker Threads**: CPU cores - 1 (automatic)
- **Mnemonic Length**: 12 words (BIP39 standard)

## Performance

- **Single Core**: ~500-1000 mnemonics/second
- **Multi-core**: Scales linearly with available cores
- **Memory**: ~100MB base + minimal per-thread overhead

## Safety & Legal

⚠️ **DISCLAIMER**: This tool is for educational purposes only.
- Scanning blockchain addresses without authorization may violate laws in your jurisdiction
- Use only on testnets or with explicit permission
- The author assumes no responsibility for misuse

## Dependencies

- **bitcoinj**: BIP39 mnemonic handling
- **solana-web3**: Solana blockchain interaction
- **okhttp3**: HTTP client for RPC calls
- **gson**: JSON parsing
- **eddsa**: ED25519 cryptography

## License

MIT License - See LICENSE file for details

## Support

For issues, questions, or improvements, please open an issue on GitHub.