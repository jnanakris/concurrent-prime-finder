// PrimeFinder.java
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PrimeFinder {
    
    // Check if a number is prime
    public static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        
        int i = 5;
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
            i += 6;
        }
        return true;
    }
    
    // Find primes in a range
    public static List<Integer> findPrimesInRange(int start, int end) {
        List<Integer> primes = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                primes.add(i);
            }
        }
        return primes;
    }
    
    // Sequential implementation
    public static Result findPrimesSequential(int start, int end) {
        long startTime = System.nanoTime();
        List<Integer> primes = findPrimesInRange(start, end);
        long duration = System.nanoTime() - startTime;
        
        return new Result(start, end, primes.size(), duration / 1_000_000_000.0, 1, primes);
    }
    
    // Thread pool implementation
    public static Result findPrimesThreadPool(int start, int end, int numThreads) throws InterruptedException, ExecutionException {
        long startTime = System.nanoTime();
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<List<Integer>>> futures = new ArrayList<>();
        
        int chunkSize = (end - start + 1) / numThreads;
        if (chunkSize < 1) chunkSize = 1;
        
        // Submit tasks
        for (int i = start; i <= end; i += chunkSize) {
            final int rangeStart = i;
            final int rangeEnd = Math.min(i + chunkSize - 1, end);
            
            Future<List<Integer>> future = executor.submit(() -> findPrimesInRange(rangeStart, rangeEnd));
            futures.add(future);
        }
        
        // Collect results
        List<Integer> allPrimes = new ArrayList<>();
        for (Future<List<Integer>> future : futures) {
            allPrimes.addAll(future.get());
        }
        
        executor.shutdown();
        long duration = System.nanoTime() - startTime;
        
        Collections.sort(allPrimes);
        return new Result(start, end, allPrimes.size(), duration / 1_000_000_000.0, numThreads, allPrimes);
    }
    
    // CompletableFuture implementation
    public static Result findPrimesCompletableFuture(int start, int end, int numThreads) {
        long startTime = System.nanoTime();
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        int chunkSize = (end - start + 1) / numThreads;
        if (chunkSize < 1) chunkSize = 1;
        
        List<CompletableFuture<List<Integer>>> futures = new ArrayList<>();
        
        // Create CompletableFutures
        for (int i = start; i <= end; i += chunkSize) {
            final int rangeStart = i;
            final int rangeEnd = Math.min(i + chunkSize - 1, end);
            
            CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(
                () -> findPrimesInRange(rangeStart, rangeEnd),
                executor
            );
            futures.add(future);
        }
        
        // Combine all futures
        CompletableFuture<List<Integer>> combinedFuture = CompletableFuture
            .allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted()
                .collect(Collectors.toList())
            );
        
        List<Integer> allPrimes = combinedFuture.join();
        executor.shutdown();
        
        long duration = System.nanoTime() - startTime;
        return new Result(start, end, allPrimes.size(), duration / 1_000_000_000.0, numThreads, allPrimes);
    }
    
    // Parallel Stream implementation
    public static Result findPrimesParallelStream(int start, int end) {
        long startTime = System.nanoTime();
        
        List<Integer> primes = IntStream.rangeClosed(start, end)
            .parallel()
            .filter(PrimeFinder::isPrime)
            .boxed()
            .collect(Collectors.toList());
        
        long duration = System.nanoTime() - startTime;
        Collections.sort(primes);
        
        return new Result(start, end, primes.size(), duration / 1_000_000_000.0, 
                         Runtime.getRuntime().availableProcessors(), primes);
    }
    
    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        int start = 1;
        int end = 100000;
        int workers = Runtime.getRuntime().availableProcessors();
        String method = "all";
        boolean savePrimes = false;
        String outputFile = "java_results.json";
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--start":
                    start = Integer.parseInt(args[++i]);
                    break;
                case "--end":
                    end = Integer.parseInt(args[++i]);
                    break;
                case "--workers":
                    workers = Integer.parseInt(args[++i]);
                    break;
                case "--method":
                    method = args[++i];
                    break;
                case "--save-primes":
                    savePrimes = true;
                    break;
                case "--output":
                    outputFile = args[++i];
                    break;
            }
        }
        
        System.out.println("Finding primes from " + start + " to " + end);
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        
        Map<String, Object> results = new HashMap<>();
        results.put("configuration", Map.of(
            "start_range", start,
            "end_range", end,
            "cpu_count", Runtime.getRuntime().availableProcessors()
        ));
        
        List<Integer> primes = null;
        
        if (method.equals("all") || method.equals("sequential")) {
            System.out.println("\nRunning sequential version...");
            Result seqResult = findPrimesSequential(start, end);
            if (!savePrimes) seqResult.primes = null;
            results.put("sequential", seqResult.toMap());
            primes = seqResult.primes;
            System.out.println("Sequential: " + seqResult.primesFound + " primes in " + 
                             String.format("%.4f", seqResult.executionTime) + " seconds");
        }
        
        if (method.equals("all") || method.equals("threadpool")) {
            System.out.println("\nRunning thread pool version with " + workers + " threads...");
            Result threadResult = findPrimesThreadPool(start, end, workers);
            if (!savePrimes) threadResult.primes = null;
            results.put("threadpool", threadResult.toMap());
            if (primes == null) primes = threadResult.primes;
            System.out.println("Thread Pool: " + threadResult.primesFound + " primes in " + 
                             String.format("%.4f", threadResult.executionTime) + " seconds");
        }
        
        if (method.equals("all") || method.equals("completable")) {
            System.out.println("\nRunning CompletableFuture version with " + workers + " threads...");
            Result cfResult = findPrimesCompletableFuture(start, end, workers);
            if (!savePrimes) cfResult.primes = null;
            results.put("completablefuture", cfResult.toMap());
            if (primes == null) primes = cfResult.primes;
            System.out.println("CompletableFuture: " + cfResult.primesFound + " primes in " + 
                             String.format("%.4f", cfResult.executionTime) + " seconds");
        }
        
        if (method.equals("all") || method.equals("parallel")) {
            System.out.println("\nRunning parallel stream version...");
            Result parallelResult = findPrimesParallelStream(start, end);
            if (!savePrimes) parallelResult.primes = null;
            results.put("parallelstream", parallelResult.toMap());
            if (primes == null) primes = parallelResult.primes;
            System.out.println("Parallel Stream: " + parallelResult.primesFound + " primes in " + 
                             String.format("%.4f", parallelResult.executionTime) + " seconds");
        }
        
        if (savePrimes && primes != null) {
            results.put("primes", primes);
        }
        
        // Save results to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(results, writer);
        }
        
        System.out.println("\nResults saved to " + outputFile);
    }
    
    // Result class
    static class Result {
        int startRange;
        int endRange;
        int primesFound;
        double executionTime;
        int workers;
        List<Integer> primes;
        
        Result(int startRange, int endRange, int primesFound, double executionTime, 
               int workers, List<Integer> primes) {
            this.startRange = startRange;
            this.endRange = endRange;
            this.primesFound = primesFound;
            this.executionTime = executionTime;
            this.workers = workers;
            this.primes = primes;
        }
        
        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("primes_found", primesFound);
            map.put("execution_time", executionTime);
            map.put("workers", workers);
            return map;
        }
    }
}

