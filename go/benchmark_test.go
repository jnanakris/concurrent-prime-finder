// benchmark_test.go
package main

import (
    "runtime"
    "testing"
)

// Benchmarks for different implementations
func BenchmarkFindPrimesSequential(b *testing.B) {
    for i := 0; i < b.N; i++ {
        findPrimesSequential(1, 10000)
    }
}

func BenchmarkFindPrimesConcurrent2Workers(b *testing.B) {
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 10000, 2)
    }
}

func BenchmarkFindPrimesConcurrent4Workers(b *testing.B) {
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 10000, 4)
    }
}

func BenchmarkFindPrimesConcurrent8Workers(b *testing.B) {
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 10000, 8)
    }
}

func BenchmarkFindPrimesConcurrentCPUWorkers(b *testing.B) {
    workers := runtime.NumCPU()
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 10000, workers)
    }
}

// Benchmark for larger ranges
func BenchmarkFindPrimesLargeRangeSequential(b *testing.B) {
    for i := 0; i < b.N; i++ {
        findPrimesSequential(1, 100000)
    }
}

func BenchmarkFindPrimesLargeRangeConcurrent(b *testing.B) {
    workers := runtime.NumCPU()
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 100000, workers)
    }
}

// Unit tests
func TestIsPrime(t *testing.T) {
    tests := []struct {
        n     int
        prime bool
    }{
        {1, false},
        {2, true},
        {3, true},
        {4, false},
        {5, true},
        {6, false},
        {7, true},
        {8, false},
        {9, false},
        {10, false},
        {11, true},
        {100, false},
        {101, true},
        {1000, false},
        {1009, true},
        {7919, true}, // 1000th prime
    }
    
    for _, tt := range tests {
        if got := isPrime(tt.n); got != tt.prime {
            t.Errorf("isPrime(%d) = %v, want %v", tt.n, got, tt.prime)
        }
    }
}

func TestFindPrimesInRange(t *testing.T) {
    tests := []struct {
        start    int
        end      int
        expected []int
    }{
        {1, 10, []int{2, 3, 5, 7}},
        {10, 20, []int{11, 13, 17, 19}},
        {24, 28, []int{}},
        {17, 17, []int{17}},
        {18, 18, []int{}},
    }
    
    for _, tt := range tests {
        primes := findPrimesInRange(tt.start, tt.end)
        if len(primes) != len(tt.expected) {
            t.Errorf("findPrimesInRange(%d, %d) returned %d primes, expected %d",
                tt.start, tt.end, len(primes), len(tt.expected))
            continue
        }
        
        for i, p := range primes {
            if p != tt.expected[i] {
                t.Errorf("findPrimesInRange(%d, %d)[%d] = %d, expected %d",
                    tt.start, tt.end, i, p, tt.expected[i])
            }
        }
    }
}

func TestConcurrentConsistency(t *testing.T) {
    // Test that concurrent version produces same results as sequential
    start, end := 1, 1000
    
    seqPrimes, _ := findPrimesSequential(start, end)
    
    for workers := 1; workers <= 8; workers *= 2 {
        concPrimes, _ := findPrimesConcurrent(start, end, workers)
        
        if len(concPrimes) != len(seqPrimes) {
            t.Errorf("Concurrent with %d workers found %d primes, expected %d",
                workers, len(concPrimes), len(seqPrimes))
        }
        
        // Check that all primes match
        seqMap := make(map[int]bool)
        for _, p := range seqPrimes {
            seqMap[p] = true
        }
        
        for _, p := range concPrimes {
            if !seqMap[p] {
                t.Errorf("Concurrent with %d workers found incorrect prime: %d",
                    workers, p)
            }
        }
    }
}

func TestEmptyRange(t *testing.T) {
    primes := findPrimesInRange(0, 1)
    if len(primes) != 0 {
        t.Errorf("Expected no primes in range [0,1], got %v", primes)
    }
    
    // Test reverse range
    primes = findPrimesInRange(10, 5)
    if len(primes) != 0 {
        t.Errorf("Expected no primes in reverse range, got %v", primes)
    }
}

func TestLargePrimeCount(t *testing.T) {
    // There are 168 primes less than 1000
    primes, _ := findPrimesSequential(1, 1000)
    if len(primes) != 168 {
        t.Errorf("Expected 168 primes under 1000, got %d", len(primes))
    }
    
    // There are 78498 primes less than 1000000
    // Skip this test in short mode as it's slow
    if !testing.Short() {
        primes, _ = findPrimesSequential(1, 1000000)
        if len(primes) != 78498 {
            t.Errorf("Expected 78498 primes under 1000000, got %d", len(primes))
        }
    }
}

func TestWorkerPoolEdgeCases(t *testing.T) {
    // Test with more workers than range size
    primes, _ := findPrimesConcurrent(1, 10, 100)
    expected := []int{2, 3, 5, 7}
    
    if len(primes) != len(expected) {
        t.Errorf("Expected %d primes with many workers, got %d", 
            len(expected), len(primes))
    }
}

// Benchmark the isPrime function itself
func BenchmarkIsPrime(b *testing.B) {
    for i := 0; i < b.N; i++ {
        isPrime(1000003) // A known large prime
    }
}

func BenchmarkIsPrimeNonPrime(b *testing.B) {
    for i := 0; i < b.N; i++ {
        isPrime(1000000) // A non-prime
    }
}

---
