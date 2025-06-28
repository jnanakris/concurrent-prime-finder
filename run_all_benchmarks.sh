#!/bin/bash
# run_all_benchmarks.sh

echo "Running Prime Finder Benchmarks"
echo "==============================="

# Create results directory
mkdir -p results

# Run Go benchmarks
echo -e "\n[GO IMPLEMENTATION]"
cd go
go build -o prime_finder
./prime_finder -start 1 -end 1000000 -workers 8 -output ../results/go_results.json
echo "Go benchmarks:"
go test -bench=. -benchtime=10s
cd ..

# Run Python benchmarks
echo -e "\n[PYTHON IMPLEMENTATION]"
cd python
python prime_finder.py --start 1 --end 1000000 --workers 8 --output ../results/python_results.json
echo "Python tests:"
python -m pytest test_prime_finder.py -v
cd ..

# Run Java benchmarks
echo -e "\n[JAVA IMPLEMENTATION]"
cd java
javac -cp ".:gson-2.10.1.jar" PrimeFinder.java
java -cp ".:gson-2.10.1.jar" PrimeFinder --start 1 --end 1000000 --workers 8 --output ../results/java_results.json
echo "Java tests:"
javac -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" PrimeFinderTest.java
java -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" org.junit.runner.JUnitCore PrimeFinderTest
cd ..

echo -e "\nAll benchmarks complete! Results saved in results/"

---
# setup.sh
#!/bin/bash

echo "Setting up Concurrent Prime Finder Project"
echo "=========================================="

# Create directory structure
mkdir -p go python java results

# Go setup
echo "Setting up Go project..."
cd go
go mod init prime-finder
cat > go.mod << EOF
module prime-finder

go 1.22
EOF
cd ..

# Python setup
echo "Setting up Python project..."
cd python
cat > requirements.txt << EOF
pytest==7.4.3
pytest-benchmark==4.0.0
matplotlib==3.8.2
numpy==1.26.2
EOF

python -m venv venv
source venv/bin/activate 2>/dev/null || venv\Scripts\activate
pip install -r requirements.txt
cd ..

# Java setup
echo "Setting up Java project..."
cd java
# Download required JARs
echo "Downloading Java dependencies..."
curl -L -o gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
curl -L -o junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
curl -L -o hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
cd ..

echo "Setup complete!"

---
# README.md
# Concurrent Prime Number Finder

A comparative study of concurrent programming in Go, Python, and Java through implementing a prime number finder.

## Project Structure

```
concurrent-prime-finder/
├── go/                 # Go implementation using goroutines
├── python/             # Python with threading and multiprocessing
├── java/               # Java with thread pools and CompletableFuture
├── results/            # Benchmark results
├── setup.sh            # Setup script
├── run_all_benchmarks.sh # Run all implementations
└── README.md
```

## Quick Start

1. Clone the repository:
```bash
git clone https://github.com/[your-username]/concurrent-prime-finder
cd concurrent-prime-finder
```

2. Run setup:
```bash
chmod +x setup.sh
./setup.sh
```

3. Copy the implementation files to their respective directories:
- `go/main.go` and `go/benchmark_test.go`
- `python/prime_finder.py` and `python/test_prime_finder.py`
- `java/PrimeFinder.java` and `java/PrimeFinderTest.java`

4. Run benchmarks:
```bash
chmod +x run_all_benchmarks.sh
./run_all_benchmarks.sh
```

## Running Individual Implementations

### Go
```bash
cd go
go run main.go -start 1 -end 100000 -workers 8
go test -bench=.
```

### Python
```bash
cd python
python prime_finder.py --start 1 --end 100000 --workers 8 --method all
python -m pytest test_prime_finder.py
```

### Java
```bash
cd java
javac -cp ".:gson-2.10.1.jar" PrimeFinder.java
java -cp ".:gson-2.10.1.jar" PrimeFinder --start 1 --end 100000 --workers 8
```

## Command Line Options

All implementations support:
- `--start`: Starting number (default: 1)
- `--end`: Ending number (default: 100000)
- `--workers`: Number of concurrent workers (default: CPU count)
- `--output`: Output JSON file
- `--save-primes`: Include actual prime numbers in output

## Results

Results are saved as JSON files in the `results/` directory containing:
- Number of primes found
- Execution time
- Worker configuration
- Optional: actual prime numbers

## Performance Highlights

- **Go**: Best balance of performance and simplicity
- **Python**: Multiprocessing required for CPU-bound parallelism
- **Java**: Highest raw performance with parallel streams

See the full report for detailed analysis.