# Build and Compilation Instructions

## Prerequisites
- Java 11 or later (JDK)
- Maven 3.6.0 or later
- Git

## Building from Source

### 1. Clone the Repository
```bash
git clone https://github.com/rahman1337/SOL_Java.git
cd SOL_Java
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Compile the Project
```bash
mvn compile
```

### 4. Run Tests (if available)
```bash
mvn test
```

### 5. Package into JAR
```bash
mvn clean package
```

This creates a fat JAR with all dependencies:
- Location: `target/wallet-scanner-1.0.0-jar-with-dependencies.jar`
- Size: ~5-10 MB

## Running the Application

### Quick Start
```bash
java -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

### With Custom Memory
```bash
# Allocate 4GB of heap memory
java -Xmx4g -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

### With Console Output
```bash
java -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar 2>&1 | tee scan.log
```

## Maven Commands Reference

```bash
# Clean build artifacts
mvn clean

# Compile source code
mvn compile

# Run unit tests
mvn test

# Package as JAR
mvn package

# Install to local repository
mvn install

# Deploy to remote repository
mvn deploy

# Skip tests during build
mvn clean package -DskipTests

# Display dependency tree
mvn dependency:tree

# Check for dependency updates
mvn versions:display-dependency-updates
```

## Troubleshooting Build Issues

### "mvn: command not found"
- Install Maven from: https://maven.apache.org/download.cgi
- Add Maven bin directory to PATH

### "Java version mismatch"
```bash
# Check Java version
java -version

# Should output Java 11 or higher
```

### Build Fails with Dependencies
```bash
# Clear Maven cache and rebuild
rm -rf ~/.m2/repository
mvn clean install
```

### Out of Memory During Build
```bash
# Set Maven memory options
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
mvn clean package
```

## Development Setup

### Using IntelliJ IDEA
1. Open project: File → Open → select SOL_Java folder
2. Maven should auto-configure
3. Build: Build → Build Project
4. Run: Right-click SolanaWalletScanner.java → Run

### Using Eclipse
1. File → Import → Existing Maven Projects
2. Select SOL_Java folder
3. Eclipse will configure automatically
4. Project → Build Project

### Using VS Code
1. Install Extension Pack for Java
2. Open folder in VS Code
3. Maven should auto-load
4. Use Terminal to run: `mvn clean package`

## Performance Optimization

### Optimized Build Command
```bash
mvn clean package -T 4 -DskipTests -Dmaven.compiler.useIncrementalCompilation=true
```

Flags:
- `-T 4`: Use 4 threads for parallel building
- `-DskipTests`: Skip test compilation
- `-Dmaven.compiler.useIncrementalCompilation=true`: Incremental compilation

### Optimized Runtime
```bash
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xmx4g \
  -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

GC Options:
- `-XX:+UseG1GC`: Use G1 garbage collector
- `-XX:MaxGCPauseMillis=200`: Optimize pause times

## Creating Executable Scripts

### Linux/Mac (run.sh)
```bash
#!/bin/bash
cd "$(dirname "$0")"
java -Xmx4g -XX:+UseG1GC \
  -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

### Windows (run.bat)
```batch
@echo off
cd /d "%~dp0"
java -Xmx4g -XX:+UseG1GC ^
  -jar target\wallet-scanner-1.0.0-jar-with-dependencies.jar
```

Make executable:
```bash
chmod +x run.sh
```

## CI/CD Integration

### GitHub Actions Workflow
Create `.github/workflows/build.yml`:
```yaml
name: Build

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Maven
        run: mvn clean package -DskipTests
      - name: Upload artifact
        uses: actions/upload-artifact@v2
        with:
          name: wallet-scanner
          path: target/*.jar
```

## Debugging

### Enable Debug Logging
```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug \
  -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

### Remote Debugging
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  -jar target/wallet-scanner-1.0.0-jar-with-dependencies.jar
```

Connect debugger to localhost:5005 in your IDE.

## Size Optimization

If JAR size is a concern:

```bash
# Remove unused classes
mvn clean package -DskipTests -Pmini

# Compress JAR
zip -q -r wallet-scanner.zip target/*.jar
```

## Next Steps

1. Follow QUICKSTART.md for running
2. Prepare bip39.txt file
3. Monitor hits.txt for results
4. Review README.md for detailed documentation
