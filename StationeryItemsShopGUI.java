import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

public class StationeryItemsShopGUI {
    private static final String DB_URL = "jdbc:mysql://sql12.freesqldatabase.com:3306/sql12751962";
    private static final String USER = "sql12751962";
    private static final String PASS = "Pv3EqAxm5T";

    private List<StationeryItem> inventory = new ArrayList<>();
    private double totalSales = 0;

    public StationeryItemsShopGUI() {
        // Load JDBC Driver and Initialize Inventory
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            loadInventory();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "JDBC Driver not found.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Create GUI
        createAndShowGUI();
    }

    private void loadInventory() throws SQLException {
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
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Stationery Shop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        // Table to Display Inventory
        String[] columnNames = {"Item Name", "Price", "Quantity"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable inventoryTable = new JTable(tableModel);
        for (StationeryItem item : inventory) {
            tableModel.addRow(new Object[]{item.getName(), item.getPrice(), item.getQuantity()});
        }
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        // Purchase Section
        JPanel purchasePanel = new JPanel();
        purchasePanel.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel lblItemNumber = new JLabel("Item Number:");
        JTextField txtItemNumber = new JTextField();

        JLabel lblQuantity = new JLabel("Quantity:");
        JTextField txtQuantity = new JTextField();

        JButton btnPurchase = new JButton("Purchase");

        purchasePanel.add(lblItemNumber);
        purchasePanel.add(txtItemNumber);
        purchasePanel.add(lblQuantity);
        purchasePanel.add(txtQuantity);
        purchasePanel.add(btnPurchase);

        // Sales Display
        JLabel lblTotalSales = new JLabel("Total Sales: $0.00");

        // Purchase Button Action
        btnPurchase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int itemNumber = Integer.parseInt(txtItemNumber.getText());
                    int quantity = Integer.parseInt(txtQuantity.getText());

                    if (itemNumber > 0 && itemNumber <= inventory.size()) {
                        StationeryItem selectedItem = inventory.get(itemNumber - 1);

                        if (quantity > 0 && quantity <= selectedItem.getQuantity()) {
                            double subtotal = selectedItem.getPrice() * quantity;
                            totalSales += subtotal;
                            selectedItem.subtractFromQuantity(quantity);
                            lblTotalSales.setText("Total Sales: $" + String.format("%.2f", totalSales));

                            // Update Table
                            tableModel.setValueAt(selectedItem.getQuantity(), itemNumber - 1, 2);

                            // Update Database
                            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                                String updateQuery = "UPDATE stationery_items SET quantity = ? WHERE name = ?";
                                try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                                    updateStatement.setInt(1, selectedItem.getQuantity());
                                    updateStatement.setString(2, selectedItem.getName());
                                    updateStatement.executeUpdate();
                                }
                            }
                            JOptionPane.showMessageDialog(frame, "Purchase successful! Subtotal: $" + subtotal);
                        } else {
                            JOptionPane.showMessageDialog(frame, "Insufficient stock.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid item number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException | SQLException ex) {
                    JOptionPane.showMessageDialog(frame, "Error processing purchase.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        // Layout Configuration
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(purchasePanel, BorderLayout.SOUTH);
        frame.add(lblTotalSales, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StationeryItemsShopGUI::new);
    }
}
