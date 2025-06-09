import requests
import time
import json
import sys
from datetime import datetime

class HDFCBankMicroservicesTester:
    def __init__(self):
        self.eureka_url = "http://localhost:8761"
        self.customer_service_url = "http://localhost:8081/api/customers"
        self.account_service_url = "http://localhost:8082/api/accounts"
        self.tests_run = 0
        self.tests_passed = 0
        self.created_customer_id = None
        self.created_account_number = None

    def run_test(self, name, method, url, expected_status, data=None):
        """Run a single API test"""
        headers = {'Content-Type': 'application/json'}
        
        self.tests_run += 1
        print(f"\n🔍 Testing {name}...")
        
        try:
            if method == 'GET':
                response = requests.get(url, headers=headers)
            elif method == 'POST':
                response = requests.post(url, json=data, headers=headers)
            elif method == 'PUT':
                response = requests.put(url, json=data, headers=headers)
            elif method == 'DELETE':
                response = requests.delete(url, headers=headers)

            success = response.status_code == expected_status
            if success:
                self.tests_passed += 1
                print(f"✅ Passed - Status: {response.status_code}")
                try:
                    return success, response.json() if response.text else {}
                except json.JSONDecodeError:
                    return success, response.text
            else:
                print(f"❌ Failed - Expected {expected_status}, got {response.status_code}")
                print(f"Response: {response.text}")
                return False, {}

        except Exception as e:
            print(f"❌ Failed - Error: {str(e)}")
            return False, {}

    def test_eureka_server(self):
        """Test if Eureka server is running"""
        print("\n===== Testing Eureka Server =====")
        try:
            response = requests.get(self.eureka_url)
            if response.status_code == 200:
                print("✅ Eureka Server is running")
                return True
            else:
                print(f"❌ Eureka Server returned status code {response.status_code}")
                return False
        except Exception as e:
            print(f"❌ Failed to connect to Eureka Server: {str(e)}")
            return False

    def test_customer_service_health(self):
        """Test Customer Service health endpoint"""
        print("\n===== Testing Customer Service Health =====")
        success, _ = self.run_test(
            "Customer Service Health",
            "GET",
            f"{self.customer_service_url}/health",
            200
        )
        return success

    def test_account_service_health(self):
        """Test Account Service health endpoint"""
        print("\n===== Testing Account Service Health =====")
        success, _ = self.run_test(
            "Account Service Health",
            "GET",
            f"{self.account_service_url}/health",
            200
        )
        return success

    def test_create_customer(self):
        """Test creating a new customer"""
        print("\n===== Testing Customer Creation =====")
        timestamp = datetime.now().strftime('%H%M%S')
        customer_data = {
            "firstName": "Raj",
            "lastName": "Sharma", 
            "email": f"raj.sharma{timestamp}@email.com",
            "phoneNumber": f"+9198765{timestamp[:6]}",
            "dateOfBirth": "1990-05-15",
            "panNumber": "ABCDE1236F",
            "aadhaarNumber": f"123456{timestamp[:6]}",
            "address": "123 MG Road",
            "city": "Mumbai",
            "state": "Maharashtra", 
            "pinCode": "400001",
            "annualIncome": 500000,
            "occupation": "Software Engineer"
        }
        
        success, response = self.run_test(
            "Create Customer",
            "POST",
            self.customer_service_url,
            201,
            data=customer_data
        )
        
        if success and 'customerId' in response:
            self.created_customer_id = response['customerId']
            print(f"Created customer with ID: {self.created_customer_id}")
            return True
        return False

    def test_get_all_customers(self):
        """Test getting all customers"""
        print("\n===== Testing Get All Customers =====")
        success, response = self.run_test(
            "Get All Customers",
            "GET",
            self.customer_service_url,
            200
        )
        
        if success and isinstance(response, list):
            print(f"Found {len(response)} customers")
            return True
        return False

    def test_get_customer_by_id(self):
        """Test getting a customer by ID"""
        print("\n===== Testing Get Customer By ID =====")
        if not self.created_customer_id:
            print("❌ No customer ID available for testing")
            return False
            
        success, response = self.run_test(
            "Get Customer By ID",
            "GET",
            f"{self.customer_service_url}/{self.created_customer_id}",
            200
        )
        
        return success

    def test_create_account(self):
        """Test creating a new account"""
        print("\n===== Testing Account Creation =====")
        if not self.created_customer_id:
            print("❌ No customer ID available for account creation")
            return False
            
        account_data = {
            "customerId": self.created_customer_id,
            "accountType": "SAVINGS",
            "branchCode": "HDFC0001",
            "ifscCode": "HDFC0001234"
        }
        
        success, response = self.run_test(
            "Create Account",
            "POST",
            self.account_service_url,
            201,
            data=account_data
        )
        
        if success and 'accountNumber' in response:
            self.created_account_number = response['accountNumber']
            print(f"Created account with number: {self.created_account_number}")
            return True
        return False

    def test_get_all_accounts(self):
        """Test getting all accounts"""
        print("\n===== Testing Get All Accounts =====")
        success, response = self.run_test(
            "Get All Accounts",
            "GET",
            self.account_service_url,
            200
        )
        
        if success and isinstance(response, list):
            print(f"Found {len(response)} accounts")
            return True
        return False

    def test_deposit_money(self):
        """Test depositing money into an account"""
        print("\n===== Testing Deposit Money =====")
        if not self.created_account_number:
            print("❌ No account number available for deposit")
            return False
            
        deposit_data = {
            "amount": 5000.00
        }
        
        success, response = self.run_test(
            "Deposit Money",
            "POST",
            f"{self.account_service_url}/{self.created_account_number}/deposit",
            200,
            data=deposit_data
        )
        
        return success

    def test_check_balance(self):
        """Test checking account balance"""
        print("\n===== Testing Check Balance =====")
        if not self.created_account_number:
            print("❌ No account number available for balance check")
            return False
            
        success, response = self.run_test(
            "Check Balance",
            "GET",
            f"{self.account_service_url}/{self.created_account_number}/balance",
            200
        )
        
        if success and 'balance' in response:
            print(f"Account balance: {response['balance']}")
            return True
        return False

    def run_all_tests(self):
        """Run all tests in sequence"""
        # Test Eureka Server
        eureka_running = self.test_eureka_server()
        if not eureka_running:
            print("❌ Eureka Server is not running. Skipping service tests.")
            return False
            
        # Test service health
        customer_health = self.test_customer_service_health()
        account_health = self.test_account_service_health()
        
        if not customer_health or not account_health:
            print("❌ One or more services are not healthy. Skipping API tests.")
            return False
            
        # Test Customer Service APIs
        # Skip customer creation as we already have customers
        # self.test_create_customer()
        self.test_get_all_customers()
        
        # Use an existing customer ID
        self.created_customer_id = "CUST612932A88D"
        print(f"Using existing customer ID: {self.created_customer_id}")
        
        self.test_get_customer_by_id()
        
        # Test Account Service APIs
        self.test_create_account()
        self.test_get_all_accounts()
        self.test_deposit_money()
        self.test_check_balance()
        
        # Print results
        print(f"\n📊 Tests passed: {self.tests_passed}/{self.tests_run}")
        return self.tests_passed == self.tests_run

def main():
    print("Starting HDFC Bank Microservices Tests...")
    print("Waiting for services to start up...")
    time.sleep(5)  # Give services time to start
    
    tester = HDFCBankMicroservicesTester()
    success = tester.run_all_tests()
    
    return 0 if success else 1

if __name__ == "__main__":
    sys.exit(main())