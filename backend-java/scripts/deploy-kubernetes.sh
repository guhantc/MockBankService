#!/bin/bash

# HDFC Bank Kubernetes Deployment Script
# This script deploys all microservices to Kubernetes cluster

set -e

echo "🚀 Deploying HDFC Bank to Kubernetes..."
echo "======================================="

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

# Check if kubectl is installed and configured
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed. Please install kubectl and try again."
    exit 1
fi

# Check cluster connectivity
if ! kubectl cluster-info &> /dev/null; then
    print_error "Cannot connect to Kubernetes cluster. Please check your kubeconfig."
    exit 1
fi

# Project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KUBERNETES_DIR="$PROJECT_ROOT/kubernetes"

cd "$KUBERNETES_DIR"

# Deployment order (dependencies first)
DEPLOYMENT_ORDER=(
    "namespace.yaml"
    "oracle-database-deployment.yaml"
    "redis-deployment.yaml"
    "eureka-server-deployment.yaml"
    "api-gateway-deployment.yaml"
    "user-service-deployment.yaml"
    "saga-orchestrator-deployment.yaml"
    "account-service-deployment.yaml"
    "loan-service-deployment.yaml"
    "card-service-deployment.yaml"
    "investment-service-deployment.yaml"
    "transaction-service-deployment.yaml"
    "notification-service-deployment.yaml"
    "monitoring.yaml"
)

# Function to wait for deployment to be ready
wait_for_deployment() {
    local deployment_name=$1
    local namespace=${2:-hdfc-bank}
    local timeout=${3:-300}
    
    print_status "Waiting for deployment $deployment_name to be ready..."
    
    if kubectl wait --for=condition=available deployment/$deployment_name \
        --namespace=$namespace --timeout=${timeout}s &> /dev/null; then
        print_success "Deployment $deployment_name is ready"
        return 0
    else
        print_error "Deployment $deployment_name failed to become ready within ${timeout}s"
        return 1
    fi
}

# Function to wait for statefulset to be ready
wait_for_statefulset() {
    local statefulset_name=$1
    local namespace=${2:-hdfc-bank}
    local timeout=${3:-600}
    
    print_status "Waiting for statefulset $statefulset_name to be ready..."
    
    if kubectl wait --for=condition=ready pod -l app=$statefulset_name \
        --namespace=$namespace --timeout=${timeout}s &> /dev/null; then
        print_success "StatefulSet $statefulset_name is ready"
        return 0
    else
        print_error "StatefulSet $statefulset_name failed to become ready within ${timeout}s"
        return 1
    fi
}

# Deploy each component
for deployment_file in "${DEPLOYMENT_ORDER[@]}"; do
    if [ -f "$deployment_file" ]; then
        print_status "Deploying $deployment_file..."
        
        kubectl apply -f "$deployment_file"
        if [ $? -eq 0 ]; then
            print_success "$deployment_file applied successfully"
            
            # Wait for specific components to be ready
            case "$deployment_file" in
                "oracle-database-deployment.yaml")
                    wait_for_statefulset "oracle-database" "hdfc-bank" 600
                    ;;
                "redis-deployment.yaml")
                    wait_for_deployment "redis" "hdfc-bank" 120
                    ;;
                "eureka-server-deployment.yaml")
                    wait_for_deployment "eureka-server" "hdfc-bank" 180
                    ;;
                "api-gateway-deployment.yaml")
                    wait_for_deployment "api-gateway" "hdfc-bank" 120
                    ;;
                *"-deployment.yaml")
                    # Extract service name from filename
                    service_name=$(basename "$deployment_file" -deployment.yaml)
                    if [[ "$service_name" != "oracle-database" ]]; then
                        wait_for_deployment "$service_name" "hdfc-bank" 120
                    fi
                    ;;
            esac
        else
            print_error "Failed to apply $deployment_file"
            exit 1
        fi
    else
        print_warning "Deployment file $deployment_file not found, skipping..."
    fi
done

# Verify all deployments
print_status "Verifying all deployments..."
kubectl get all -n hdfc-bank

# Check service endpoints
print_status "Checking service endpoints..."
kubectl get endpoints -n hdfc-bank

# Display access information
echo ""
echo "🎉 Deployment completed successfully!"
echo "===================================="
echo ""
echo "📋 Access Information:"
echo "----------------------"

# Get API Gateway external IP
GATEWAY_IP=$(kubectl get service api-gateway -n hdfc-bank -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Pending")
if [ "$GATEWAY_IP" != "Pending" ] && [ -n "$GATEWAY_IP" ]; then
    echo "🌐 API Gateway: http://$GATEWAY_IP"
else
    echo "🌐 API Gateway: Use 'kubectl port-forward svc/api-gateway -n hdfc-bank 8080:80' to access locally"
fi

echo "📊 Monitoring:"
echo "  - Prometheus: kubectl port-forward svc/prometheus -n hdfc-bank 9090:9090"
echo "  - Grafana: kubectl port-forward svc/grafana -n hdfc-bank 3000:3000"
echo ""
echo "🔍 Useful Commands:"
echo "  - View pods: kubectl get pods -n hdfc-bank"
echo "  - View logs: kubectl logs -f <pod-name> -n hdfc-bank"
echo "  - View services: kubectl get svc -n hdfc-bank"
echo "  - Delete deployment: kubectl delete namespace hdfc-bank"
echo ""

# Save cluster info
echo "💾 Saving cluster information..."
kubectl get all -n hdfc-bank > "${PROJECT_ROOT}/deployment-status.txt"
print_success "Cluster information saved to deployment-status.txt"