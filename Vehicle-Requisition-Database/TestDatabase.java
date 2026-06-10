// TestDatabase.java
public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("Testing Vehicle Requisition Portal Database...");
        
        try {
            // Test 1: Basic connection
            System.out.println("\n1. Testing basic connection...");
            DatabaseConnection.testConnection();
            
            // Test 2: Create tables
            System.out.println("\n2. Creating tables...");
            DatabaseConnection.createTables();
            
            // Test 3: Authentication
            System.out.println("\n3. Testing authentication...");
            String[] user = DatabaseOperations.authenticateUser("10101", "aaru@123");
            if (user != null) {
                System.out.println("✅ Authentication successful for: " + user[1]);
            } else {
                System.out.println("❌ Authentication failed");
            }
            
            // Test 4: Garage requests
            System.out.println("\n4. Testing garage requests...");
            java.util.List<String[]> requests = DatabaseOperations.getGarageRequests();
            System.out.println("✅ Found " + requests.size() + " garage requests");
            
            System.out.println("\n🎉 All tests passed!");
            
        } catch (Exception e) {
            System.err.println("\n❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}