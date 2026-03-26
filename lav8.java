import java.util.*;
import java.text.SimpleDateFormat;

public class Main {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Warehouse w = new Warehouse("Төв агуулах");
        Clerk c = new Clerk("Бат", w);

        Product p1 = new Product("Laptop", 10);
        Product p2 = new Product("Mouse", 50);
        w.addProduct(p1);
        w.addProduct(p2);

        c.receiveProduct(p1, 5, "Supplier A", sdf.parse("2026-03-26"));
        c.receiveProduct(p2, 20, "Supplier B", sdf.parse("2026-03-26"));

        c.issueProduct(p2, 10, "Customer X", sdf.parse("2026-03-27"));

        c.generateInventoryReport(new ArrayList<>());

        c.generateClerkReport(sdf.parse("2026-03-25"), sdf.parse("2026-03-27"), new ArrayList<>());

        c.performStockCount(p1, 20, sdf.parse("2026-03-28"));
    }
}
class Product {
    String name;
    int quantity;

    public Product(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }
}
class Warehouse {
    String name;
    Clerk clerk;
    List<Product> products = new ArrayList<>();

    public Warehouse(String name) {
        this.name = name;
    }

    void addProduct(Product p) {
        products.add(p);
    }
}
class Receipt {
    Date date;
    String supplier;
    Map<Product, Integer> items = new HashMap<>();

    void printReceipt() {
        System.out.println("Орлогын падаан");
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            System.out.println(entry.getKey().name + ": " + entry.getValue());
        }
    }
}

class Issue {
    Date date;
    String receiver;
    Map<Product, Integer> items = new HashMap<>();

    void printIssue() {
        System.out.println("Зарлагын падаан");
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            System.out.println(entry.getKey().name + ": " + entry.getValue());
        }
    }
}


class Clerk {
    String name;
    Warehouse warehouse;
    Map<Product, List<Receipt>> receipts = new HashMap<>();
    Map<Product, List<Issue>> issues = new HashMap<>();

    public Clerk(String name, Warehouse warehouse) {
        this.name = name;
        this.warehouse = warehouse;
    }

    void receiveProduct(Product p, int quantity, String supplier, Date date) {
        p.quantity += quantity;
        Receipt r = new Receipt();
        r.date = date;
        r.supplier = supplier;
        r.items.put(p, quantity);
        receipts.computeIfAbsent(p, k -> new ArrayList<>()).add(r);
        r.printReceipt();
    }

    void issueProduct(Product p, int quantity, String receiver, Date date) {
        if (p.quantity >= quantity) {
            p.quantity -= quantity;
            Issue i = new Issue();
            i.date = date;
            i.receiver = receiver;
            i.items.put(p, quantity);
            issues.computeIfAbsent(p, k -> new ArrayList<>()).add(i);
            i.printIssue();
        } else {
            System.out.println("Үлдэгдэл хүрэлцэхгүй: " + p.name);
        }
    }

    void generateInventoryReport(List<Product> selectedProducts) {
        System.out.println("Нөөцийн тайлан");
        List<Product> list = selectedProducts.isEmpty() ? warehouse.products : selectedProducts;
        for (Product p : list) {
            System.out.println(p.name + ": " + p.quantity);
        }
    }

    void generateClerkReport(Date startDate, Date endDate, List<Product> selectedProducts) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Няравын тайлан (" + sdf.format(startDate) + " - " + sdf.format(endDate) + ") ===");
        List<Product> list = selectedProducts.isEmpty() ? warehouse.products : selectedProducts;
        for (Product p : list) {
            int totalReceived = receipts.getOrDefault(p, new ArrayList<>())
                    .stream()
                    .filter(r -> !r.date.before(startDate) && !r.date.after(endDate))
                    .mapToInt(r -> r.items.get(p))
                    .sum();
            int totalIssued = issues.getOrDefault(p, new ArrayList<>())
                    .stream()
                    .filter(i -> !i.date.before(startDate) && !i.date.after(endDate))
                    .mapToInt(i -> i.items.get(p))
                    .sum();
            int startingBalance = p.quantity - totalReceived + totalIssued;
            int endingBalance = p.quantity;
            System.out.println(p.name + ": Эхний үлдэгдэл=" + startingBalance +
                    ", Орлого=" + totalReceived +
                    ", Зарлага=" + totalIssued +
                    ", Эцсийн үлдэгдэл=" + endingBalance);
        }
    }

    void performStockCount(Product p, int actualQuantity, Date date) {
        int diff = actualQuantity - p.quantity;
        if (diff > 0) System.out.println("Илүүдэл: +" + diff + " (" + p.name + ")");
        else if (diff < 0) System.out.println("Дутагдал: " + diff + " (" + p.name + ")");
        else System.out.println("Зөрүүгүй: " + p.name);
        p.quantity = actualQuantity;
    }
}