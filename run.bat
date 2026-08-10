@echo off
REM Solana Wallet Scanner - Run Script for Windows
REM Usage: run.bat

echo Solana Wallet Scanner
echo.

REM Check if JAR exists
if not exist "target\wallet-scanner-1.0.0-jar-with-dependencies.jar" (
    echo JAR not found. Building...
    call mvn clean package -DskipTests
)

REM Check if bip39.txt exists
if not exist "bip39.txt" (
    echo bip39.txt not found!
    echo.
    echo Download BIP39 word list:
    echo Invoke-WebRequest -Uri "https://raw.githubusercontent.com/trezor/python-mnemonic/master/mnemonic/wordlist/english.txt" -OutFile "bip39.txt"
    pause
    exit /b 1
)

echo Configuration found
echo Starting scanner...
echo.

REM Run the scanner
java -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar target\wallet-scanner-1.0.0-jar-with-dependencies.jar
pause
