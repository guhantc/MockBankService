#!/bin/bash

# HDFC Bank Microservices Build Script
# This script builds all microservices and creates Docker images

set -e

echo "🏗️  Building HDFC Bank Microservices..."
echo "========================================"

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

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker and try again."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven and try again."
    exit 1
fi

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Services to build
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

# Build each service
for service in "${SERVICES[@]}"; do
    if [ -d "$service" ]; then
        print_status "Building $service..."
        
        cd "$service"
        
        # Clean and compile
        mvn clean compile -q
        if [ $? -eq 0 ]; then
            print_success "$service compiled successfully"
        else
            print_error "Failed to compile $service"
            exit 1
        fi
        
        # Run tests
        print_status "Running tests for $service..."
        mvn test -q
        if [ $? -eq 0 ]; then
            print_success "$service tests passed"
        else
            print_warning "$service tests failed, but continuing build..."
        fi
        
        # Package
        mvn package -DskipTests -q
        if [ $? -eq 0 ]; then
            print_success "$service packaged successfully"
        else
            print_error "Failed to package $service"
            exit 1
        fi
        
        # Build Docker image
        if [ -f "../docker/Dockerfile.$service" ]; then
            print_status "Building Docker image for $service..."
            docker build -f "../docker/Dockerfile.$service" -t "hdfc-bank/$service:1.0.0" ..
            if [ $? -eq 0 ]; then
                print_success "Docker image for $service built successfully"
            else
                print_error "Failed to build Docker image for $service"
                exit 1
            fi
        else
            print_warning "No Dockerfile found for $service"
        fi
        
        cd "$PROJECT_ROOT"
    else
        print_warning "Service directory $service not found, skipping..."
    fi
done

print_success "All services built successfully!"

# Generate build report
echo ""
echo "📋 Build Report"
echo "==============="
echo "Build Date: $(date)"
echo "Built Services:"

for service in "${SERVICES[@]}"; do
    if [ -d "$service" ]; then
        if docker images | grep -q "hdfc-bank/$service"; then
            echo "  ✅ $service"
        else
            echo "  ❌ $service (Docker image not found)"
        fi
    fi
done

echo ""
print_status "To start all services, run: docker-compose -f docker/docker-compose.yml up -d"
print_status "To deploy to Kubernetes, run: ./scripts/deploy-kubernetes.sh"