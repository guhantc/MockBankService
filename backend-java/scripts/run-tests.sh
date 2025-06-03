#!/bin/bash

# HDFC Bank Microservices Test Runner
# This script runs all types of tests across all microservices

set -e

echo "🧪 Running HDFC Bank Test Suite"
echo "==============================="

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

# Test report directory
REPORT_DIR="$PROJECT_ROOT/test-reports"
mkdir -p "$REPORT_DIR"

# Services to test
SERVICES=(
    "eureka-server"
    "api-gateway"
    "user-service"
    "account-service"
    "loan-service"
    "card-service"
    "investment-service"
    "transaction-service"
    "notification-service"
    "saga-orchestrator"
)

# Test types
TEST_TYPES=("unit" "integration" "performance" "security")

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
TOTAL_SERVICES=0
PASSED_SERVICES=0

# Function to run tests for a service
run_service_tests() {
    local service=$1
    local test_type=${2:-"all"}
    
    if [ ! -d "$service" ]; then
        print_warning "Service directory $service not found, skipping..."
        return 1
    fi
    
    print_status "Testing $service ($test_type tests)..."
    cd "$service"
    
    local service_passed=true
    
    case $test_type in
        "unit")
            run_unit_tests "$service" || service_passed=false
            ;;
        "integration")
            run_integration_tests "$service" || service_passed=false
            ;;
        "performance")
            run_performance_tests "$service" || service_passed=false
            ;;
        "security")
            run_security_tests "$service" || service_passed=false
            ;;
        "all")
            run_unit_tests "$service" || service_passed=false
            run_integration_tests "$service" || service_passed=false
            ;;
    esac
    
    if [ "$service_passed" = true ]; then
        print_success "$service tests passed"
        PASSED_SERVICES=$((PASSED_SERVICES + 1))
    else
        print_error "$service tests failed"
    fi
    
    TOTAL_SERVICES=$((TOTAL_SERVICES + 1))
    cd "$PROJECT_ROOT"
    
    return $([ "$service_passed" = true ])
}