// PrimeFinderTest.java
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class PrimeFinderTest {
    
    @Test
    public void testIsPrime() {
        assertFalse(PrimeFinder.isPrime(1));
        assertTrue(PrimeFinder.isPrime(2));
        assertTrue(PrimeFinder.isPrime(3));
        assertFalse(PrimeFinder.isPrime(4));
        assertTrue(PrimeFinder.isPrime(5));
        assertFalse(PrimeFinder.isPrime(100));
        assertTrue(PrimeFinder.isPrime(101));
    }
    
    @Test
    public void testFindPrimesInRange() {
        List<Integer> primes = PrimeFinder.findPrimesInRange(1, 10);
        assertEquals(4, primes.size());
        assertTrue(primes.contains(2));
        assertTrue(primes.contains(3));
        assertTrue(primes.contains(5));
        assertTrue(primes.contains(7));
    }
    
    @Test
    public void testConcurrentResults() throws Exception {
        // Test that all methods find the same primes
        PrimeFinder.Result sequential = PrimeFinder.findPrimesSequential(1, 1000);
        PrimeFinder.Result threadPool = PrimeFinder.findPrimesThreadPool(1, 1000, 4);
        PrimeFinder.Result completable = PrimeFinder.findPrimesCompletableFuture(1, 1000, 4);
        
        assertEquals(sequential.primesFound, threadPool.primesFound);
        assertEquals(sequential.primesFound, completable.primesFound);
    }
}