# Concurrent Prime Number Finder

A comparative study of concurrent programming models in Go, Python, and Java through implementing a prime number finder.

**Author:** Jnana Krishnamsetty  
**Course:** ITCS 4102/5102 - Programming Languages  
**Repository:** https://github.com/jnanakris/concurrent-prime-finder

## Overview

This project implements a concurrent prime number finder in three different programming languages to compare their concurrency models, performance characteristics, and developer experience. The implementation uses:

- **Go**: Goroutines and channels
- **Python**: Threading vs Multiprocessing
- **Java**: Thread pools, CompletableFuture, and Parallel Streams

## Project Structure

```
concurrent-prime-finder/
├── README.md                    # This file
├── go/                         # Go implementation
│   ├── main.go                 # Main program with concurrent prime finder
│   ├── benchmark_test.go       # Benchmark tests
│   └── go.mod                  # Go module file
├── python/                     # Python implementation
│   ├── prime_finder.py         # Main program with threading/multiprocessing
│   ├── test_prime_finder.py    # Unit tests
│   ├── visualize_results.py    # Results visualization script
│   └── requirements.txt        # Python dependencies
├── java/                       # Java implementation
│   ├── PrimeFinder.java        # Main program with multiple concurrency approaches
│   ├── PrimeFinderTest.java    # JUnit tests
│   ├── gson-2.10.1.jar         # JSON library
│   └── junit-4.13.2.jar        # Testing library
├── results/                    # Benchmark results (generated)
│   ├── go_results.json
│   ├── python_results.json
│   ├── java_results.json
│   └── performance_comparison.png
├── docs/                       # Documentation
│   ├── project_proposal.pdf    # Project proposal
│   └── final_report.pdf        # Final analysis report
├── setup.sh                    # Setup script for dependencies
└── run_all_benchmarks.sh       # Run all implementations

```

## Prerequisites

- **Go** 1.22 or higher
- **Python** 3.12 or higher
- **Java** 21 or higher
- **Git** for version control
- **Bash** shell (for scripts)

## Quick Start

1. **Clone the repository:**
```bash
git clone https://github.com/jnanakris/concurrent-prime-finder
cd concurrent-prime-finder
```

2. **Run the setup script:**
```bash
chmod +x setup.sh
./setup.sh
```

3. **Run all benchmarks:**
```bash
chmod +x run_all_benchmarks.sh
./run_all_benchmarks.sh
```

## Running Individual Implementations

### Go Implementation

```bash
cd go

# Run with default settings
go run main.go

# Run with custom parameters
go run main.go -start 1 -end 1000000 -workers 8 -output results.json

# Run benchmarks
go test -bench=.

# Run tests
go test -v
```

### Python Implementation

```bash
cd python

# Activate virtual environment (if created by setup script)
source venv/bin/activate  # On Windows: venv\Scripts\activate

# Run all methods comparison
python prime_finder.py --start 1 --end 1000000 --workers 8 --method all

# Run specific method
python prime_finder.py --method multiprocessing --workers 4

# Run tests
python -m pytest test_prime_finder.py -v

# Generate visualizations
python visualize_results.py
```

### Java Implementation

```bash
cd java

# Compile
javac -cp ".:gson-2.10.1.jar" PrimeFinder.java

# Run with default settings
java -cp ".:gson-2.10.1.jar" PrimeFinder

# Run with custom parameters
java -cp ".:gson-2.10.1.jar" PrimeFinder --start 1 --end 1000000 --workers 8 --method all

# Run tests
javac -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" PrimeFinderTest.java
java -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" org.junit.runner.JUnitCore PrimeFinderTest
```

## Command Line Options

All implementations support similar command-line arguments:

| Option | Description | Default |
|--------|-------------|---------|
| `--start` | Starting number of range | 1 |
| `--end` | Ending number of range | 100000 |
| `--workers` | Number of concurrent workers | CPU count |
| `--output` | Output JSON file path | results.json |
| `--save-primes` | Include prime numbers in output | false |

Python additional options:
- `--method`: Choose `sequential`, `threading`, `multiprocessing`, or `all`

Java additional options:
- `--method`: Choose `sequential`, `threadpool`, `completable`, `parallel`, or `all`

## Performance Results Summary

Based on finding primes from 1 to 1,000,000 on an 8-core machine:

| Implementation | Time (8 workers) | Speedup | Memory Usage |
|----------------|------------------|---------|--------------|
| Go Concurrent | 1.8s | 6.8x | 15 MB |
| Python Multiprocessing | 2.1s | 6.2x | 380 MB |
| Python Threading | 12.7s | 1.03x | 45 MB |
| Java Parallel Stream | 1.6s | 7.4x | 145 MB |
| Java ThreadPool | 1.7s | 6.9x | 145 MB |

## Key Findings

1. **Go** provides the best balance of performance and resource efficiency
2. **Python** threading is ineffective for CPU-bound tasks due to the GIL
3. **Java** achieves the highest raw performance but with higher memory usage
4. All languages achieve near-linear speedup with multiprocessing/true parallelism

## Building and Testing

### Running All Tests

```bash
# Go tests
cd go && go test -v

# Python tests  
cd python && python -m pytest -v

# Java tests
cd java && java -cp ".:junit-4.13.2.jar:hamcrest-core-1.3.jar" org.junit.runner.JUnitCore PrimeFinderTest
```

### Building Executables

```bash
# Build Go binary
cd go && go build -o prime_finder

# Create Python executable (using PyInstaller)
cd python && pip install pyinstaller
pyinstaller --onefile prime_finder.py

# Compile Java to JAR
cd java && javac -cp ".:gson-2.10.1.jar" PrimeFinder.java
jar cfm PrimeFinder.jar Manifest.txt *.class
```

## Visualization

To generate performance comparison charts:

```bash
cd python
python visualize_results.py
```

This creates `performance_comparison.png` in the results directory.

## Documentation

- **Project Proposal**: `docs/project_proposal.pdf` - Initial project plan and objectives
- **Final Report**: `docs/final_report.pdf` - Detailed analysis and conclusions
- **Code Documentation**: Inline comments in source files

## Troubleshooting

### Common Issues

1. **Go module errors**: Run `go mod init prime-finder` in the go directory
2. **Python import errors**: Ensure virtual environment is activated
3. **Java classpath errors**: Use proper path separator (`:` on Unix, `;` on Windows)
4. **Permission denied**: Run `chmod +x` on shell scripts

### Platform-Specific Notes

- **Windows**: Use `\` instead of `/` for paths, `;` for Java classpath
- **macOS**: May need to install command line tools: `xcode-select --install`
- **Linux**: Ensure you have build-essential package installed

## Contributing

This is a course project, but if you'd like to experiment:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is for educational purposes as part of ITCS 4102/5102 coursework.

## Acknowledgments

- Course instructor for project guidelines
- "The Go Programming Language" by Donovan & Kernighan
- "Java Concurrency in Practice" by Goetz et al.
- David Beazley's talks on Python concurrency

## Contact

Jnana Krishnamsetty - [GitHub Profile](https://github.com/jnanakris)