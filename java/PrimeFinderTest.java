import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;

public class PrimeFinderTest {
    
    private static final double DELTA = 0.0001; // For double comparisons
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("Starting PrimeFinder tests...");
    }
    
    @Test
    public void testIsPrime() {
        // Test known primes and non-primes
        assertFalse("1 should not be prime", PrimeFinder.isPrime(1));
        assertTrue("2 should be prime", PrimeFinder.isPrime(2));
        assertTrue("3 should be prime", PrimeFinder.isPrime(3));
        assertFalse("4 should not be prime", PrimeFinder.isPrime(4));
        assertTrue("5 should be prime", PrimeFinder.isPrime(5));
        assertFalse("6 should not be prime", PrimeFinder.isPrime(6));
        assertTrue("7 should be prime", PrimeFinder.isPrime(7));
        assertFalse("8 should not be prime", PrimeFinder.isPrime(8));
        assertFalse("9 should not be prime", PrimeFinder.isPrime(9));
        assertFalse("10 should not be prime", PrimeFinder.isPrime(10));
        assertTrue("11 should be prime", PrimeFinder.isPrime(11));
        assertFalse("100 should not be prime", PrimeFinder.isPrime(100));
        assertTrue("101 should be prime", PrimeFinder.isPrime(101));
        assertFalse("1000 should not be prime", PrimeFinder.isPrime(1000));
        assertTrue("1009 should be prime", PrimeFinder.isPrime(1009));
        assertTrue("7919 should be prime (1000th prime)", PrimeFinder.isPrime(7919));
    }
    
    @Test
    public void testFindPrimesInRange() {
        // Test small range
        List<Integer> primes = PrimeFinder.findPrimesInRange(1, 10);
        List<Integer> expected = Arrays.asList(2, 3, 5, 7);
        assertEquals("Should find correct primes in range 1-10", expected, primes);
        
        // Test range not starting at 1
        primes = PrimeFinder.findPrimesInRange(10, 20);
        expected = Arrays.asList(11, 13, 17, 19);
        assertEquals("Should find correct primes in range 10-20", expected, primes);
        
        // Test range with no primes
        primes = PrimeFinder.findPrimesInRange(24, 28);
        assertTrue("Should find no primes in range 24-28", primes.isEmpty());
        
        // Test single number range (prime)
        primes = PrimeFinder.findPrimesInRange(17, 17);
        expected = Arrays.asList(17);
        assertEquals("Should find single prime", expected, primes);
        
        // Test single number range (non-prime)
        primes = PrimeFinder.findPrimesInRange(18, 18);
        assertTrue("Should find no primes for non-prime single number", primes.isEmpty());
    }
    
    @Test
    public void testEmptyRange() {
        // Test empty range
        List<Integer> primes = PrimeFinder.findPrimesInRange(0, 1);
        assertTrue("Should return empty list for range 0-1", primes.isEmpty());
        
        // Test invalid range (end < start)
        primes = PrimeFinder.findPrimesInRange(10, 5);
        assertTrue("Should return empty list for invalid range", primes.isEmpty());
    }
    
    @Test
    public void testSequentialImplementation() {
        PrimeFinder.Result result = PrimeFinder.findPrimesSequential(1, 100);
        
        assertEquals("Should find 25 primes under 100", 25, result.primesFound);
        assertEquals("Should have correct start range", 1, result.startRange);
        assertEquals("Should have correct end range", 100, result.endRange);
        assertEquals("Should use 1 worker for sequential", 1, result.workers);
        assertTrue("Execution time should be positive", result.executionTime > 0);
        
        // Check first few primes
        if (result.primes != null && result.primes.size() >= 5) {
            assertEquals("First prime should be 2", Integer.valueOf(2), result.primes.get(0));
            assertEquals("Second prime should be 3", Integer.valueOf(3), result.primes.get(1));
            assertEquals("Third prime should be 5", Integer.valueOf(5), result.primes.get(2));
            assertEquals("Fourth prime should be 7", Integer.valueOf(7), result.primes.get(3));
            assertEquals("Fifth prime should be 11", Integer.valueOf(11), result.primes.get(4));
        }
    }
    
    @Test
    public void testThreadPoolImplementation() throws InterruptedException, ExecutionException {
        PrimeFinder.Result result = PrimeFinder.findPrimesThreadPool(1, 100, 4);
        
        assertEquals("Should find 25 primes under 100", 25, result.primesFound);
        assertEquals("Should use 4 workers", 4, result.workers);
        assertTrue("Execution time should be positive", result.executionTime > 0);
    }
    
    @Test
    public void testCompletableFutureImplementation() {
        PrimeFinder.Result result = PrimeFinder.findPrimesCompletableFuture(1, 100, 4);
        
        assertEquals("Should find 25 primes under 100", 25, result.primesFound);
        assertEquals("Should use 4 workers", 4, result.workers);
        assertTrue("Execution time should be positive", result.executionTime > 0);
    }
    
    @Test
    public void testParallelStreamImplementation() {
        PrimeFinder.Result result = PrimeFinder.findPrimesParallelStream(1, 100);
        
        assertEquals("Should find 25 primes under 100", 25, result.primesFound);
        assertTrue("Execution time should be positive", result.executionTime > 0);
        assertTrue("Should use multiple workers", result.workers > 0);
    }
    
    @Test
    public void testConcurrentResultsConsistency() throws Exception {
        int start = 1;
        int end = 1000;
        
        // Run all methods
        PrimeFinder.Result sequential = PrimeFinder.findPrimesSequential(start, end);
        PrimeFinder.Result threadPool = PrimeFinder.findPrimesThreadPool(start, end, 4);
        PrimeFinder.Result completable = PrimeFinder.findPrimesCompletableFuture(start, end, 4);
        PrimeFinder.Result parallel = PrimeFinder.findPrimesParallelStream(start, end);
        
        // All should find same number of primes
        assertEquals("ThreadPool should find same number of primes as sequential", 
                    sequential.primesFound, threadPool.primesFound);
        assertEquals("CompletableFuture should find same number of primes as sequential", 
                    sequential.primesFound, completable.primesFound);
        assertEquals("Parallel stream should find same number of primes as sequential", 
                    sequential.primesFound, parallel.primesFound);
        
        // Should be 168 primes under 1000
        assertEquals("Should find 168 primes under 1000", 168, sequential.primesFound);
    }
    
    @Test
    public void testLargePrimeCount() {
        // Test known prime count for specific range
        PrimeFinder.Result result = PrimeFinder.findPrimesSequential(1, 1000);
        assertEquals("Should find 168 primes under 1000", 168, result.primesFound);
    }
    
    @Test
    public void testEdgeCaseMoreWorkersThanRange() throws Exception {
        // Test with more workers than numbers in range
        PrimeFinder.Result result = PrimeFinder.findPrimesThreadPool(1, 10, 100);
        
        assertEquals("Should still find correct number of primes", 4, result.primesFound);
        assertTrue("Should complete successfully", result.executionTime > 0);
    }
    
    @Test
    public void testPerformanceImprovement() throws Exception {
        // Skip this test in fast mode as it takes time
        if (System.getProperty("test.fast") != null) {
            return;
        }
        
        int start = 1;
        int end = 50000;
        
        // Sequential
        PrimeFinder.Result sequential = PrimeFinder.findPrimesSequential(start, end);
        
        // Parallel with multiple workers
        PrimeFinder.Result parallel = PrimeFinder.findPrimesThreadPool(start, end, 
                                                Runtime.getRuntime().availableProcessors());
        
        // Parallel should be faster (allow some margin for system variance)
        if (Runtime.getRuntime().availableProcessors() > 1) {
            double speedup = sequential.executionTime / parallel.executionTime;
            assertTrue("Parallel execution should show speedup, got " + speedup + "x", 
                      speedup > 1.2);
        }
    }
    
    @Test
    public void testResultToMap() {
        PrimeFinder.Result result = new PrimeFinder.Result(1, 100, 25, 1.5, 4, new ArrayList<>());
        
        var map = result.toMap();
        
        assertEquals("Map should contain primes_found", 25, map.get("primes_found"));
        assertEquals("Map should contain execution_time", 1.5, (Double)map.get("execution_time"), DELTA);
        assertEquals("Map should contain workers", 4, map.get("workers"));
    }
    
    @Test
    public void testNegativeNumbers() {
        // Negative numbers should not be prime
        assertFalse("Negative numbers should not be prime", PrimeFinder.isPrime(-5));
        assertFalse("Negative numbers should not be prime", PrimeFinder.isPrime(-1));
        
        // Range with negative numbers
        List<Integer> primes = PrimeFinder.findPrimesInRange(-10, 10);
        List<Integer> expected = Arrays.asList(2, 3, 5, 7);
        assertEquals("Should only find positive primes", expected, primes);
    }
    
    @Test
    public void testZero() {
        assertFalse("Zero should not be prime", PrimeFinder.isPrime(0));
    }
    
    @Test(timeout = 5000) // 5 second timeout
    public void testPerformanceTimeout() {
        // Ensure the algorithm completes in reasonable time
        PrimeFinder.Result result = PrimeFinder.findPrimesSequential(1, 100000);
        assertTrue("Should find primes within timeout", result.primesFound > 0);
    }
}