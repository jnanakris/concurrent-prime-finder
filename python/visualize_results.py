# visualize_results.py
import json
import matplotlib.pyplot as plt
import numpy as np
from pathlib import Path

def load_results():
    """Load benchmark results from JSON files"""
    results = {}
    
    # Load Go results
    go_file = Path("results/go_results.json")
    if go_file.exists():
        with open(go_file) as f:
            data = json.load(f)
            results['Go'] = {
                'time': data['execution_time'],
                'workers': data['workers'],
                'primes': data['primes_found']
            }
    
    # Load Python results
    python_file = Path("results/python_results.json")
    if python_file.exists():
        with open(python_file) as f:
            data = json.load(f)
            if 'multiprocessing' in data:
                results['Python (MP)'] = {
                    'time': data['multiprocessing']['execution_time'],
                    'workers': data['multiprocessing']['workers'],
                    'primes': data['multiprocessing']['primes_found']
                }
            if 'threading' in data:
                results['Python (Thread)'] = {
                    'time': data['threading']['execution_time'],
                    'workers': data['threading']['workers'],
                    'primes': data['threading']['primes_found']
                }
    
    # Load Java results
    java_file = Path("results/java_results.json")
    if java_file.exists():
        with open(java_file) as f:
            data = json.load(f)
            if 'threadpool' in data:
                results['Java (ThreadPool)'] = {
                    'time': data['threadpool']['execution_time'],
                    'workers': data['threadpool']['workers'],
                    'primes': data['threadpool']['primes_found']
                }
            if 'parallelstream' in data:
                results['Java (Parallel)'] = {
                    'time': data['parallelstream']['execution_time'],
                    'workers': data['parallelstream']['workers'],
                    'primes': data['parallelstream']['primes_found']
                }
    
    return results

def create_visualizations(results):
    """Create performance comparison charts"""
    
    # Set up the plot style
    plt.style.use('seaborn-v0_8-darkgrid')
    fig, ((ax1, ax2), (ax3, ax4)) = plt.subplots(2, 2, figsize=(12, 10))
    
    # Extract data
    languages = list(results.keys())
    times = [results[lang]['time'] for lang in languages]
    workers = [results[lang]['workers'] for lang in languages]
    
    # 1. Execution Time Comparison
    colors = plt.cm.viridis(np.linspace(0, 1, len(languages)))
    bars1 = ax1.bar(languages, times, color=colors)
    ax1.set_ylabel('Execution Time (seconds)')
    ax1.set_title('Execution Time by Implementation')
    ax1.tick_params(axis='x', rotation=45)
    
    # Add value labels on bars
    for bar, time in zip(bars1, times):
        height = bar.get_height()
        ax1.text(bar.get_x() + bar.get_width()/2., height,
                f'{time:.2f}s', ha='center', va='bottom')
    
    # 2. Speedup Comparison (if sequential data available)
    ax2.set_title('Relative Performance')
    if times:
        max_time = max(times)
        speedups = [max_time / t for t in times]
        bars2 = ax2.bar(languages, speedups, color=colors)
        ax2.set_ylabel('Relative Speed (vs slowest)')
        ax2.tick_params(axis='x', rotation=45)
        
        for bar, speedup in zip(bars2, speedups):
            height = bar.get_height()
            ax2.text(bar.get_x() + bar.get_width()/2., height,
                    f'{speedup:.2f}x', ha='center', va='bottom')
    
    # 3. Workers Used
    ax3.bar(languages, workers, color=colors)
    ax3.set_ylabel('Number of Workers')
    ax3.set_title('Concurrent Workers Used')
    ax3.tick_params(axis='x', rotation=45)
    
    # 4. Efficiency (Primes found per second)
    if all('primes' in results[lang] for lang in languages):
        efficiency = [results[lang]['primes'] / results[lang]['time'] for lang in languages]
        bars4 = ax4.bar(languages, efficiency, color=colors)
        ax4.set_ylabel('Primes per Second')
        ax4.set_title('Processing Efficiency')
        ax4.tick_params(axis='x', rotation=45)
        
        for bar, eff in zip(bars4, efficiency):
            height = bar.get_height()
            ax4.text(bar.get_x() + bar.get_width()/2., height,
                    f'{eff:.0f}', ha='center', va='bottom')
    
    plt.tight_layout()
    plt.savefig('results/performance_comparison.png', dpi=300, bbox_inches='tight')
    plt.show()
    
    # Create a summary table
    print("\nPerformance Summary")
    print("=" * 60)
    print(f"{'Implementation':<20} {'Time (s)':<10} {'Workers':<10} {'Primes':<10}")
    print("-" * 60)
    for lang in languages:
        print(f"{lang:<20} {results[lang]['time']:<10.2f} {results[lang]['workers']:<10} {results[lang]['primes']:<10}")

if __name__ == "__main__":
    results = load_results()
    if results:
        create_visualizations(results)
    else:
        print("No results found! Run benchmarks first.")

---
# create_sample_results.py
# This creates sample results for testing the visualization
import json

# Sample Go results
go_results = {
    "start_range": 1,
    "end_range": 1000000,
    "primes_found": 78498,
    "execution_time": 1.823,
    "workers": 8
}

# Sample Python results
python_results = {
    "configuration": {
        "start_range": 1,
        "end_range": 1000000,
        "cpu_count": 8
    },
    "sequential": {
        "primes_found": 78498,
        "execution_time": 12.456,
        "workers": 1
    },
    "threading": {
        "primes_found": 78498,
        "execution_time": 12.234,
        "workers": 8,
        "speedup": 1.02
    },
    "multiprocessing": {
        "primes_found": 78498,
        "execution_time": 2.145,
        "workers": 8,
        "speedup": 5.81
    }
}

# Sample Java results
java_results = {
    "configuration": {
        "start_range": 1,
        "end_range": 1000000,
        "cpu_count": 8
    },
    "sequential": {
        "primes_found": 78498,
        "execution_time": 11.234,
        "workers": 1
    },
    "threadpool": {
        "primes_found": 78498,
        "execution_time": 1.756,
        "workers": 8
    },
    "parallelstream": {
        "primes_found": 78498,
        "execution_time": 1.623,
        "workers": 8
    }
}

# Save sample results
import os
os.makedirs("results", exist_ok=True)

with open("results/go_results.json", "w") as f:
    json.dump(go_results, f, indent=2)

with open("results/python_results.json", "w") as f:
    json.dump(python_results, f, indent=2)

with open("results/java_results.json", "w") as f:
    json.dump(java_results, f, indent=2)

print("Sample results created in results/")