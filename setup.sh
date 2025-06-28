#!/bin/bash
# setup.sh - Setup script for Concurrent Prime Finder project

set -e  # Exit on error

echo "=================================================="
echo "Setting up Concurrent Prime Finder Project"
echo "=================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

# Check if running in project root
if [ ! -f "README.md" ]; then
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Create directory structure
echo "Creating project directories..."
mkdir -p go python java results docs
print_status "Directory structure created"

# Check for required commands
echo ""
echo "Checking dependencies..."

# Check Go
if command -v go &> /dev/null; then
    GO_VERSION=$(go version | awk '{print $3}')
    print_status "Go installed: $GO_VERSION"
else
    print_warning "Go not found. Please install Go 1.22 or higher"
fi

# Check Python
if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version)
    print_status "Python installed: $PYTHON_VERSION"
elif command -v python &> /dev/null; then
    PYTHON_VERSION=$(python --version)
    print_status "Python installed: $PYTHON_VERSION"
    # Create alias for python3
    alias python3=python
else
    print_warning "Python not found. Please install Python 3.12 or higher"
fi

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_status "Java installed: $JAVA_VERSION"
else
    print_warning "Java not found. Please install Java 21 or higher"
fi

# Check javac
if command -v javac &> /dev/null; then
    JAVAC_VERSION=$(javac -version 2>&1)
    print_status "Java compiler installed: $JAVAC_VERSION"
else
    print_warning "javac not found. Please install JDK (not just JRE)"
fi

# Setup Go project
echo ""
echo "Setting up Go project..."
cd go

# Initialize Go module if not exists
if [ ! -f "go.mod" ]; then
    go mod init prime-finder
    print_status "Go module initialized"
else
    print_status "Go module already exists"
fi

# Verify Go setup by running tests (if main.go exists)
if [ -f "main.go" ] && [ -f "benchmark_test.go" ]; then
    echo "Running Go tests to verify setup..."
    if go test -short; then
        print_status "Go setup verified"
    else
        print_warning "Go tests failed - check your implementation"
    fi
fi

cd ..

# Setup Python project
echo ""
echo "Setting up Python project..."
cd python

# Determine Python command
if command -v python3 &> /dev/null; then
    PYTHON_CMD=python3
elif command -v python &> /dev/null; then
    PYTHON_CMD=python
else
    print_error "Python not found!"
    cd ..
    exit 1
fi

# Create requirements.txt if it doesn't exist
if [ ! -f "requirements.txt" ]; then
    cat > requirements.txt << 'EOF'
pytest==7.4.3
pytest-benchmark==4.0.0
matplotlib==3.8.2
numpy==1.26.2
EOF
    print_status "Created requirements.txt"
fi

# Create virtual environment
if [ ! -d "venv" ]; then
    echo "Creating Python virtual environment..."
    $PYTHON_CMD -m venv venv
    print_status "Virtual environment created"
else
    print_status "Virtual environment already exists"
fi

# Activate virtual environment and install dependencies
echo "Installing Python dependencies..."
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    # Windows
    source venv/Scripts/activate 2>/dev/null || venv\\Scripts\\activate
else
    # Unix-like
    source venv/bin/activate
fi

pip install --quiet --upgrade pip
pip install --quiet -r requirements.txt
print_status "Python dependencies installed"

# Run Python tests if available
if [ -f "prime_finder.py" ] && [ -f "test_prime_finder.py" ]; then
    echo "Running Python tests to verify setup..."
    if python -m pytest test_prime_finder.py -v --tb=short -q; then
        print_status "Python setup verified"
    else
        print_warning "Python tests failed - check your implementation"
    fi
fi

deactivate 2>/dev/null || true
cd ..

# Setup Java project
echo ""
echo "Setting up Java project..."
cd java

# Download required JAR files if they don't exist
echo "Downloading Java dependencies..."

# Download Gson
if [ ! -f "gson-2.10.1.jar" ]; then
    echo "Downloading Gson..."
    if command -v curl &> /dev/null; then
        curl -L -o gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
    elif command -v wget &> /dev/null; then
        wget -O gson-2.10.1.jar https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar
    else
        print_error "Neither curl nor wget found. Please download Gson manually"
    fi
    
    if [ -f "gson-2.10.1.jar" ]; then
        print_status "Gson downloaded"
    fi
else
    print_status "Gson already exists"
fi

# Download JUnit
if [ ! -f "junit-4.13.2.jar" ]; then
    echo "Downloading JUnit..."
    if command -v curl &> /dev/null; then
        curl -L -o junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
    elif command -v wget &> /dev/null; then
        wget -O junit-4.13.2.jar https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar
    else
        print_error "Neither curl nor wget found. Please download JUnit manually"
    fi
    
    if [ -f "junit-4.13.2.jar" ]; then
        print_status "JUnit downloaded"
    fi
else
    print_status "JUnit already exists"
fi

# Download Hamcrest (required by JUnit)
if [ ! -f "hamcrest-core-1.3.jar" ]; then
    echo "Downloading Hamcrest..."
    if command -v curl &> /dev/null; then
        curl -L -o hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
    elif command -v wget &> /dev/null; then
        wget -O hamcrest-core-1.3.jar https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar
    else
        print_error "Neither curl nor wget found. Please download Hamcrest manually"
    fi
    
    if [ -f "hamcrest-core-1.3.jar" ]; then
        print_status "Hamcrest downloaded"
    fi
else
    print_status "Hamcrest already exists"
fi

# Test Java compilation if source exists
if [ -f "PrimeFinder.java" ]; then
    echo "Testing Java compilation..."
    if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
        CLASSPATH=".;gson-2.10.1.jar"
    else
        CLASSPATH=".:gson-2.10.1.jar"
    fi
    
    if javac -cp "$CLASSPATH" PrimeFinder.java 2>/dev/null; then
        print_status "Java compilation successful"
        rm -f *.class 2>/dev/null
    else
        print_warning "Java compilation failed - check your implementation"
    fi
fi

cd ..

# Create sample URLs file if it doesn't exist
if [ ! -f "urls.txt" ]; then
    echo "Creating sample urls.txt..."
    cat > urls.txt << 'EOF'
# Sample URLs for web scraper project (if needed)
https://httpbin.org/html
https://example.com
https://example.org
EOF
    print_status "Sample urls.txt created"
fi

# Final summary
echo ""
echo "=================================================="
echo "Setup Complete!"
echo "=================================================="
echo ""
echo "Next steps:"
echo "1. Copy your implementation files to their respective directories:"
echo "   - Go: main.go → go/"
echo "   - Python: prime_finder.py → python/"
echo "   - Java: PrimeFinder.java → java/"
echo ""
echo "2. Run individual implementations:"
echo "   - Go: cd go && go run main.go"
echo "   - Python: cd python && source venv/bin/activate && python prime_finder.py"
echo "   - Java: cd java && javac -cp .:gson-2.10.1.jar PrimeFinder.java && java -cp .:gson-2.10.1.jar PrimeFinder"
echo ""
echo "3. Run all benchmarks:"
echo "   chmod +x run_all_benchmarks.sh"
echo "   ./run_all_benchmarks.sh"
echo ""

# Check if all dependencies are installed
if command -v go &> /dev/null && command -v python3 &> /dev/null && command -v java &> /dev/null && command -v javac &> /dev/null; then
    print_status "All dependencies found. You're ready to go!"
else
    print_warning "Some dependencies are missing. Please install them before running the benchmarks."
fi