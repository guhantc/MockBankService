#!/bin/bash

# HDFC Bank Microservices Test Suite
# This script runs comprehensive tests for all microservices

set -e

echo "🧪 Running HDFC Bank Test Suite..."
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Test report file
REPORT_FILE="$PROJECT_ROOT/test-report.html"
COVERAGE_DIR="$PROJECT_ROOT/coverage-reports"

# Create directories
mkdir -p "$COVERAGE_DIR"

# Services to test
SERVICES=(
    "eureka-server"
    "api-gateway"
    "user-service"
    "saga-orchestrator"
    "account-service"
    "loan-service"
    "card-service"
    "investment-service"
    "transaction-service"
    "notification-service"
)

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Start HTML report
cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>HDFC Bank Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #1e3a8a; color: white; padding: 20px; border-radius: 8px; }
        .summary { margin: 20px 0; padding: 15px; background: #f3f4f6; border-radius: 8px; }
        .service { margin: 10px 0; padding: 15px; border: 1px solid #ddd; border-radius: 8px; }
        .success { background: #dcfce7; border-color: #16a34a; }
        .failure { background: #fef2f2; border-color: #dc2626; }
        .warning { background: #fefce8; border-color: #ca8a04; }
        .coverage { margin: 10px 0; }
        .metric { display: inline-block; margin-right: 20px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🏦 HDFC Bank Microservices Test Report</h1>
        <p>Generated on: $(date)</p>
    </div>
EOF

# Function to run tests for a service
run_service_tests() {
    local service=$1
    local start_time=$(date +%s)
    
    print_status "Testing $service..."
    
    if [ ! -d "$service" ]; then
        print_warning "Service directory $service not found, skipping..."
        echo "<div class=\"service warning\"><h3>⚠️ $service</h3><p>Service directory not found</p></div>" >> "$REPORT_FILE"
        return
    fi
    
    cd "$service"
    
    # Unit Tests
    print_status "Running unit tests for $service..."
    if mvn test -q > "../test-output-$service.log" 2>&1; then
        local test_result="success"
        print_success "Unit tests passed for $service"
    else
        local test_result="failure"
        print_error "Unit tests failed for $service"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    
    # Integration Tests
    print_status "Running integration tests for $service..."
    if mvn test -Dtest="*IntegrationTest" -q >> "../test-output-$service.log" 2>&1; then
        print_success "Integration tests passed for $service"
    else
        print_warning "Integration tests failed for $service"
    fi
    
    # Code Coverage
    print_status "Generating code coverage for $service..."
    mvn jacoco:report -q >> "../test-output-$service.log" 2>&1 || true
    
    # Extract test metrics
    local surefire_report="target/surefire-reports"
    local test_count=0
    local failure_count=0
    local skip_count=0
    
    if [ -d "$surefire_report" ]; then
        test_count=$(find "$surefire_report" -name "*.xml" -exec grep -l "testcase" {} \; | wc -l)
        failure_count=$(find "$surefire_report" -name "*.xml" -exec grep -c "failure\|error" {} \; | awk '{sum+=$1} END {print sum+0}')
    fi
    
    # Extract coverage percentage
    local coverage="N/A"
    if [ -f "target/site/jacoco/index.html" ]; then
        coverage=$(grep -o '[0-9]\+%' target/site/jacoco/index.html | head -1 || echo "N/A")
        cp -r target/site/jacoco "$COVERAGE_DIR/$service-coverage" 2>/dev/null || true
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    TOTAL_TESTS=$((TOTAL_TESTS + test_count))
    
    # Add to HTML report
    local status_class="success"
    local status_icon="✅"
    if [ "$test_result" = "failure" ]; then
        status_class="failure"
        status_icon="❌"
    fi
    
    cat >> "$REPORT_FILE" << EOF
    <div class="service $status_class">
        <h3>$status_icon $service</h3>
        <div class="coverage">
            <span class="metric"><strong>Tests:</strong> $test_count</span>
            <span class="metric"><strong>Failures:</strong> $failure_count</span>
            <span class="metric"><strong>Coverage:</strong> $coverage</span>
            <span class="metric"><strong>Duration:</strong> ${duration}s</span>
        </div>
        <details>
            <summary>View Details</summary>
            <pre>$(cat "../test-output-$service.log" | tail -50)</pre>
        </details>
    </div>
EOF
    
    cd "$PROJECT_ROOT"
}

# Run tests for each service
for service in "${SERVICES[@]}"; do
    run_service_tests "$service"
done

# Performance Tests
print_status "Running performance tests..."
if [ -f "tests/performance/load-test.sh" ]; then
    bash tests/performance/load-test.sh > "performance-test.log" 2>&1 || true
    echo "<div class=\"service\"><h3>🚀 Performance Tests</h3><pre>$(cat performance-test.log)</pre></div>" >> "$REPORT_FILE"
fi

# Security Tests
print_status "Running security tests..."
if command -v dependency-check &> /dev/null; then
    dependency-check --project "HDFC Bank" --scan . --format HTML --out security-report.html > "security-test.log" 2>&1 || true
    echo "<div class=\"service\"><h3>🔒 Security Scan</h3><p>Security report generated: <a href=\"security-report.html\">View Report</a></p></div>" >> "$REPORT_FILE"
fi

# Generate summary
PASSED_TESTS=$((TOTAL_TESTS - FAILED_TESTS))
SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS)) 2>/dev/null || SUCCESS_RATE=0

# Complete HTML report
cat >> "$REPORT_FILE" << EOF
    <div class="summary">
        <h2>📊 Test Summary</h2>
        <div class="metric"><strong>Total Tests:</strong> $TOTAL_TESTS</div>
        <div class="metric"><strong>Passed:</strong> $PASSED_TESTS</div>
        <div class="metric"><strong>Failed:</strong> $FAILED_TESTS</div>
        <div class="metric"><strong>Success Rate:</strong> $SUCCESS_RATE%</div>
    </div>
    
    <div class="summary">
        <h2>📋 Coverage Reports</h2>
        <ul>
EOF

# Add coverage report links
for service in "${SERVICES[@]}"; do
    if [ -d "$COVERAGE_DIR/$service-coverage" ]; then
        echo "            <li><a href=\"coverage-reports/$service-coverage/index.html\">$service Coverage Report</a></li>" >> "$REPORT_FILE"
    fi
done

cat >> "$REPORT_FILE" << EOF
        </ul>
    </div>
</body>
</html>
EOF

# Print summary
echo ""
echo "📊 Test Summary"
echo "==============="
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"
echo "Success Rate: $SUCCESS_RATE%"
echo ""
print_success "Test report generated: $REPORT_FILE"

if [ -d "$COVERAGE_DIR" ]; then
    print_success "Coverage reports available in: $COVERAGE_DIR"
fi

# Clean up temporary files
rm -f test-output-*.log performance-test.log security-test.log

# Exit with error if any tests failed
if [ $FAILED_TESTS -gt 0 ]; then
    print_error "Some tests failed. Check the report for details."
    exit 1
else
    print_success "All tests passed!"
    exit 0
fi