# test_prime_finder.py
import unittest
import time
import multiprocessing
from prime_finder import (
    is_prime, 
    find_primes_in_range, 
    find_primes_sequential,
    find_primes_threading,
    find_primes_multiprocessing
)

class TestPrimeFinder(unittest.TestCase):
    """Test cases for prime finder functions"""
    
    def test_is_prime(self):
        """Test the is_prime function with known values"""
        test_cases = [
            (1, False),
            (2, True),
            (3, True),
            (4, False),
            (5, True),
            (6, False),
            (7, True),
            (8, False),
            (9, False),
            (10, False),
            (11, True),
            (100, False),
            (101, True),
            (1000, False),
            (1009, True),
            (1013, True),
        ]
        
        for n, expected in test_cases:
            with self.subTest(n=n):
                self.assertEqual(is_prime(n), expected, 
                               f"is_prime({n}) should return {expected}")
    
    def test_find_primes_in_range(self):
        """Test finding primes in a specific range"""
        # Test small range
        primes = find_primes_in_range(1, 10)
        self.assertEqual(primes, [2, 3, 5, 7])
        
        # Test range starting from non-one
        primes = find_primes_in_range(10, 20)
        self.assertEqual(primes, [11, 13, 17, 19])
        
        # Test range with no primes
        primes = find_primes_in_range(24, 28)
        self.assertEqual(primes, [])
        
        # Test single number range (prime)
        primes = find_primes_in_range(17, 17)
        self.assertEqual(primes, [17])
        
        # Test single number range (non-prime)
        primes = find_primes_in_range(18, 18)
        self.assertEqual(primes, [])
    
    def test_empty_range(self):
        """Test empty or invalid ranges"""
        primes = find_primes_in_range(0, 1)
        self.assertEqual(primes, [])
        
        # Test reverse range
        primes = find_primes_in_range(10, 5)
        self.assertEqual(primes, [])
    
    def test_sequential_implementation(self):
        """Test sequential prime finding"""
        primes, duration = find_primes_sequential(1, 100)
        
        # Check correct number of primes found
        self.assertEqual(len(primes), 25)  # There are 25 primes under 100
        
        # Check first and last few primes
        self.assertEqual(primes[:5], [2, 3, 5, 7, 11])
        self.assertEqual(primes[-3:], [83, 89, 97])
        
        # Check duration is positive
        self.assertGreater(duration, 0)
    
    def test_threading_implementation(self):
        """Test threading implementation"""
        primes, duration = find_primes_threading(1, 100, num_threads=4)
        
        # Results should be same as sequential
        self.assertEqual(len(primes), 25)
        self.assertIn(2, primes)
        self.assertIn(97, primes)
        
        # Check duration is positive
        self.assertGreater(duration, 0)
    
    def test_multiprocessing_implementation(self):
        """Test multiprocessing implementation"""
        primes, duration = find_primes_multiprocessing(1, 100, num_processes=4)
        
        # Results should be same as sequential
        self.assertEqual(len(primes), 25)
        self.assertEqual(primes[0], 2)
        self.assertEqual(primes[-1], 97)
        
        # Check duration is positive
        self.assertGreater(duration, 0)
    
    def test_consistency_across_methods(self):
        """Test that all methods produce same results"""
        test_range = (1, 1000)
        
        # Run all methods
        seq_primes, _ = find_primes_sequential(*test_range)
        thread_primes, _ = find_primes_threading(*test_range, num_threads=4)
        mp_primes, _ = find_primes_multiprocessing(*test_range, num_processes=4)
        
        # All should find same number of primes
        self.assertEqual(len(seq_primes), len(thread_primes))
        self.assertEqual(len(seq_primes), len(mp_primes))
        
        # All should find same primes (order might differ)
        self.assertEqual(set(seq_primes), set(thread_primes))
        self.assertEqual(set(seq_primes), set(mp_primes))
    
    def test_large_range_prime_count(self):
        """Test known prime count for larger range"""
        # There are 168 primes less than 1000
        primes, _ = find_primes_sequential(1, 1000)
        self.assertEqual(len(primes), 168)
    
    def test_performance_scaling(self):
        """Test that multiprocessing is faster than sequential for large ranges"""
        test_range = (1, 50000)
        
        # Sequential
        _, seq_duration = find_primes_sequential(*test_range)
        
        # Multiprocessing with CPU count workers
        _, mp_duration = find_primes_multiprocessing(
            *test_range, 
            num_processes=multiprocessing.cpu_count()
        )
        
        # Multiprocessing should be faster (at least 1.5x)
        # Note: This might fail on single-core machines
        if multiprocessing.cpu_count() > 1:
            speedup = seq_duration / mp_duration
            self.assertGreater(speedup, 1.5, 
                             f"Expected speedup > 1.5, got {speedup:.2f}")

class TestEdgeCases(unittest.TestCase):
    """Test edge cases and error conditions"""
    
    def test_single_worker(self):
        """Test with single worker (should work like sequential)"""
        primes_seq, _ = find_primes_sequential(1, 100)
        primes_thread, _ = find_primes_threading(1, 100, num_threads=1)
        primes_mp, _ = find_primes_multiprocessing(1, 100, num_processes=1)
        
        self.assertEqual(len(primes_seq), len(primes_thread))
        self.assertEqual(len(primes_seq), len(primes_mp))
    
    def test_more_workers_than_range(self):
        """Test with more workers than numbers in range"""
        # Range has only 10 numbers, but use 20 workers
        primes, duration = find_primes_multiprocessing(1, 10, num_processes=20)
        
        # Should still work correctly
        self.assertEqual(primes, [2, 3, 5, 7])
        self.assertGreater(duration, 0)
    
    def test_very_large_prime(self):
        """Test with a known large prime"""
        large_prime = 7919  # 1000th prime number
        self.assertTrue(is_prime(large_prime))
        self.assertFalse(is_prime(large_prime + 1))

if __name__ == '__main__':
    # Run tests with verbosity
    unittest.main(verbosity=2)

---