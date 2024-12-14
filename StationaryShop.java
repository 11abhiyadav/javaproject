import java.util.Scanner;
import java.util.Vector;

class StationeryItem {
    String name;
    double price;
    int quantity;

    StationeryItem(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}

public class StationaryShop {
    public static void main(String[] args) {
        // Initialize a Vector for managing inventory
        Vector<StationeryItem> inventory = new Vector<>();
        inventory.add(new StationeryItem("Pens", 5, 100));
        inventory.add(new StationeryItem("Pencils", 5, 200));
        inventory.add(new StationeryItem("Notebooks", 50, 50));
        inventory.add(new StationeryItem("Staplers", 100, 20));
        inventory.add(new StationeryItem("Glue", 15, 30));

        double totalSales = 0.0;

        // Create a Scanner object to read user input
        Scanner scanner = new Scanner(System.in);

        // Display menu
        System.out.println("Welcome to the Stationary Shop!");
        System.out.println("-------------------------------");
        for (int i = 0; i < inventory.size(); i++) {
            StationeryItem item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.name + " - $" + item.price);
        }
        System.out.println("-------------------------------");

        // Loop until user chooses to exit
        while (true) {
            System.out.print("Enter the number of the product you'd like to purchase (or 0 to exit): ");
            int choice = scanner.nextInt();
            if (choice == 0) {
                break;
            } else if (choice > 0 && choice <= inventory.size()) {
                System.out.print("Enter the quantity you'd like to purchase: ");
                int quantity = scanner.nextInt();
                StationeryItem selectedItem = inventory.get(choice - 1);
                if (quantity > 0 && quantity <= selectedItem.quantity) {
                    double subtotal = selectedItem.price * quantity;
                    totalSales += subtotal;
                    selectedItem.quantity -= quantity;
                    System.out.println("You purchased " + quantity + " " + selectedItem.name + "(s) for $" + subtotal);
                } else {
                    System.out.println("Sorry, we don't have enough stock.");
                }
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }

        System.out.println("Thank you for shopping at the Stationary Shop!");
        System.out.println("Total sales: $" + totalSales);
        scanner.close();
    }
}

