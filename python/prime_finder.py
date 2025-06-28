# prime_finder.py
import argparse
import json
import time
import threading
import multiprocessing
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor
from typing import List, Tuple
import os

def is_prime(n: int) -> bool:
    """Check if a number is prime using trial division"""
    if n <= 1:
        return False
    if n <= 3:
        return True
    if n % 2 == 0 or n % 3 == 0:
        return False
    
    i = 5
    while i * i <= n:
        if n % i == 0 or n % (i + 2) == 0:
            return False
        i += 6
    return True

def find_primes_in_range(start: int, end: int) -> List[int]:
    """Find all primes in a given range"""
    return [i for i in range(start, end + 1) if is_prime(i)]

def find_primes_sequential(start: int, end: int) -> Tuple[List[int], float]:
    """Find primes sequentially"""
    start_time = time.time()
    primes = find_primes_in_range(start, end)
    duration = time.time() - start_time
    return primes, duration

def find_primes_threading(start: int, end: int, num_threads: int) -> Tuple[List[int], float]:
    """Find primes using threading"""
    start_time = time.time()
    
    chunk_size = (end - start + 1) // num_threads
    if chunk_size < 1:
        chunk_size = 1
    
    all_primes = []
    lock = threading.Lock()
    
    def worker(range_start, range_end):
        primes = find_primes_in_range(range_start, range_end)
        with lock:
            all_primes.extend(primes)
    
    with ThreadPoolExecutor(max_workers=num_threads) as executor:
        futures = []
        for i in range(start, end + 1, chunk_size):
            range_end = min(i + chunk_size - 1, end)
            future = executor.submit(worker, i, range_end)
            futures.append(future)
        
        # Wait for all to complete
        for future in futures:
            future.result()
    
    duration = time.time() - start_time
    return sorted(all_primes), duration

def find_primes_multiprocessing(start: int, end: int, num_processes: int) -> Tuple[List[int], float]:
    """Find primes using multiprocessing"""
    start_time = time.time()
    
    chunk_size = (end - start + 1) // num_processes
    if chunk_size < 1:
        chunk_size = 1
    
    # Create ranges for each process
    ranges = []
    for i in range(start, end + 1, chunk_size):
        range_end = min(i + chunk_size - 1, end)
        ranges.append((i, range_end))
    
    with ProcessPoolExecutor(max_workers=num_processes) as executor:
        # Map the function to all ranges
        results = executor.map(lambda r: find_primes_in_range(r[0], r[1]), ranges)
        
        # Flatten results
        all_primes = []
        for primes in results:
            all_primes.extend(primes)
    
    duration = time.time() - start_time
    return sorted(all_primes), duration

def benchmark_all_methods(start: int, end: int, workers: int):
    """Benchmark all methods and return results"""
    results = {}
    
    # Sequential
    print("Running sequential version...")
    primes, duration = find_primes_sequential(start, end)
    results['sequential'] = {
        'primes_found': len(primes),
        'execution_time': duration,
        'workers': 1
    }
    print(f"Sequential: {len(primes)} primes in {duration:.4f} seconds")
    
    # Threading
    print(f"\nRunning threading version with {workers} threads...")
    primes_thread, duration_thread = find_primes_threading(start, end, workers)
    results['threading'] = {
        'primes_found': len(primes_thread),
        'execution_time': duration_thread,
        'workers': workers,
        'speedup': duration / duration_thread
    }
    print(f"Threading: {len(primes_thread)} primes in {duration_thread:.4f} seconds")
    print(f"Speedup: {results['threading']['speedup']:.2f}x")
    
    # Multiprocessing
    print(f"\nRunning multiprocessing version with {workers} processes...")
    primes_multi, duration_multi = find_primes_multiprocessing(start, end, workers)
    results['multiprocessing'] = {
        'primes_found': len(primes_multi),
        'execution_time': duration_multi,
        'workers': workers,
        'speedup': duration / duration_multi
    }
    print(f"Multiprocessing: {len(primes_multi)} primes in {duration_multi:.4f} seconds")
    print(f"Speedup: {results['multiprocessing']['speedup']:.2f}x")
    
    return results, primes

def main():
    parser = argparse.ArgumentParser(description='Prime number finder with different concurrency methods')
    parser.add_argument('--start', type=int, default=1, help='Start of range')
    parser.add_argument('--end', type=int, default=100000, help='End of range')
    parser.add_argument('--workers', type=int, default=os.cpu_count(), help='Number of workers')
    parser.add_argument('--method', choices=['sequential', 'threading', 'multiprocessing', 'all'], 
                       default='all', help='Method to use')
    parser.add_argument('--save-primes', action='store_true', help='Save actual prime numbers')
    parser.add_argument('--output', default='python_results.json', help='Output file')
    
    args = parser.parse_args()
    
    print(f"Finding primes from {args.start} to {args.end}")
    print(f"CPU count: {os.cpu_count()}")
    
    if args.method == 'all':
        results, primes = benchmark_all_methods(args.start, args.end, args.workers)
        
        # Add configuration to results
        results['configuration'] = {
            'start_range': args.start,
            'end_range': args.end,
            'cpu_count': os.cpu_count()
        }
        
        if args.save_primes:
            results['primes'] = primes
    else:
        # Run specific method
        if args.method == 'sequential':
            primes, duration = find_primes_sequential(args.start, args.end)
            workers = 1
        elif args.method == 'threading':
            primes, duration = find_primes_threading(args.start, args.end, args.workers)
            workers = args.workers
        else:  # multiprocessing
            primes, duration = find_primes_multiprocessing(args.start, args.end, args.workers)
            workers = args.workers
        
        results = {
            args.method: {
                'primes_found': len(primes),
                'execution_time': duration,
                'workers': workers
            },
            'configuration': {
                'start_range': args.start,
                'end_range': args.end,
                'cpu_count': os.cpu_count()
            }
        }
        
        if args.save_primes:
            results['primes'] = primes
        
        print(f"\nFound {len(primes)} primes in {duration:.4f} seconds")
    
    # Save results
    with open(args.output, 'w') as f:
        json.dump(results, f, indent=2)
    
    print(f"\nResults saved to {args.output}")

if __name__ == "__main__":
    main()

# test_prime_finder.py
import unittest
from prime_finder import is_prime, find_primes_in_range

class TestPrimeFinder(unittest.TestCase):
    def test_is_prime(self):
        """Test the is_prime function"""
        test_cases = [
            (1, False),
            (2, True),
            (3, True),
            (4, False),
            (5, True),
            (100, False),
            (101, True),
            (1000, False),
            (1009, True),
        ]
        
        for n, expected in test_cases:
            with self.subTest(n=n):
                self.assertEqual(is_prime(n), expected)
    
    def test_find_primes_in_range(self):
        """Test finding primes in a range"""
        primes = find_primes_in_range(1, 10)
        self.assertEqual(primes, [2, 3, 5, 7])
        
        primes = find_primes_in_range(10, 20)
        self.assertEqual(primes, [11, 13, 17, 19])
    
    def test_empty_range(self):
        """Test empty range"""
        primes = find_primes_in_range(0, 1)
        self.assertEqual(primes, [])

if __name__ == '__main__':
    unittest.main()