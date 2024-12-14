import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class StationeryItem {
    private String name;
    private double price;
    private int quantity;

    public StationeryItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void subtractFromQuantity(int quantityToSubtract) {
        if (quantityToSubtract <= quantity) {
            this.quantity -= quantityToSubtract;
        }
    }
}

public class StationeryItemsShop {
    private static final String DB_URL = "jdbc:mysql://localhost:3307/stationary_shop_db";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void main(String[] args) {
        List<StationeryItem> inventory = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        // Load JDBC Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found.");
            e.printStackTrace();
            return;
        }

        // Connect to the database and load inventory
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            String query = "SELECT name, price, quantity FROM stationery_items";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    inventory.add(new StationeryItem(
                            resultSet.getString("name"),
                            resultSet.getDouble("price"),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database.");
            e.printStackTrace();
            return;
        }

        // Display inventory
        System.out.println("Welcome to the Stationery Shop!");
        for (int i = 0; i < inventory.size(); i++) {
            StationeryItem item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - $" + item.getPrice());
        }

        double totalSales = 0;

        while (true) {
            System.out.print("Enter the number of the product you'd like to purchase (0 to exit): ");
            int choice = scanner.nextInt();

            if (choice == 0) break;

            if (choice > 0 && choice <= inventory.size()) {
                System.out.print("Enter quantity: ");
                int quantity = scanner.nextInt();

                StationeryItem selectedItem = inventory.get(choice - 1);

                if (quantity > 0 && quantity <= selectedItem.getQuantity()) {
                    double subtotal = selectedItem.getPrice() * quantity;
                    totalSales += subtotal;
                    selectedItem.subtractFromQuantity(quantity);
                    System.out.println("You purchased " + quantity + " " + selectedItem.getName() +
                            " for $" + subtotal);

                    // Update database
                    try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                        String updateQuery = "UPDATE stationery_items SET quantity = ? WHERE name = ?";
                        try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                            updateStatement.setInt(1, selectedItem.getQuantity());
                            updateStatement.setString(2, selectedItem.getName());
                            updateStatement.executeUpdate();
                        }
                    } catch (SQLException e) {
                        System.out.println("Error updating inventory.");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Insufficient stock.");
                }
            } else {
                System.out.println("Invalid choice.");
            }
        }

        System.out.println("Total Sales: $" + totalSales);
        System.out.println("Thank you for shopping with us!");
        scanner.close();
    }
}