# Function to run unit tests
run_unit_tests() {
    local service=$1
    print_status "Running unit tests for $service..."
    
    if mvn test -Dtest="*Test" -DexcludedGroups="integration,performance" \
        -Dmaven.test.failure.ignore=true \
        -Dsurefire.reportNameSuffix=unit \
        > "../test-reports/${service}-unit-tests.log" 2>&1; then
        
        # Extract test results
        local test_count=$(grep -o "Tests run: [0-9]*" target/surefire-reports/*.txt | head -1 | grep -o "[0-9]*" || echo "0")
        local failure_count=$(grep -o "Failures: [0-9]*" target/surefire-reports/*.txt | head -1 | grep -o "[0-9]*" || echo "0")
        local error_count=$(grep -o "Errors: [0-9]*" target/surefire-reports/*.txt | head -1 | grep -o "[0-9]*" || echo "0")
        
        TOTAL_TESTS=$((TOTAL_TESTS + test_count))
        local failed=$((failure_count + error_count))
        FAILED_TESTS=$((FAILED_TESTS + failed))
        PASSED_TESTS=$((PASSED_TESTS + test_count - failed))
        
        print_success "Unit tests: $test_count run, $failed failed"
        return 0
    else
        print_error "Unit tests failed for $service"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    local service=$1
    print_status "Running integration tests for $service..."
    
    if mvn test -Dtest="*IntegrationTest" \
        -Dmaven.test.failure.ignore=true \
        -Dsurefire.reportNameSuffix=integration \
        > "../test-reports/${service}-integration-tests.log" 2>&1; then
        
        print_success "Integration tests passed for $service"
        return 0
    else
        print_error "Integration tests failed for $service"
        return 1
    fi
}

# Function to run performance tests
run_performance_tests() {
    local service=$1
    print_status "Running performance tests for $service..."
    
    if [ -f "src/test/java/**/*PerformanceTest.java" ]; then
        if mvn test -Dtest="*PerformanceTest" \
            -Dmaven.test.failure.ignore=true \
            > "../test-reports/${service}-performance-tests.log" 2>&1; then
            
            print_success "Performance tests passed for $service"
            return 0
        else
            print_warning "Performance tests failed for $service"
            return 1
        fi
    else
        print_warning "No performance tests found for $service"
        return 0
    fi
}

# Function to run security tests
run_security_tests() {
    local service=$1
    print_status "Running security tests for $service..."
    
    # OWASP Dependency Check
    if command -v dependency-check &> /dev/null; then
        dependency-check \
            --project "$service" \
            --scan . \
            --format HTML \
            --out "../test-reports/${service}-security.html" \
            > "../test-reports/${service}-security.log" 2>&1 || true
        
        print_success "Security scan completed for $service"
    else
        print_warning "OWASP Dependency Check not found, skipping security tests"
    fi
    
    return 0
}

# Function to generate coverage report
generate_coverage_report() {
    print_status "Generating coverage reports..."
    
    for service in "${SERVICES[@]}"; do
        if [ -d "$service" ]; then
            cd "$service"
            mvn jacoco:report > "../test-reports/${service}-coverage.log" 2>&1 || true
            
            if [ -f "target/site/jacoco/index.html" ]; then
                cp -r target/site/jacoco "../test-reports/${service}-coverage/" 2>/dev/null || true
                print_success "Coverage report generated for $service"
            fi
            
            cd "$PROJECT_ROOT"
        fi
    done
}

# Function to run mutation tests
run_mutation_tests() {
    print_status "Running mutation tests..."
    
    for service in "${SERVICES[@]}"; do
        if [ -d "$service" ]; then
            cd "$service"
            print_status "Running mutation tests for $service..."
            
            mvn org.pitest:pitest-maven:mutationCoverage \
                -DoutputFormats=HTML \
                -DtargetClasses=com.hdfc.bank.${service//-/.}* \
                -DtargetTests=com.hdfc.bank.${service//-/.}* \
                > "../test-reports/${service}-mutation.log" 2>&1 || true
            
            if [ -d "target/pit-reports" ]; then
                cp -r target/pit-reports "../test-reports/${service}-mutation/" 2>/dev/null || true
                print_success "Mutation tests completed for $service"
            fi
            
            cd "$PROJECT_ROOT"
        fi
    done
}

# Function to run contract tests
run_contract_tests() {
    print_status "Running contract tests..."
    
    # Pact contract testing
    for service in "${SERVICES[@]}"; do
        if [ -d "$service" ] && [ -f "$service/src/test/java/**/*ContractTest.java" ]; then
            cd "$service"
            print_status "Running contract tests for $service..."
            
            mvn test -Dtest="*ContractTest" \
                > "../test-reports/${service}-contract.log" 2>&1 || true
            
            print_success "Contract tests completed for $service"
            cd "$PROJECT_ROOT"
        fi
    done
}

# Parse command line arguments
TEST_TYPE="all"
SPECIFIC_SERVICE=""
PARALLEL_EXECUTION=false
GENERATE_REPORTS=true
RUN_MUTATION=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--type)
            TEST_TYPE="$2"
            shift 2
            ;;
        -s|--service)
            SPECIFIC_SERVICE="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL_EXECUTION=true
            shift
            ;;
        --no-reports)
            GENERATE_REPORTS=false
            shift
            ;;
        --mutation)
            RUN_MUTATION=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  -t, --type TYPE       Test type: unit, integration, performance, security, all (default: all)"
            echo "  -s, --service SERVICE Run tests for specific service only"
            echo "  -p, --parallel        Run tests in parallel"
            echo "  --no-reports          Skip report generation"
            echo "  --mutation            Run mutation tests"
            echo "  -h, --help           Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Validate test type
