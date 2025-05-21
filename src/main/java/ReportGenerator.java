import java.io.*;
import java.util.*;

public class ReportGenerator {

    static class TaskRunnable implements Runnable {
        private final String path;
        private double totalCost;
        private int totalAmount;
        private int totalDiscountSum;
        private int totalLines;
        private Product mostExpensiveProduct;
        private double highestCostAfterDiscount;

        public TaskRunnable(String path) {
            this.path = path;
            this.totalCost = 0;
            this.totalAmount = 0;
            this.totalDiscountSum = 0;
            this.totalLines = 0;
            this.highestCostAfterDiscount = 0;
            this.mostExpensiveProduct = null;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip empty lines
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        String[] parts = line.split("\\s*,\\s*"); // Handles spaces around commas
                        if (parts.length != 3) {
                            System.err.println("Invalid line format in " + path + ": " + line);
                            continue;
                        }

                        int productId = Integer.parseInt(parts[0]);
                        int amount = Integer.parseInt(parts[1]);
                        int discount = Integer.parseInt(parts[2]);

                        // Validate input values
                        if (amount <= 0 || discount < 0 || discount > 100) {
                            System.err.println("Invalid values in " + path + ": " + line);
                            continue;
                        }

                        Product product = findProductById(productId);
                        if (product == null) {
                            System.err.println("Product not found in catalog: " + productId);
                            continue;
                        }

                        double cost = product.getPrice() * amount;
                        double discountedCost = cost * (1 - discount / 100.0);

                        // Update statistics
                        synchronized (this) {
                            totalCost += discountedCost;
                            totalAmount += amount;
                            totalDiscountSum += discount;
                            totalLines++;

                            if (discountedCost > highestCostAfterDiscount) {
                                highestCostAfterDiscount = discountedCost;
                                mostExpensiveProduct = product;
                            }
                        }

                    } catch (NumberFormatException e) {
                        System.err.println("Number format error in " + path + ": " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing file " + path + ": " + e.getMessage());
            }
        }

        private Product findProductById(int id) {
            for (Product p : productCatalog) {
                if (p != null && p.getProductID() == id) return p;
            }
            return null;
        }

        public void makeReport() {
            System.out.println("Report for file: " + path);
            System.out.printf("Total cost: $%.2f%n", totalCost);
            System.out.println("Total items bought: " + totalAmount);
            double averageDiscount = totalLines > 0 ? (double) totalDiscountSum / totalLines : 0;
            System.out.printf("Average discount: %.2f%%%n", averageDiscount);
            if (mostExpensiveProduct != null) {
                System.out.printf("Most expensive purchase (after discount): %s - $%.2f%n",
                        mostExpensiveProduct.getProductName(), highestCostAfterDiscount);
            } else {
                System.out.println("Most expensive purchase: N/A");
            }
            System.out.println("--------------------------------------------------");
        }
    }

    static class Product {
        private final int productID;
        private final String productName;
        private final double price;

        public Product(int productID, String productName, double price) {
            this.productID = productID;
            this.productName = productName;
            this.price = price;
        }

        public int getProductID() {
            return productID;
        }

        public String getProductName() {
            return productName;
        }

        public double getPrice() {
            return price;
        }
    }

    private static final String[] ORDER_FILES = {
            "src\\main\\resources\\2021_order_details.txt",
            "src\\main\\resources\\2022_order_details.txt",
            "src\\main\\resources\\2023_order_details.txt",
            "src\\main\\resources\\2024_order_details.txt"
    };

    static Product[] productCatalog = new Product[10];

    public static void loadProducts() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\Products.txt"))) {
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null && index < productCatalog.length) {
                String[] parts = line.trim().split(",");
                if (parts.length != 3) continue;

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();
                double price = Double.parseDouble(parts[2].trim());

                productCatalog[index++] = new Product(id, name, price);
            }
            System.out.println(productCatalog);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        try {
            loadProducts();
        } catch (IOException e) {
            System.out.println("Error loading product catalog.");
            e.printStackTrace();
            return;
        }

        List<TaskRunnable> tasks = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();

        for (String orderFile : ORDER_FILES) {
            TaskRunnable task = new TaskRunnable(orderFile);
            Thread thread = new Thread(task);
            tasks.add(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("===== Final Reports =====");
        for (TaskRunnable task : tasks) {
            task.makeReport();
        }
    }
}
