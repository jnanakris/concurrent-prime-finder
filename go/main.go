// main.go
package main

import (
    "encoding/json"
    "flag"
    "fmt"
    "os"
    "runtime"
    "sync"
    "time"
)

type Result struct {
    StartRange   int           `json:"start_range"`
    EndRange     int           `json:"end_range"`
    PrimesFound  int           `json:"primes_found"`
    ExecutionTime float64      `json:"execution_time_seconds"`
    Workers      int           `json:"workers"`
    Primes       []int         `json:"primes,omitempty"`
}

// isPrime checks if a number is prime using trial division
func isPrime(n int) bool {
    if n <= 1 {
        return false
    }
    if n <= 3 {
        return true
    }
    if n%2 == 0 || n%3 == 0 {
        return false
    }
    
    i := 5
    for i*i <= n {
        if n%i == 0 || n%(i+2) == 0 {
            return false
        }
        i += 6
    }
    return true
}

// findPrimesInRange finds all primes in a given range
func findPrimesInRange(start, end int) []int {
    var primes []int
    for i := start; i <= end; i++ {
        if isPrime(i) {
            primes = append(primes, i)
        }
    }
    return primes
}

// worker processes chunks of ranges
func worker(id int, jobs <-chan [2]int, results chan<- []int, wg *sync.WaitGroup) {
    defer wg.Done()
    
    for job := range jobs {
        start, end := job[0], job[1]
        primes := findPrimesInRange(start, end)
        results <- primes
    }
}

// findPrimesConcurrent finds primes using concurrent workers
func findPrimesConcurrent(start, end, workers int) ([]int, time.Duration) {
    startTime := time.Now()
    
    chunkSize := (end - start + 1) / workers
    if chunkSize < 1 {
        chunkSize = 1
    }
    
    jobs := make(chan [2]int, workers)
    results := make(chan []int, workers)
    
    var wg sync.WaitGroup
    
    // Start workers
    for i := 0; i < workers; i++ {
        wg.Add(1)
        go worker(i, jobs, results, &wg)
    }
    
    // Send jobs
    go func() {
        for i := start; i <= end; i += chunkSize {
            jobEnd := i + chunkSize - 1
            if jobEnd > end {
                jobEnd = end
            }
            jobs <- [2]int{i, jobEnd}
        }
        close(jobs)
    }()
    
    // Wait for workers to complete
    go func() {
        wg.Wait()
        close(results)
    }()
    
    // Collect results
    var allPrimes []int
    for primes := range results {
        allPrimes = append(allPrimes, primes...)
    }
    
    return allPrimes, time.Since(startTime)
}

// findPrimesSequential finds primes sequentially for comparison
func findPrimesSequential(start, end int) ([]int, time.Duration) {
    startTime := time.Now()
    primes := findPrimesInRange(start, end)
    return primes, time.Since(startTime)
}

func main() {
    var (
        start      = flag.Int("start", 1, "Start of range")
        end        = flag.Int("end", 100000, "End of range")
        workers    = flag.Int("workers", runtime.NumCPU(), "Number of workers")
        sequential = flag.Bool("sequential", false, "Run sequential version")
        savePrimes = flag.Bool("save-primes", false, "Save actual prime numbers")
        output     = flag.String("output", "results.json", "Output file")
    )
    
    flag.Parse()
    
    fmt.Printf("Finding primes from %d to %d\n", *start, *end)
    
    var primes []int
    var duration time.Duration
    
    if *sequential {
        fmt.Println("Running sequential version...")
        primes, duration = findPrimesSequential(*start, *end)
    } else {
        fmt.Printf("Running concurrent version with %d workers...\n", *workers)
        primes, duration = findPrimesConcurrent(*start, *end, *workers)
    }
    
    fmt.Printf("Found %d primes in %v\n", len(primes), duration)
    
    // Prepare result
    result := Result{
        StartRange:    *start,
        EndRange:      *end,
        PrimesFound:   len(primes),
        ExecutionTime: duration.Seconds(),
        Workers:       *workers,
    }
    
    if *savePrimes {
        result.Primes = primes
    }
    
    // Save results
    file, err := os.Create(*output)
    if err != nil {
        fmt.Printf("Error creating output file: %v\n", err)
        return
    }
    defer file.Close()
    
    encoder := json.NewEncoder(file)
    encoder.SetIndent("", "  ")
    if err := encoder.Encode(result); err != nil {
        fmt.Printf("Error encoding results: %v\n", err)
        return
    }
    
    fmt.Printf("Results saved to %s\n", *output)
}

// benchmark_test.go
package main

import (
    "runtime"
    "testing"
)

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

func BenchmarkFindPrimesConcurrentCPUWorkers(b *testing.B) {
    workers := runtime.NumCPU()
    for i := 0; i < b.N; i++ {
        findPrimesConcurrent(1, 10000, workers)
    }
}

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
        {100, false},
        {101, true},
        {1000, false},
        {1009, true},
    }
    
    for _, tt := range tests {
        if got := isPrime(tt.n); got != tt.prime {
            t.Errorf("isPrime(%d) = %v, want %v", tt.n, got, tt.prime)
        }
    }
}

func TestFindPrimesInRange(t *testing.T) {
    primes := findPrimesInRange(1, 10)
    expected := []int{2, 3, 5, 7}
    
    if len(primes) != len(expected) {
        t.Errorf("Expected %d primes, got %d", len(expected), len(primes))
    }
    
    for i, p := range primes {
        if p != expected[i] {
            t.Errorf("Expected prime[%d] = %d, got %d", i, expected[i], p)
        }
    }
}