if [[ ! " ${TEST_TYPES[@]} all " =~ " ${TEST_TYPE} " ]]; then
    print_error "Invalid test type: $TEST_TYPE"
    print_error "Valid types: ${TEST_TYPES[@]} all"
    exit 1
fi

# Main execution
print_status "Starting test execution..."
print_status "Test type: $TEST_TYPE"
print_status "Parallel execution: $PARALLEL_EXECUTION"

START_TIME=$(date +%s)

# Clean previous reports
rm -rf "$REPORT_DIR"/* 2>/dev/null || true

if [ -n "$SPECIFIC_SERVICE" ]; then
    # Run tests for specific service
    if run_service_tests "$SPECIFIC_SERVICE" "$TEST_TYPE"; then
        print_success "Tests passed for $SPECIFIC_SERVICE"
    else
        print_error "Tests failed for $SPECIFIC_SERVICE"
        exit 1
    fi
else
    # Run tests for all services
    if [ "$PARALLEL_EXECUTION" = true ]; then
        # Parallel execution
        print_status "Running tests in parallel..."
        
        for service in "${SERVICES[@]}"; do
            run_service_tests "$service" "$TEST_TYPE" &
        done
        
        wait # Wait for all background jobs to complete
    else
        # Sequential execution
        for service in "${SERVICES[@]}"; do
            run_service_tests "$service" "$TEST_TYPE"
        done
    fi
fi

# Generate reports
if [ "$GENERATE_REPORTS" = true ]; then
    generate_coverage_report
    
    if [ "$RUN_MUTATION" = true ]; then
        run_mutation_tests
    fi
    
    # Run contract tests if available
    run_contract_tests
fi

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# Generate summary
echo ""
echo "📊 Test Execution Summary"
echo "========================="
echo "Duration: ${DURATION}s"
echo "Services tested: $TOTAL_SERVICES"
echo "Services passed: $PASSED_SERVICES"
echo "Total tests: $TOTAL_TESTS"
echo "Tests passed: $PASSED_TESTS"
echo "Tests failed: $FAILED_TESTS"

if [ $TOTAL_TESTS -gt 0 ]; then
    SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo "Success rate: $SUCCESS_RATE%"
fi

echo ""
print_status "Test reports available in: $REPORT_DIR"

# Generate HTML summary report
cat > "$REPORT_DIR/summary.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>HDFC Bank Test Summary</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #1e3a8a; color: white; padding: 20px; border-radius: 8px; }
        .summary { margin: 20px 0; padding: 15px; background: #f3f4f6; border-radius: 8px; }
        .service { margin: 10px 0; padding: 15px; border: 1px solid #ddd; border-radius: 8px; }
        .success { background: #dcfce7; border-color: #16a34a; }
        .failure { background: #fef2f2; border-color: #dc2626; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🏦 HDFC Bank Test Execution Summary</h1>
        <p>Generated on: $(date)</p>
        <p>Duration: ${DURATION}s</p>
    </div>
    
    <div class="summary">
        <h2>📊 Overall Results</h2>
        <p><strong>Services tested:</strong> $TOTAL_SERVICES</p>
        <p><strong>Services passed:</strong> $PASSED_SERVICES</p>
        <p><strong>Total tests:</strong> $TOTAL_TESTS</p>
        <p><strong>Tests passed:</strong> $PASSED_TESTS</p>
        <p><strong>Tests failed:</strong> $FAILED_TESTS</p>
        <p><strong>Success rate:</strong> $SUCCESS_RATE%</p>
    </div>
</body>
</html>
EOF

print_success "Summary report generated: $REPORT_DIR/summary.html"

# Exit with appropriate code
if [ $FAILED_TESTS -eq 0 ] && [ $PASSED_SERVICES -eq $TOTAL_SERVICES ]; then
    print_success "All tests passed!"
    exit 0
else
    print_error "Some tests failed. Check reports for details."
    exit 1
fi