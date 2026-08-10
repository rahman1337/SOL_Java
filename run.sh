#!/bin/bash

# Solana Wallet Scanner - Run Script
# Usage: ./run.sh [options]

set -e

echo "🚀 Solana Wallet Scanner"
echo ""

# Check if JAR exists
if [ ! -f "target/wallet-scanner-1.0.0-jar-with-dependencies.jar" ]; then
    echo "❌ JAR not found. Building..."
    mvn clean package -DskipTests
fi

# Check if bip39.txt exists
if [ ! -f "bip39.txt" ]; then
    echo "❌ bip39.txt not found!"
    echo ""
    echo "Download BIP39 word list:"
    echo "curl -s https://raw.githubusercontent.com/trezor/python-mnemonic/master/mnemonic/wordlist/english.txt -o bip39.txt"
    exit 1
fi

echo "✓ Configuration found"
echo "✓ Starting scanner..."
echo ""

# Memory settings
MEMORY="-Xmx4g"

# GC settings
GC_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run the scanner
java $MEMORY $GC_OPTS -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
